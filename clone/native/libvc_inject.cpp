/**
 * libvc_clone.so — replacement for /data/libvc.so
 *
 * ShadowHook hooks on libcameraservice.so returnBufferCheckedLocked (3 variants).
 * Injects NV12 frames from FrameInjectQueue (fed by vcplax_clone decode thread).
 *
 * Export: init(void* loadedLibConfig) — ABI-compatible with original libvc.so
 * Export: vc_clone_publish_frame — C API for vcplax decode thread (same process via dlopen)
 */

#include "FrameInjectQueue.h"
#include "HookSymbols.h"

#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>

#include <atomic>
#include <cstdint>
#include <cstring>

#define LOG_TAG "libvc_clone"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace vlive::clone {
namespace {

using shadowhook_init_fn = int (*)(int mode, int debug);
using shadowhook_hook_sym_fn = void* (*)(const char* lib, const char* sym, void* replace,
                                         void** orig);

shadowhook_init_fn p_shadowhook_init = nullptr;
shadowhook_hook_sym_fn p_shadowhook_hook_sym_name = nullptr;

struct android_ycbcr {
    void* y;
    void* cb;
    void* cr;
    size_t ystride;
    size_t cstride;
    size_t chroma_step;
};

using gb_from_fn = void* (*)(void* anwBuffer);
using lock_ycbcr_fn = int (*)(void* gb, uint32_t usage, android_ycbcr* ycbcr);
using unlock_fn = int (*)(void* gb);

gb_from_fn p_gb_from = nullptr;
lock_ycbcr_fn p_lock_ycbcr = nullptr;
unlock_fn p_unlock = nullptr;

struct camera3_stream {
    int stream_type;
    uint32_t width;
    uint32_t height;
    uint32_t format;
    uint32_t usage;
    size_t max_buffers;
    void* priv;
};

struct camera3_stream_buffer {
    camera3_stream* stream;
    void* buffer;
    int status;
    int acquire_fence;
    int release_fence;
};

std::atomic<bool> g_hooks_live{false};

using rb_v1_fn = int (*)(void* self, const camera3_stream_buffer* sb, int64_t ts, int64_t ts2,
                         int bi, int flag, void* vec, void* fence);
using rb_v2_fn = int (*)(void* self, const camera3_stream_buffer* sb, int64_t ts, bool flag,
                         void* vec, void* fence);
using rb_v3_fn = int (*)(void* self, const void* sb, int64_t ts, bool flag, void* vec,
                         void* fence);
using disconnect_fn = void (*)(void* device);

rb_v1_fn orig_return_buffer_v1 = nullptr;
rb_v2_fn orig_return_buffer_v2 = nullptr;
rb_v3_fn orig_return_buffer_v3 = nullptr;
disconnect_fn orig_disconnect = nullptr;

bool ensureGraphicBufferApi() {
    if (p_gb_from) return true;
    void* ui = dlopen("libui.so", RTLD_NOW | RTLD_NOLOAD);
    if (!ui) ui = dlopen("libui.so", RTLD_NOW);
    if (!ui) {
        ALOGE("dlopen libui.so: %s", dlerror());
        return false;
    }
    p_gb_from = reinterpret_cast<gb_from_fn>(
        dlsym(ui, "_ZN7android13GraphicBuffer4fromEP19ANativeWindowBuffer"));
    p_lock_ycbcr = reinterpret_cast<lock_ycbcr_fn>(
        dlsym(ui, "_ZN7android13GraphicBuffer9lockYCbCrEjP13android_ycbcr"));
    p_unlock =
        reinterpret_cast<unlock_fn>(dlsym(ui, "_ZN7android13GraphicBuffer6unlockEv"));
    if (!p_gb_from || !p_lock_ycbcr || !p_unlock) {
        ALOGE("GraphicBuffer symbols missing");
        return false;
    }
    return true;
}

void copyNv12ToYcbcr(const uint8_t* src, int width, int height, const android_ycbcr& ycbcr) {
    const size_t ySize = static_cast<size_t>(width) * static_cast<size_t>(height);
    auto* yPlane = static_cast<uint8_t*>(ycbcr.y);
    for (int row = 0; row < height; ++row) {
        std::memcpy(yPlane + row * ycbcr.ystride, src + row * width,
                    static_cast<size_t>(width));
    }

    const uint8_t* srcUv = src + ySize;
    auto* dstUv = static_cast<uint8_t*>(ycbcr.cb);
    const int uvRows = height / 2;
    const int uvWidth = width;
    for (int row = 0; row < uvRows; ++row) {
        std::memcpy(dstUv + row * ycbcr.cstride, srcUv + row * uvWidth,
                    static_cast<size_t>(uvWidth));
    }
}

bool injectIntoBuffer(const camera3_stream_buffer* sb, const Nv12Frame& frame) {
    if (!sb->buffer || !ensureGraphicBufferApi()) return false;

    void* gb = p_gb_from(sb->buffer);
    if (!gb) return false;

    android_ycbcr ycbcr{};
    constexpr uint32_t kSwWrite = 0x33;  // GRALLOC_USAGE_SW_READ/WRITE_OFTEN
    if (p_lock_ycbcr(gb, kSwWrite, &ycbcr) != 0) return false;

    copyNv12ToYcbcr(frame.data.data(), frame.width, frame.height, ycbcr);
    p_unlock(gb);
    return true;
}

bool tryInject(const camera3_stream_buffer* sb) {
    if (!sb || !sb->stream) return false;
    const int w = static_cast<int>(sb->stream->width);
    const int h = static_cast<int>(sb->stream->height);
    auto frame = FrameInjectQueue::instance().consume(w, h);
    if (!frame) return false;
    if (injectIntoBuffer(sb, *frame)) {
        ALOGI("inject OK %dx%d bytes=%zu", w, h, frame->data.size());
        return true;
    }
    ALOGI("inject fallback (lock failed) %dx%d", w, h);
    return false;
}

int hook_return_buffer_v1(void* self, const camera3_stream_buffer* sb, int64_t ts, int64_t ts2,
                          int bi, int flag, void* vec, void* fence) {
    if (g_hooks_live && sb && (flag || sb->status == 0)) tryInject(sb);
    if (orig_return_buffer_v1) {
        return orig_return_buffer_v1(self, sb, ts, ts2, bi, flag, vec, fence);
    }
    return -1;
}

int hook_return_buffer_v2(void* self, const camera3_stream_buffer* sb, int64_t ts, bool flag,
                          void* vec, void* fence) {
    if (g_hooks_live && sb && (flag || sb->status == 0)) tryInject(sb);
    if (orig_return_buffer_v2) return orig_return_buffer_v2(self, sb, ts, flag, vec, fence);
    return -1;
}

int hook_return_buffer_v3(void* self, const void* sb, int64_t ts, bool flag, void* vec,
                          void* fence) {
    if (g_hooks_live && sb) {
        tryInject(reinterpret_cast<const camera3_stream_buffer*>(sb));
    }
    if (orig_return_buffer_v3) return orig_return_buffer_v3(self, sb, ts, flag, vec, fence);
    return -1;
}

void hook_disconnect(void* device) {
    ALOGI("disconnect — clearing inject queue");
    FrameInjectQueue::instance().clear();
    if (orig_disconnect) orig_disconnect(device);
}

void* hook_worker(void* /*arg*/) {
    for (int i = 0; i < 50; ++i) {
        if (FrameInjectQueue::instance().publishCount() > 0) break;
        usleep(200000);
    }
    ALOGI("hook_worker done (published=%llu)",
          (unsigned long long)FrameInjectQueue::instance().publishCount());
    return nullptr;
}

bool loadShadowHook(const char* shadowhookPath) {
    void* h = dlopen(shadowhookPath, RTLD_NOW | RTLD_GLOBAL);
    if (!h) {
        ALOGE("dlopen shadowhook %s: %s", shadowhookPath, dlerror());
        return false;
    }
    p_shadowhook_init = reinterpret_cast<shadowhook_init_fn>(dlsym(h, "shadowhook_init"));
    p_shadowhook_hook_sym_name =
        reinterpret_cast<shadowhook_hook_sym_fn>(dlsym(h, "shadowhook_hook_sym_name"));
    if (!p_shadowhook_init || !p_shadowhook_hook_sym_name) {
        ALOGE("shadowhook symbols missing");
        return false;
    }
    if (p_shadowhook_init(1, 0) != 0) {
        ALOGE("shadowhook_init failed");
        return false;
    }
    return true;
}

bool installHooks() {
    using namespace hooks;
    void* r1 = p_shadowhook_hook_sym_name(kHookLib, kReturnBufferV1,
                                          reinterpret_cast<void*>(hook_return_buffer_v1),
                                          reinterpret_cast<void**>(&orig_return_buffer_v1));
    void* r2 = p_shadowhook_hook_sym_name(kHookLib, kReturnBufferV2,
                                          reinterpret_cast<void*>(hook_return_buffer_v2),
                                          reinterpret_cast<void**>(&orig_return_buffer_v2));
    void* r3 = p_shadowhook_hook_sym_name(kHookLib, kReturnBufferV3,
                                          reinterpret_cast<void*>(hook_return_buffer_v3),
                                          reinterpret_cast<void**>(&orig_return_buffer_v3));
    p_shadowhook_hook_sym_name(kHookLib, kDisconnect, reinterpret_cast<void*>(hook_disconnect),
                               reinterpret_cast<void**>(&orig_disconnect));
    ALOGI("hook v1=%p v2=%p v3=%p", r1, r2, r3);
    g_hooks_live = (r1 != nullptr || r2 != nullptr || r3 != nullptr);
    return g_hooks_live;
}

}  // namespace

extern "C" void vc_clone_publish_frame(int width, int height, const uint8_t* nv12, size_t size) {
    Nv12Frame f;
    f.width = width;
    f.height = height;
    f.data.assign(nv12, nv12 + size);
    FrameInjectQueue::instance().publish(std::move(f));
}

extern "C" int init(void* /*loadedLibConfig*/) {
    const char* shPath = "/data/libvc++.so";
    if (!loadShadowHook(shPath)) {
        shPath = "/data/libshadowhook.so";
        if (!loadShadowHook(shPath)) return -1;
    }
    if (!installHooks()) {
        ALOGE("no hooks installed");
        return -1;
    }
    pthread_t tid;
    pthread_create(&tid, nullptr, hook_worker, nullptr);
    pthread_detach(tid);
    ALOGI("init OK — libcameraservice hooks live");
    return 0;
}

}  // namespace vlive::clone
