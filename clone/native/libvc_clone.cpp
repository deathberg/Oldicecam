/**
 * libvc_clone.cpp — BBinder onTransact skeleton for IMyBinderService.
 *
 * Wire-up with Android NDK:
 *   #include <binder/BBinder.h>
 *   #include <binder/IPCThreadState.h>
 *   #include <binder/IServiceManager.h>
 *   #include <binder/Parcel.h>
 */

#include "MediaContext.h"

#include <android/log.h>
#include <binder/Binder.h>
#include <binder/IBinder.h>
#include <binder/IInterface.h>
#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/Parcel.h>
#include <binder/ProcessState.h>
#include <utils/String16.h>
#include <utils/String8.h>

#include <mutex>

#define LOG_TAG "libvc_clone"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace vlive::clone {

static constexpr const char* kDescriptor = "com.xiaomi.vlive.IMyBinderService";

// ─── MediaPipeline stubs (replace with FFmpeg + AMediaCodec) ───────────────

bool MediaPipeline::setMode(int mode, const std::string& pathOrUrl) {
    std::lock_guard<std::mutex> lock(mu_);
    teardownPipeline();
    ctx_->mode = mode;
    ctx_->sourcePath = pathOrUrl;
    ALOGI("setMode mode=%d path=%s", mode, pathOrUrl.c_str());
    if (mode == 1) return openLocalFile(pathOrUrl);
    if (mode == 2) return openRtmpUrl(pathOrUrl);
    return false;
}

bool MediaPipeline::playSource(const std::string& path, int /*mirrorIgnored*/, bool loop) {
    std::lock_guard<std::mutex> lock(mu_);
    ctx_->sourcePath = path;
    ctx_->loop = loop;
    ALOGI("playSource path=%s loop=%d", path.c_str(), loop);
    running_ = true;
    ctx_->pollCounters[0]++;  // placeholder: frames/sessions started
    return true;
}

void MediaPipeline::stop() {
    std::lock_guard<std::mutex> lock(mu_);
    ALOGI("stop");
    running_ = false;
    teardownPipeline();
}

int MediaPipeline::getStatus() const {
    return running_ ? STATUS_PLAYING : 0;
}

void MediaPipeline::seekRange(int64_t beginUs, int64_t endUs) {
    std::lock_guard<std::mutex> lock(mu_);
    ctx_->seekBeginUs = beginUs;
    ctx_->seekEndUs = endUs;
    ALOGI("seekRange %lld..%lld us", (long long)beginUs, (long long)endUs);
    // Original: FUN_00541f7c(ctx, beginUs, endUs)
}

void MediaPipeline::setTransform(const MediaContext::TransformState& t) {
    std::lock_guard<std::mutex> lock(mu_);
    ctx_->transform = t;
    ALOGI("transform mode=%d x=%.2f y=%.2f int=%.3f dia=%.3f color=%d",
          t.mode, t.x, t.y, t.intensity, t.diameter, t.colorMode);
}

void MediaPipeline::onFrameDecoded() {
    ctx_->pollCounters[1]++;  // placeholder: decoded frames
}

void MediaPipeline::onNetworkStats(int32_t bytesPerSec) {
    ctx_->pollCounters[4] = bytesPerSec;
}

bool MediaPipeline::openLocalFile(const std::string& path) {
    ALOGI("openLocalFile %s (avformat_open_input + AMediaCodec decoder)", path.c_str());
    // TODO: avformat_open_input, find video stream, AMediaCodec_createDecoderByType("video/avc")
    return true;
}

bool MediaPipeline::openRtmpUrl(const std::string& url) {
    ALOGI("openRtmpUrl %s (libavformat rtmp:// + ffrtmpcrypt)", url.c_str());
    // TODO: AVDictionary rtmp options, avformat_open_input, custom IO for inject
    return true;
}

void MediaPipeline::teardownPipeline() {
    // TODO: avformat_close_input, AMediaCodec_delete, join decode thread
    running_ = false;
}

