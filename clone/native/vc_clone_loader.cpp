#include "VcCloneLoader.h"

#include <android/log.h>
#include <dlfcn.h>

#define LOG_TAG "vcplax_clone"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace vlive::clone {

namespace {

using init_fn = int (*)(void*);
using publish_fn = void (*)(int, int, const uint8_t*, size_t);

void* g_libvc = nullptr;
init_fn g_init = nullptr;
publish_fn g_publish = nullptr;

}  // namespace

bool loadLibVc(const char* path) {
    if (g_libvc) return g_init != nullptr;

    const char* shadowPaths[] = {"/data/libvc++.so", "/data/libshadowhook.so", nullptr};
    for (int i = 0; shadowPaths[i]; ++i) {
        if (dlopen(shadowPaths[i], RTLD_NOW | RTLD_GLOBAL)) {
            ALOGI("preloaded %s", shadowPaths[i]);
            break;
        }
    }

    g_libvc = dlopen(path, RTLD_NOW | RTLD_GLOBAL);
    if (!g_libvc) {
        ALOGE("dlopen %s: %s", path, dlerror());
        return false;
    }

    g_init = reinterpret_cast<init_fn>(dlsym(g_libvc, "init"));
    g_publish = reinterpret_cast<publish_fn>(dlsym(g_libvc, "vc_clone_publish_frame"));
    if (!g_init) {
        ALOGE("dlsym init missing");
        return false;
    }
    if (!g_publish) {
        ALOGE("dlsym vc_clone_publish_frame missing");
        return false;
    }

    if (g_init(nullptr) != 0) {
        ALOGE("init() failed");
        return false;
    }

    ALOGI("loadLibVc OK path=%s", path);
    return true;
}

void publishInjectFrame(int width, int height, const uint8_t* nv12, size_t size) {
    if (g_publish) g_publish(width, height, nv12, size);
}

}  // namespace vlive::clone
