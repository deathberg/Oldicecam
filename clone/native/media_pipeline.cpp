#include "FrameInjectQueue.h"
#include "MediaContext.h"
#include "VcCloneLoader.h"

#include <android/log.h>
#include <pthread.h>
#include <unistd.h>

#include <atomic>
#include <cstring>

#define LOG_TAG "vcplax_clone"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace vlive::clone {

namespace {
std::atomic<bool> g_decodeRunning{false};
pthread_t g_decodeThread{};
int g_frameWidth = 640;
int g_frameHeight = 480;
MediaContext* g_ctx = nullptr;
uint8_t g_pat = 0;

void* decodeLoop(void* arg) {
    auto* ctx = static_cast<MediaContext*>(arg);
    ALOGI("decode thread start %dx%d", g_frameWidth, g_frameHeight);
    while (g_decodeRunning.load()) {
        Nv12Frame frame = makeSolidNv12(g_frameWidth, g_frameHeight, g_pat, 128, 128);
        g_pat = static_cast<uint8_t>(g_pat + 3);
        publishInjectFrame(frame.width, frame.height, frame.data.data(), frame.data.size());

        ctx->pollCounters[0] = 1;
        ctx->pollCounters[1] = g_frameWidth;
        ctx->pollCounters[2] = g_frameHeight;
        ctx->pollCounters[3] = 0;

        usleep(33333);  // ~30 fps stub
    }
    ALOGI("decode thread stop");
    return nullptr;
}

void startDecodeThread(MediaContext* ctx) {
    if (g_decodeRunning.exchange(true)) return;
    g_ctx = ctx;
    pthread_create(&g_decodeThread, nullptr, decodeLoop, ctx);
}

void stopDecodeThread() {
    if (!g_decodeRunning.exchange(false)) return;
    pthread_join(g_decodeThread, nullptr);
}

}  // namespace

bool MediaPipeline::setMode(int mode, const std::string& pathOrUrl) {
    std::lock_guard<std::mutex> lock(mu_);
    teardownPipeline();
    ctx_->mode = mode;
    ctx_->sourcePath = pathOrUrl;
    ctx_->pipelineArmed = true;
    ALOGI("setMode mode=%d path=%s", mode, pathOrUrl.c_str());
    if (mode == 1) return openLocalFile(pathOrUrl);
    if (mode == 2) return openRtmpUrl(pathOrUrl);
    return false;
}

bool MediaPipeline::playSource(const std::string& path, int /*mirrorIgnored*/, bool loop) {
    std::lock_guard<std::mutex> lock(mu_);
    ctx_->sourcePath = path;
    ctx_->loop = loop;
    running_ = true;
    ctx_->pipelineArmed = true;
    startDecodeThread(ctx_);
    ALOGI("playSource path=%s loop=%d", path.c_str(), loop);
    return true;
}

void MediaPipeline::stop() {
    std::lock_guard<std::mutex> lock(mu_);
    ALOGI("stop");
    stopDecodeThread();
    running_ = false;
    ctx_->pollCounters[0] = 0;
    teardownPipeline();
}

int MediaPipeline::getStatus() const {
    return running_ ? STATUS_PLAYING : 0;
}

void MediaPipeline::seekRange(int64_t beginMs, int64_t endMs) {
    std::lock_guard<std::mutex> lock(mu_);
    ctx_->seekBeginMs = beginMs;
    ctx_->seekEndMs = endMs;
    ctx_->seekPending = true;
    ALOGI("seekRange %lld..%lld ms", (long long)beginMs, (long long)endMs);
}

void MediaPipeline::setTransform(const MediaContext::TransformState& t) {
    std::lock_guard<std::mutex> lock(mu_);
    ctx_->transform = t;
    ALOGI("transform mode=%d x=%.2f y=%.2f int=%.3f dia=%.3f color=%d",
          t.mode, t.x, t.y, t.intensity, t.diameter, t.colorMode);
}

void MediaPipeline::onFrameDecoded(int width, int height) {
    ctx_->pollCounters[1] = width;
    ctx_->pollCounters[2] = height;
}

void MediaPipeline::onNetworkStats(int32_t bytesPerSec) {
    ctx_->pollCounters[4] = bytesPerSec;
}

bool MediaPipeline::openLocalFile(const std::string& path) {
    ALOGI("openLocalFile %s (TODO: avformat_open_input + AMediaCodec)", path.c_str());
    g_frameWidth = 640;
    g_frameHeight = 480;
    return true;
}

bool MediaPipeline::openRtmpUrl(const std::string& url) {
    ALOGI("openRtmpUrl %s (TODO: FFmpeg rtmp)", url.c_str());
    g_frameWidth = 1280;
    g_frameHeight = 720;
    return true;
}

void MediaPipeline::teardownPipeline() {
    stopDecodeThread();
    running_ = false;
}

}  // namespace vlive::clone
