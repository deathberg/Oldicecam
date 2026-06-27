#include "FrameInjectQueue.h"

#include <cstring>

namespace vlive::clone {

FrameInjectQueue& FrameInjectQueue::instance() {
    static FrameInjectQueue q;
    return q;
}

void FrameInjectQueue::publish(Nv12Frame frame) {
    if (frame.width <= 0 || frame.height <= 0 || frame.data.empty()) return;
    std::lock_guard<std::mutex> lock(mu_);
    latest_[streamKey(frame.width, frame.height)] = std::move(frame);
    ++publishCount_;
}

std::optional<Nv12Frame> FrameInjectQueue::consume(int width, int height) {
    std::lock_guard<std::mutex> lock(mu_);
    auto it = latest_.find(streamKey(width, height));
    if (it == latest_.end()) return std::nullopt;
    ++consumeCount_;
    return it->second;
}

bool FrameInjectQueue::hasFrame(int width, int height) const {
    std::lock_guard<std::mutex> lock(mu_);
    return latest_.find(streamKey(width, height)) != latest_.end();
}

void FrameInjectQueue::clear() {
    std::lock_guard<std::mutex> lock(mu_);
    latest_.clear();
}

Nv12Frame makeSolidNv12(int width, int height, uint8_t y, uint8_t u, uint8_t v) {
    Nv12Frame f;
    f.width = width;
    f.height = height;
    const size_t ySize = static_cast<size_t>(width) * static_cast<size_t>(height);
    const size_t uvSize = ySize / 2;
    f.data.resize(ySize + uvSize);
    std::memset(f.data.data(), y, ySize);
    for (size_t i = 0; i < uvSize; i += 2) {
        f.data[ySize + i] = u;
        f.data[ySize + i + 1] = v;
    }
    return f;
}

}  // namespace vlive::clone
