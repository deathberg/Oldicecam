# Full native decompilation — `vcplax` + `libvc.so`

Decompiler: **Ghidra 11.3.1** headless (arm64-v8a). Image base `0x100000` (subtract for file VAs).
Curated C output:
- [`reconstructed/native-src/vcplax/vcplax_daemon.decompiled.c`](../reconstructed/native-src/vcplax/vcplax_daemon.decompiled.c)
- [`reconstructed/native-src/libvc/libvc_hooks.decompiled.c`](../reconstructed/native-src/libvc/libvc_hooks.decompiled.c)

Full raw dumps (artifacts, not committed): `libvc_full_decompiled.c.gz` (2368 functions),
`vcplax_handlers_decompiled.c`.

This closes the previously-open native layer: both binaries are now decompiled to C pseudocode
and the app-specific logic is mapped function-by-function. (FFmpeg/libc++/STL internals are
upstream code and are identified, not re-reversed.)

---

## 1. `vcplax` — Binder daemon (the camera-injection server)

`main()` registers a `BBinder` under the masquerade service name (argv[1]); `onTransact`
(`FUN_0053f8b4` @ file `0x43f8b4`) dispatches `com.xiaomi.vlive.IMyBinderService`.

### onTransact dispatch — decompiled, with MediaContext offsets

`ctx = *(self + 0x38)` (the MediaContext). Verified field writes:

| tx | handler | effect on ctx | reply* |
|---:|---------|---------------|:-----:|
| 0x0b PLAY_SOURCE | `FUN_0053fedc` | alloc 0x14c session, `pipeline_start`, spawn decode thread (ctx+0x20) | 1 |
| 0x0c | `FUN_005402b0` | — | 2 |
| 0x0d POLL_STATE | `FUN_00540324` | writes 5×int32 counters | int[] |
| 0x0e SET_MODE | `FUN_005403d4` | mode + path | 4 |
| 0x0f GET_STATUS | `FUN_00541f7c(ctx,0,-1)` | — | 5 |
| 0x10 AUTO_ROTATE | inline | `ctx+0xa8 = bool` | 6 |
| 0x11 SET_LOOP | inline | `ctx+0x98 = bool` | 7 |
| 0x12 SET_ANGLE | inline | validate {0,90,180,270}; `ctx+0xac=deg`, `ctx+0xa8=flag` | 8 |
| 0x13 SET_MIRROR | inline | `ctx+0xa8=0`, `ctx+0xb0=bool` | 9 |
| 0x16 SEEK_RANGE | inline | `readInt64 ×2 → FUN_00541f7c(ctx,begin,end)` | 12 |
| 0x18 TRANSFORM | inline | int mode + 4×float + int colorMode → `ctx+0x160..0x174` | 14 |
| 0x19 HARD_RECOVERY | inline | `self+0x44 ^= 1` | void |
| else | `BBinder::onTransact` | — | — |

\* reply values from the live trace (`docs/RUNTIME_PROTOCOL_VERIFICATION.md`); pattern `reply = code−10`.

**These offsets exactly match `clone/include/MediaContext.h`** — the clone's MediaContext layout
is now decompiler-verified, not inferred.

### Playback pipeline — `pipeline_start` (`FUN_00541108`), FFmpeg identified

```
mode = strncasecmp(uri,"rtmp://",7)==0 ? 2 : 1     // ctx+0x18
avformat_open_input(&fmt, uri, ...)                 // FUN_00545618  ("avformat_alloc_context")
avformat_find_stream_info(fmt, ...)                 // FUN_00547354
for each stream: if codec_type==VIDEO(0):
    dec   = avcodec_find_decoder(codecpar->codec_id)         // FUN_00671bd4
    avctx = avcodec_alloc_context3(dec)                      // FUN_0094a698
    avcodec_parameters_to_context(avctx, codecpar)           // FUN_007464e4
    avcodec_open2(avctx, dec, NULL)                          // FUN_006d5cec
    // rotation: AV_PKT_DATA_DISPLAYMATRIX(5) or "rotate" tag -> ctx+0x80 (deg % 360)
```