// ─── BBinder service ───────────────────────────────────────────────────────

class MyBinderService : public android::BBinder {
public:
    MyBinderService() : pipeline_(&ctx_) {}

    android::status_t onTransact(uint32_t code, const android::Parcel& data,
                                 android::Parcel* reply, uint32_t flags) override {
        android::status_t err = android::BBinder::onTransact(code, data, reply, flags);
        if (err != android::UNKNOWN_TRANSACTION) return err;

        android::String16 descriptor(kDescriptor);
        if (!data.enforceInterface(descriptor)) {
            return android::PERMISSION_DENIED;
        }

        switch (code) {
            case TX_PLAY_SOURCE:
                return handlePlaySource(data, reply);
            case TX_STOP_OR_QUERY:
                return handleStop(data, reply);
            case TX_POLL_STATE:
                return handlePollState(data, reply);
            case TX_SET_MODE:
                return handleSetMode(data, reply);
            case TX_GET_STATUS:
                return handleGetStatus(data, reply);
            case TX_SET_AUTO_ROTATE:
                return handleSetBool(data, reply, &ctx_.autoRotate, OK_AUTO_ROTATE);
            case TX_SET_LOOP:
                return handleSetBool(data, reply, &ctx_.loop, OK_LOOP);
            case TX_SET_ANGLE:
                return handleSetAngle(data, reply);
            case TX_SET_MIRROR:
                return handleSetBool(data, reply, &ctx_.mirror, OK_MIRROR);
            case TX_SEEK_RANGE:
                return handleSeekRange(data, reply);
            case TX_TRANSFORM:
                return handleTransform(data, reply);
            case TX_HARD_RECOVERY:
                return handleHardRecovery(data, reply);
            default:
                return android::UNKNOWN_TRANSACTION;
        }
    }

private:
    MediaContext ctx_;
    MediaPipeline pipeline_;
    std::mutex txMu_;

    android::status_t handlePlaySource(const android::Parcel& data, android::Parcel* reply) {
        android::String16 path16;
        android::status_t st = data.readString16(&path16);
        if (st != android::NO_ERROR) return st;
        int32_t int0 = 0;
        int32_t loopVal = 0;
        st = data.readInt32(&int0);
        if (st != android::NO_ERROR) return st;
        st = data.readInt32(&loopVal);
        if (st != android::NO_ERROR) return st;

        std::string path = android::String8(path16).string();
        {
            std::lock_guard<std::mutex> lock(txMu_);
            pipeline_.playSource(path, int0, loopVal != 0);
        }
        reply->writeNoException();
        reply->writeInt32(OK_PLAY);
        return android::NO_ERROR;
    }

    android::status_t handleStop(const android::Parcel& /*data*/, android::Parcel* reply) {
        std::lock_guard<std::mutex> lock(txMu_);
        pipeline_.stop();
        reply->writeNoException();
        reply->writeInt32(OK_STOP);
        return android::NO_ERROR;
    }

    android::status_t handlePollState(const android::Parcel& /*data*/, android::Parcel* reply) {
        reply->writeNoException();
        // Java: reply.createIntArray() — length prefix + N int32
        reply->writeInt32(5);
        for (int i = 0; i < 5; ++i) {
            reply->writeInt32(ctx_.pollCounters[i]);
        }
        return android::NO_ERROR;
    }

    android::status_t handleSetMode(const android::Parcel& data, android::Parcel* reply) {
        int32_t mode = 0;
        android::status_t st = data.readInt32(&mode);
        if (st != android::NO_ERROR) return st;
        android::String16 path16;
        st = data.readString16(&path16);
        if (st != android::NO_ERROR) return st;

        std::string path = android::String8(path16).string();
        {
            std::lock_guard<std::mutex> lock(txMu_);
            pipeline_.setMode(mode, path);
        }
        reply->writeNoException();
        reply->writeInt32(OK_SET_MODE);
        return android::NO_ERROR;
    }

