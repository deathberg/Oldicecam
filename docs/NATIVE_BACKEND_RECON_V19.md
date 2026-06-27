# Native Backend Reconstruction Notes for IceCam v19

Static materials reviewed:

- `report.html`
- `91`
- original APK structure/strings/libs
- current v18 Java source

## Confirmed native topology

The original backend is not a pure file player. It contains a frame-oriented path:

```text
GraphicBuffer / Surface / YUV frames
 -> ashmem / IMemory style frame exchange
 -> AMediaCodec H.264 encode/decode
 -> FFmpeg / FLV mux
 -> RTMP socket
```

Native libraries seen in original APK:

```text
libshadowhook.so
libvc.so
vcplax.so
```

Important native capabilities seen in strings/imports/reports:

```text
GraphicBuffer::lock
GraphicBuffer::lockYCbCr
MemoryHeapBase / MemoryBase / IMemory
AMediaCodec_createEncoderByType
AMediaCodec_createDecoderByType
AMediaCodec_queueInputBuffer
AMediaCodec_dequeueOutputBuffer
ANativeWindow_fromSurface
EGL / GLESv2
FFmpeg / librtmp network output
```

## Expected frame metadata

The reports describe a ring/shared-memory frame descriptor equivalent to:

```c
struct capture_frame_t {
    uint64_t timestamp_us;
    uint32_t width;
    uint32_t height;
    uint32_t y_stride;
    uint32_t uv_stride;
    uint32_t data_offset;
};
```

The exact chroma layout must still be probed before a YUV bridge is enabled.
Report snippets disagree between I420/YV12-like and NV21/NV12-like order.

## IceCam v18/v19 compatibility path

Current Java app still uses:

```text
TX14(mode=1, path) -> TX11(path, 0, loop)
```

This remains a legacy control path only. It is useful for compatibility and fallback,
but it should not be used as the final transform mechanism.

## Next architecture target

```text
MediaCodec decoder output Surface
 -> SurfaceTexture / OES texture
 -> OpenGL ES transform matrix
 -> encoder input Surface or YUV shared-memory bridge
 -> native backend / RTMP session
```

`TX24` must not be used for geometry transform in this branch. It is treated as a
native color/debug path. `TX25` remains reserved for hard restore only.
