/**
 * libvc_clone — Binder service skeleton replacing vcplax BBinder side.
 *
 * Implements com.xiaomi.vlive.IMyBinderService transaction codes 11–25 (UI subset).
 * Camera injection is a separate libvc_replacement.so (ShadowHook on libcameraservice.so).
 *
 * See docs/RE_FINAL_REPORT.md for verified MediaContext offsets and inject architecture.
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

/** vcplax MediaContext — offsets verified Ghidra + runtime (2026-06-27). */
struct MediaContext {
    int mode = 1;              // +0x18: 1=local file, 2=RTMP URL
    std::string sourcePath;    // +0x20: path or rtmp:// URL
    bool pipelineArmed = false; // +0x28: set before decode thread spawn
    void* libvcReady = nullptr; // +0x30
    void* decodeThread = nullptr; // +0x48
    int64_t seekBeginMs = 0;   // +0x58 (TX22 uses milliseconds, not µs)
    int64_t seekEndMs = 0;     // +0x60
    bool loop = false;         // +0x98
    bool autoRotate = false;   // +0xa8
    int angleDegrees = 0;      // +0xac: 0, 0x5a, 0xb4, 0x10e, 0x168
    bool mirror = false;       // +0xb0
    bool seekPending = false;  // +0x10c

    struct TransformState {
        int mode = 0;          // +0x160
        float x = 0.f;         // +0x164
        float y = 0.f;         // +0x168
        float diameter = 0.f;  // +0x16c
        float intensity = 0.f; // +0x170
        int colorMode = 2;     // +0x174
    } transform;

    bool hardRecovery = false; // service BBinder+0x44 toggle (TX25)

    // TX13 poll counters (globals DAT_00c79bf8.. in original).
    // Verified live (docs/RUNTIME_PROTOCOL_VERIFICATION.md):
    //   c0=active/playing flag, c1=last frame width, c2=last frame height,
    //   c3=network/RTMP counter (0 for local file), c4=queue/reserved.
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
    void seekRange(int64_t beginMs, int64_t endMs);
    void setTransform(const MediaContext::TransformState& t);

    /** Called from decode thread — updates TX13 counters + libvc inject queue. */
    void onFrameDecoded(int width, int height);
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
