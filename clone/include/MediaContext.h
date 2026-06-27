/**
 * libvc_clone — Binder service skeleton replacing vcplax BBinder side.
 *
 * Implements com.xiaomi.vlive.IMyBinderService transaction codes 11–25 (UI subset).
 * Does NOT implement libvc GraphicBuffer hooks — that is a separate injection layer.
 *
 * Build (NDK r26+, arm64-v8a):
 *   cmake -B build -DANDROID_ABI=arm64-v8a -DCMAKE_TOOLCHAIN_FILE=$NDK/build/cmake/android.toolchain.cmake
 *   cmake --build build
 */

#pragma once

#include <cstdint>
#include <mutex>
#include <string>
#include <vector>

namespace vlive::clone {

/** Mirrors vcplax media context fields recovered from Ghidra (partial). */
struct MediaContext {
    int mode = 1;              // 1=local file, 2=RTMP URL
    std::string sourcePath;    // path or rtmp:// URL
    bool loop = false;
    bool autoRotate = false;
    bool mirror = false;
    int angleDegrees = 0;
    int64_t seekBeginUs = 0;
    int64_t seekEndUs = 0;

    struct TransformState {
        int mode = 0;
        float x = 0.f;
        float y = 0.f;
        float intensity = 0.f;
        float diameter = 0.f;
        int colorMode = 2;
    } transform;

    bool hardRecovery = false;

    // TX13 counters — labels TBD after Frida [POLL_STATE] capture
    int32_t pollCounters[5] = {0, 0, 0, 0, 0};
};

enum TxCode : uint32_t {
    TX_PLAY_SOURCE     = 11,
    TX_STOP_OR_QUERY   = 12,
    TX_POLL_STATE      = 13,
    TX_SET_MODE        = 14,
    TX_GET_STATUS      = 15,
    TX_SET_AUTO_ROTATE = 16,
    TX_SET_LOOP        = 17,
    TX_SET_ANGLE       = 18,
    TX_SET_MIRROR      = 19,
    TX_SEEK_RANGE      = 22,
    TX_TRANSFORM       = 24,
    TX_HARD_RECOVERY   = 25,
};

enum ReplyCode : int32_t {
    OK_PLAY         = 1,
    OK_STOP         = 2,
    OK_SET_MODE     = 4,
    STATUS_PLAYING  = 5,
    OK_AUTO_ROTATE  = 6,
    OK_LOOP         = 7,
    OK_ANGLE        = 8,
    OK_MIRROR       = 9,
    OK_SEEK         = 12,
    OK_TRANSFORM    = 14,
};

class MediaPipeline {
public:
    explicit MediaPipeline(MediaContext* ctx) : ctx_(ctx) {}

    /** TX14 — set mode + path, flush + reconfigure demux. */
    bool setMode(int mode, const std::string& pathOrUrl);

    /** TX11 — start demux/decode thread for path (after TX14 or standalone). */
    bool playSource(const std::string& path, int mirrorIgnored, bool loop);

    void stop();
    int getStatus() const;
    void seekRange(int64_t beginUs, int64_t endUs);
    void setTransform(const MediaContext::TransformState& t);

    /** Called from decode thread — updates TX13 counters. */
    void onFrameDecoded();
    void onNetworkStats(int32_t bytesPerSec);

private:
    MediaContext* ctx_;
    std::mutex mu_;
    bool running_ = false;

    bool openLocalFile(const std::string& path);
    bool openRtmpUrl(const std::string& url);
    void teardownPipeline();
};

}  // namespace vlive::clone
