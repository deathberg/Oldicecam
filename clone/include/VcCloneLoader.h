#pragma once

#include <cstddef>
#include <cstdint>

namespace vlive::clone {

/** Load /data/libvc.so (libvc_clone build) and call init(). Must run before decode thread. */
bool loadLibVc(const char* path = "/data/libvc.so");

/** Publish NV12 to inject queue (no-op until loadLibVc succeeds). */
void publishInjectFrame(int width, int height, const uint8_t* nv12, size_t size);

}  // namespace vlive::clone
