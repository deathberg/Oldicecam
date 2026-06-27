# Runtime protocol verification — second-device capture

Source: runtime capture archive (`ymc53v.zip`) pulled by the in-app RE Tool (`retool`)
from a **live rooted device** running `com.potplayer.music`. Evidence committed under
[`docs/runtime/`](runtime/).

This is a **different device** from the earlier capture in
[`BINDER_TX_RUNTIME_ANALYSIS.md`](BINDER_TX_RUNTIME_ANALYSIS.md) (Poco X3 Pro / Android 13).
Its value is **cross-device validation** of the reverse-engineering plus finalizing a few
runtime semantics. It contains **no new code**: the pulled native binaries are byte-identical
to the APK's (see §4).

---

## 1. Binder protocol — fully confirmed from a live transaction trace

`docs/runtime/binder_tx_capture_device2.log` is a Frida trace of `vcplax`'s
`onTransact` (interface token `com.xiaomi.vlive.IMyBinderService`). Every transaction's
**reply value** is now ground-truth, and confirms the pattern **reply = code − 10**:

| tx | name | request payload | reply (verified) |
|---:|------|-----------------|-----------------:|
| 11 | PLAY_SOURCE | `String path` + `int` + `int loop` | `1` |
| 14 | SET_MODE | `int mode` + `String path` | `4` |
| 15 | GET_STATUS | (empty) | `5` = playing |
| 16 | SET_AUTO_ROTATE | `int bool` | `6` |
| 17 | SET_LOOP | `int bool` | `7` |
| 18 | SET_ANGLE | `int degrees` | `8` |
| 19 | SET_MIRROR | `int bool` | `9` |
| 22 | SEEK_RANGE | `long begin` + `long end` | `12` |
| 24 | TRANSFORM | `int mode` + 4×`float` + `int colorMode` | `14` |
| 25 | HARD_RECOVERY | (empty) | **void** (reply parcel size 4, no int) |
| 13 | POLL_STATE | (empty) | `int[]` counters (≈1 Hz background poll) |

These match the reply codes already encoded in `clone/include/MediaContext.h`
(`ReplyCode`) and the proxy in `vcamera/.../binder/VcBinderClient.java` — i.e. the clone
is wire-correct.

### Decoded payload examples (from the trace)

- **SET_MODE mode=1** → `path = /data/user/0/com.pot…` (local file).
- **SET_MODE mode=2** → `path = rtmp://ns8.indexfo…` (RTMP live). Confirms `mode 1=local file, 2=RTMP`.
- **TRANSFORM** (seq 6): `mode=0, x=70.0, y=71.0, intensity=0.24, diameter=0.47, colorMode=2`
  (`00 00 8c 42` = 70.0f, `00 00 8e 42` = 71.0f, `8f c2 75 3e` = 0.24f, `d7 a3 f0 3e` = 0.47f).

## 2. TX13 poll counters — semantics finalized

The trace's `[POLL_STATE]` deltas tie the counters to the decode pipeline:

| slot | meaning | evidence |
|------|---------|----------|
| `c0` | active/playing flag (0→1 when playback starts) | `DELTA {c0:0->1}` right after PLAY_SOURCE |
| `c1` | **last decoded frame width** | `c1=1280` then `c1=640` |
| `c2` | **last decoded frame height** | `c2=960` then `c2=480` (1280×960, 640×480) |
| `c3` | network/RTMP counter (0 for local file) | stays 0 in these local sessions |

Counters reset to 0 on TRANSFORM / source change, then repopulate on the next decoded frame.

## 3. libvc camera-hook targets — re-verified on a 2nd device

Running `tools/decode_libvc_xor.py` on the pulled `libvc.so` (XOR-obfuscated `.rodata`
strings) yields the **same** hook chain as the original analysis — same target symbols **and
the same per-string XOR keys** — proving the hooks are build/device-stable
(`docs/runtime/libvc_hook_symbols_device2.txt`):

- Hooked library: **`libcameraservice.so`**
- Primary target (3 ABI variants): `android::camera3::Camera3OutputStream::returnBufferCheckedLocked(...)`
- Fallbacks: `android::Camera3Device::initializeCommonLocked()`, `…::disconnect()`

Mechanism: ShadowHook (`/data/libvc++.so`) installs inline hooks on
`returnBufferCheckedLocked`; when a camera output buffer is about to be returned to a client
app, libvc overwrites its pixels with the decoded injected frame (NV12 → YCbCr via
`GraphicBuffer::lockYCbCr`). This is exactly the skeleton in
[`clone/native/libvc_inject.cpp`](../clone/native/libvc_inject.cpp).

## 4. Live service masquerade — confirmed

- `vcplax` was launched as `/data/vcplax dataloader_managerhow` and registered that name in
  ServiceManager (`docs/runtime/service_list_device2.txt`):
  `dataloader_managerhow: []` (empty interface = the fake service), sitting next to the real
  `dataloader_manager: [android.content.pm.IDataLoaderManager]`.
- This confirms the masquerade in `App.getServerName()` (real ServiceManager name + random
  lowercase suffix), recovered in the source reconstruction.

## 5. No new code in this archive

The pulled binaries are **byte-identical** to the APK natives already analyzed:

| file | sha256 (arm64-v8a) | == APK? |
|------|--------------------|:------:|
| `vcplax` | `5619ead4786977cbd8a1eab376c7d753848f0f1bb3ed6446f282d0e41e8bb37f` | yes |
| `libvc.so` | `924ce9570640e1c5c62cc1d5dd11b06f565437d0ca50f569a1d887dc1f2591f8` | yes |
| `libshadowhook.so` | `e222299e4110fad03c4612c369d15c94700549bf3dd637c6a1fcd37d00395980` | yes |

All three remain stripped. So the archive yields **no additional source**; it raises confidence
and finalizes runtime semantics rather than exposing new code.

---

## Impact on reverse-engineering coverage

| Layer | Before | After this capture |
|-------|-------:|-------------------:|
| Binder protocol | ~90% | **~100%** (all reply codes + modes + TX13 semantics verified live) |
| libvc hook targets | ~50% (single device) | **~85%** (symbols + keys re-verified cross-device; only in-trampoline pixel code remains) |
| vcplax media pipeline | ~85% | ~85% (unchanged — still stripped) |

Overall, the runtime archive **validates** the existing reconstruction/clone rather than
extending the decompiled source.
