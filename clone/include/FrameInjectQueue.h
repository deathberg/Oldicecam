#pragma once

#include <cstdint>
#include <mutex>
#include <optional>
#include <unordered_map>
#include <vector>

namespace vlive::clone {

/** NV12 frame blob keyed by stream WxH (matches libvc lookup_stream_by_id). */
struct Nv12Frame {
    int width = 0;
    int height = 0;
    std::vector<uint8_t> data;  // Y plane + interleaved UV (w*h*3/2)
};

inline uint64_t streamKey(int width, int height) {
    return (static_cast<uint64_t>(static_cast<uint32_t>(width)) << 32) |
           static_cast<uint32_t>(height);
}

/**
 * Thread-safe latest-frame queue shared between vcplax decode thread and libvc hooks.
 * Mirrors original globals @ libvc 0x13cad8 / 0x13cac0 (simplified to latest-frame slot).
 */
class FrameInjectQueue {
public:
    static FrameInjectQueue& instance();

    void publish(Nv12Frame frame);
    std::optional<Nv12Frame> consume(int width, int height);
    bool hasFrame(int width, int height) const;
    void clear();

    /** Stats for TX13 / debug. */
    uint64_t publishCount() const { return publishCount_; }
    uint64_t consumeCount() const { return consumeCount_; }

private:
    FrameInjectQueue() = default;

    mutable std::mutex mu_;
    std::unordered_map<uint64_t, Nv12Frame> latest_;
    uint64_t publishCount_ = 0;
    uint64_t consumeCount_ = 0;
};

/** Build solid-color NV12 test pattern (for decode stub). */
Nv12Frame makeSolidNv12(int width, int height, uint8_t y, uint8_t u, uint8_t v);

}  // namespace vlive::clone