    android::status_t handleGetStatus(const android::Parcel& /*data*/, android::Parcel* reply) {
        int status;
        {
            std::lock_guard<std::mutex> lock(txMu_);
            status = pipeline_.getStatus();
        }
        reply->writeNoException();
        reply->writeInt32(status ? STATUS_PLAYING : 0);
        return android::NO_ERROR;
    }

    android::status_t handleSetBool(const android::Parcel& data, android::Parcel* reply,
                                    bool* field, ReplyCode ack) {
        int32_t v = 0;
        android::status_t st = data.readInt32(&v);
        if (st != android::NO_ERROR) return st;
        {
            std::lock_guard<std::mutex> lock(txMu_);
            *field = (v != 0);
        }
        reply->writeNoException();
        reply->writeInt32(ack);
        return android::NO_ERROR;
    }

    android::status_t handleSetAngle(const android::Parcel& data, android::Parcel* reply) {
        int32_t deg = 0;
        android::status_t st = data.readInt32(&deg);
        if (st != android::NO_ERROR) return st;
        {
            std::lock_guard<std::mutex> lock(txMu_);
            ctx_.angleDegrees = deg;
        }
        reply->writeNoException();
        reply->writeInt32(OK_ANGLE);
        return android::NO_ERROR;
    }

    android::status_t handleSeekRange(const android::Parcel& data, android::Parcel* reply) {
        int64_t beginUs = 0;
        int64_t endUs = 0;
        android::status_t st = data.readInt64(&beginUs);
        if (st != android::NO_ERROR) return st;
        st = data.readInt64(&endUs);
        if (st != android::NO_ERROR) return st;
        {
            std::lock_guard<std::mutex> lock(txMu_);
            pipeline_.seekRange(beginUs, endUs);
        }
        reply->writeNoException();
        reply->writeInt32(OK_SEEK);
        return android::NO_ERROR;
    }

    android::status_t handleTransform(const android::Parcel& data, android::Parcel* reply) {
        MediaContext::TransformState t{};
        android::status_t st = data.readInt32(&t.mode);
        if (st != android::NO_ERROR) return st;
        st = data.readFloat(&t.x);
        if (st != android::NO_ERROR) return st;
        st = data.readFloat(&t.y);
        if (st != android::NO_ERROR) return st;
        st = data.readFloat(&t.intensity);
        if (st != android::NO_ERROR) return st;
        st = data.readFloat(&t.diameter);
        if (st != android::NO_ERROR) return st;
        st = data.readInt32(&t.colorMode);
        if (st != android::NO_ERROR) return st;
        {
            std::lock_guard<std::mutex> lock(txMu_);
            pipeline_.setTransform(t);
        }
        reply->writeNoException();
        reply->writeInt32(OK_TRANSFORM);
        return android::NO_ERROR;
    }

    android::status_t handleHardRecovery(const android::Parcel& /*data*/, android::Parcel* reply) {
        {
            std::lock_guard<std::mutex> lock(txMu_);
            ctx_.hardRecovery = !ctx_.hardRecovery;
            ALOGI("hardRecovery toggled -> %d", ctx_.hardRecovery);
        }
        reply->writeNoException();
        // Original TX25: reply parcel size=4 (no return int)
        return android::NO_ERROR;
    }
};

}  // namespace vlive::clone

// ─── Daemon entry (vcplax replacement) ─────────────────────────────────────

using android::defaultServiceManager;
using android::sp;
using android::String16;

int main(int argc, char** argv) {
    if (argc < 2) {
        ALOGE("usage: %s <service_name>", argv[0]);
        return 1;
    }

    sp<android::IServiceManager> sm = defaultServiceManager();
    sp<android::BBinder> svc = new vlive::clone::MyBinderService();
    sm->addService(String16(argv[1]), svc);

    android::ProcessState::self()->startThreadPool();
    android::IPCThreadState::self()->joinThreadPool();
    return 0;
}
