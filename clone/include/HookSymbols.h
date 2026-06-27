#pragma once

/**
 * Decoded ShadowHook targets for libvc.so (runtime pull jysdd4, tools/decode_libvc_xor.py).
 * See docs/RE_FINAL_REPORT.md §3.
 */

namespace vlive::clone::hooks {

inline constexpr char kHookLib[] = "libcameraservice.so";

inline constexpr char kReturnBufferV1[] =
    "_ZN7android7camera319Camera3OutputStream25returnBufferCheckedLockedERKNS0_20"
    "camera_stream_bufferEllbiRKNSt3__16vectorImNS5_9allocatorImEEEEPNS_2spINS_5FenceEEE";

inline constexpr char kReturnBufferV2[] =
    "_ZN7android7camera319Camera3OutputStream25returnBufferCheckedLockedERKNS0_20"
    "camera_stream_bufferElbRKNSt3__16vectorImNS5_9allocatorImEEEEPNS_2spINS_5FenceEEE";

inline constexpr char kReturnBufferV3[] =
    "_ZN7android7camera319Camera3OutputStream25returnBufferCheckedLockedERK21camera3_stream_"
    "bufferxbRKNSt3__16vectorIjNS5_9allocatorIjEEEEPNS_2spINS_5FenceEEE";

inline constexpr char kInitCommonLocked[] =
    "_ZN7android13Camera3Device22initializeCommonLockedEv";

inline constexpr char kDisconnect[] = "_ZN7android13Camera3Device10disconnectEv";

}  // namespace vlive::clone::hooks