So `vcplax` statically links **FFmpeg (libavformat/libavcodec)** and decodes the user video/RTMP
stream itself, then hands frames to the camera-hook side via the shared inject queue. The FLV/RTMP
muxer is also present (`FUN_0056259c`, strings `libavformat/flvenc.c`, `aac_adtstoasc`).

### TX13 poll (`FUN_00540324`)

`writeNoException` then 5×`writeInt32` from globals `DAT_00c79bf8/bfc/...` →
`[active, width, height, net, queue]` (semantics from the runtime trace).

---

## 2. `libvc.so` — camera hook engine (fully decompiled)

`init(ctx)` (`FUN_001774b0`, file `0x774b0`) — entry called by `vcplax` after dlopen:

1. Copies the **LoadedLib config** from `ctx` into a global struct `PTR_DAT_0023a090`; the
   per-string XOR key bytes live at `ctx[0x08,0x7d,0x7f,0x29,0x4f,0x44]` — exactly the
   `keyOff` values in `tools/decode_libvc_xor.py`.
2. XOR-decodes 6 `.rodata` blobs (`DAT_0014ae86/ae99/af38/afd5/b06e/b0a2`) with the loop
   `out[i] = ((i + (seed ^ blob[i])) - 0x11) ^ (key + i)`, `seed=7, seed+=0x1f` — the exact
   algorithm reproduced in the decode tool. Decoded (verified on two devices):
   - lib `libcameraservice.so`
   - `android::camera3::Camera3OutputStream::returnBufferCheckedLocked` (3 ABI variants)
   - `android::Camera3Device::initializeCommonLocked` / `::disconnect` (fallbacks)
3. `shadowhook_init(1,0)` then `shadowhook_hook_sym_name(lib, sym, FUN_xxx, &orig)` installs:
   - `FUN_00177238/772e4/77370` → the 3 returnBuffer hooks (call injector then chain orig
     `DAT_0023cb80/88/90`)
   - `FUN_001773fc/77408` → initializeCommonLocked / disconnect trampolines
4. `pthread_create(FUN_00178558)` — worker waiting on the inject queue.

### Frame injection (the actual pixel replacement)

The returnBuffer hooks call `FUN_00177150` → the big format handler, which (decompiled):

- locks the camera output `GraphicBuffer` (`android::GraphicBuffer::lock/unlock`),
- looks up a queued replacement frame keyed by `WxH` (`FUN_0017572c`/`FUN_001786fc`),
- **YV12** (`0x32315659`): copies the decoded plane into the buffer,
- **vendor/IMPL_DEFINED** (`0x7fa30c06`): `GraphicBuffer::lock` + `memcpy` injected frame,
- **BLOB/JPEG** (`0x21`): finds the JPEG EOI (`FF D9`), re-encodes the injected frame within the
  marker (`FUN_0017662c`) and `memcpy`s it back.

This is exactly the design in `clone/native/libvc_inject.cpp` — now confirmed against the real
decompilation (the clone used `lockYCbCr`; the original uses the lower-level `GraphicBuffer::lock`
with manual plane copy + per-format paths).

---

## 3. Coverage after full decompilation

| Layer | Before archive/decompile | Now |
|-------|-------------------------:|----:|
| Binder protocol | ~90% | **100%** (dispatch + offsets + replies decompiler-verified) |
| `libvc` hook engine | ~50% | **~95%** (init/XOR/hooks/inject all decompiled to C) |
| `vcplax` media pipeline | ~85% | **~92%** (FFmpeg demux/decode call graph identified; FFmpeg internals are upstream) |
| `vcplax` daemon scaffolding | ~85% | **~95%** (onTransact + session + pipeline_start decompiled) |

What remains genuinely out of scope: re-deriving statically-linked **FFmpeg** source (pointless —
it is upstream open source), and exact byte-level reproduction of the JPEG re-encode quality
parameters. The app-specific native logic is now decompiled end-to-end.

> Method note: Ghidra C pseudocode uses synthetic `FUN_<addr>`/`DAT_<addr>` names and inferred
> types; this is faithful behavioral reconstruction, not the original C++ source tree.
