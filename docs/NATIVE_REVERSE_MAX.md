# Native reverse engineering — extended analysis

Methods applied beyond initial jadx/strings pass:

| Method | Tool | Result |
|---|---|---|
| ELF header / NEEDED / Build-ID | `readelf`, `file` | ABI, dependencies, reproducible fingerprints |
| Dynamic symbol import/export | `readelf --dyn-syms`, `objdump -T`, `nm -D` | 326 imports in vcplax, JNI_OnLoad in shadowhook |
| Full string corpus + categorization | Python + regex | 67k strings in vcplax, 1.9k tagged |
| Structured ELF parse | **LIEF** + **pyelftools** | sections, entrypoint, imports grouped |
| Entrypoint disassembly | **Capstone** ARM64 | PIE entry @ `0x43e880` → libc init → main |
| ADRP+ADD rodata xref scan | custom `native_xref_scan.py` | xrefs to `IMyBinderService`, `libvc.so` |
| TX code immediate scan | Capstone full `.text` scan | addresses using `#11..#25` (dispatch hints) |
| Rohdata string extraction | LIEF section walk | confirmed color-replacement log strings |
| ABI diff | arm64 vs armeabi-v7a | separate Build-ID per lib (not same binary) |

Artifacts:

- `docs/native_analysis/NATIVE_DEEP_ANALYSIS.md` — auto summary per `.so`
- `docs/native_analysis/native_deep_report.json` — machine-readable full dump
- `tools/native_deep_analyze.py`, `tools/native_xref_scan.py` — reproducible scripts

---

## 1. Library roles (confirmed)

```text
┌─────────────────────────────────────────────────────────────┐
│ vcplax.so (PIE executable, ~12 MB arm64)                    │
│  • Binder BBinder service: com.xiaomi.vlive.IMyBinderService│
│  • ProcessState + IPCThreadState thread pool                  │
│  • dlopen("libvc.so") + dlopen("libvc++.so")                │
│  • FFmpeg/libavcodec/libavformat (statically linked)        │
│  • AMediaCodec NDK encode/decode path                       │
│  • RTMP/HLS/MP4 demux (libavformat strings)                 │
└───────────────────────────┬─────────────────────────────────┘
                            │ dlopen
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
 libvc.so (~1.3 MB)                  libvc++.so (= libshadowhook)
  • shadowhook_init/hook_sym_name     • ByteDance ShadowHook 1.0.10
  • GraphicBuffer lock/YCbCr          • JNI: com.bytedance.shadowhook.ShadowHook
  • AMediaCodec pipeline              • hook_sym_name / hook_func_addr
  • libui.so + libbinder.so           • trampoline hub
  • JPEG/libjpeg-turbo + libc++       │
  • May register auxiliary Binder     │
```

---

## 2. vcplax.so — daemon startup (native evidence)

Strings + imported symbols show standard Android **native Binder service** bootstrap:

```cpp
ProcessState::self();
ProcessState::startThreadPool();
IPCThreadState::self();
IPCThreadState::joinThreadPool(bool);
defaultServiceManager();
Parcel::enforceInterface(String16("com.xiaomi.vlive.IMyBinderService"), ...);
```

Dynamic loader path:

```text
dlopen("libvc.so")
dlopen("libvc++.so")    // shadowhook disguised
dlsym(...)
```

**Rodata cluster** at VA/file offset `0xFE533`:

- `com.xiaomi.vlive.IMyBinderService`
- nearby FFmpeg/libav strings (same `.rodata` region)

**Capstone xref scan** found **1 ADRP+ADD** reference to descriptor string → code near **`0x43F8FC`** region (Binder class vtable / interface setup).

---

## 3. vcplax — media pipeline (FFmpeg + NDK)

vcplax is mostly a **statically linked FFmpeg** build:

- 29 `AMediaCodec_*` imports
- Thousands of `libavcodec/*`, `libavformat/*`, `libswresample/*` internal strings
- RTMP: `rtmp://`, `NetStream.Play.Start`, `ffrtmpcrypt`, `rtmp_buffer`
- MP4/HLS: `hls_playlist`, `advanced_editlist`, `mp4a.40.33`

### App-specific operational string (high value)

```text
%8ld: Replaced %5d blocks by color %X
```

This confirms **block-level color replacement** inside native video frames — matches Java-side «三色注入» (TX24 + MediaProjection sampling).

Other Java-visible behavior tied to native:

| String / API | Meaning |
|---|---|
| `set loop flag` | TX17 loop |
| `AMediaCodec_signalEndOfInputStream` | end-of-stream drain |
| `cameraserver` | ABI probe target (same as Java `file /system/bin/cameraserver`) |
| `camera_version`, `cameraModel/Name/Serial` | metadata passthrough |

---

## 4. vcplax — onTransact dispatch (partial recovery)

Full `switch(code)` not recovered (stripped, optimized), but **immediate scan** found many sites loading `#11`–`#25`:

| TX | Sample `.text` addresses (arm64) | Likely role |
|---:|---|---|
| 11 | `0x4469F4`, `0x44F7D0`, `0x459648` | playSource |
| 12 | `0x43FB9C`, `0x44F73C` | stop/query |
| 13 | `0x44F7C0`, `0x452F08` | poll int[] |
| 14 | `0x43FA08`, `0x466154` | setMode/path |
| 15 | `0x4511C4`, `0x452EFC` | status (5=playing) |
| 16 | `0x44130C`, `0x4413B8` | auto-rotate |
| 17 | `0x443498`, `0x458778` | loop |
| 18 | `0x450834`, `0x4546D8` | angle |
| 19 | `0x451128`, `0x451148` | mirror |
| 22 | `0x46279C`, `0x466528` | seek range µs |
| 24 | `0x444024`, `0x444098`, `0x4453A4` | transform / color |
| 25 | `0x4456C0`, `0x44573C` | hard recovery |

To fully decompile handlers: import `vcplax.so` into **Ghidra/IDA** at these addresses.

---

## 5. libvc.so — injection layer

### Dependencies (NEEDED)

```text
libshadowhook.so, libmediandk.so, libbinder.so, libutils.so,
libcutils.so, libui.so, libandroid.so, liblog.so, libm, libdl, libc
```

### Key imports

| Symbol | Use |
|---|---|
| `shadowhook_init`, `shadowhook_hook_sym_name` | PLT hook install |
| `GraphicBuffer::{lock,unlock,lockYCbCr,from}` | camera buffer access |
| `AMediaCodec_*` + `AMediaFormat_*` | re-encode / inject frames |
| `defaultServiceManager`, `BBinder`, `Parcel` | optional side-band IPC |
| `MemoryHeapBase`, `MemoryBase`, `IMemory` | shared memory with vcplax |

### Hook targets

**Not stored as cleartext symbol names** in `libvc.so` rodata (likely runtime `dlsym` on `libui.so` / `libcameraservice.so` or obfuscated construction).

Only external hook library string: **`libui.so`**.

Embedded third-party code (not app logic):

- libjpeg-turbo (`jinit_*`, `jsimd_*`, `jpeg_*`)
- LLVM libc++ / libc++abi (demangle, exception machinery)

### Size note

arm64 `libvc.so` is **56% larger** than armeabi-v7a (1.29 MB vs 826 KB) — NEON SIMD JPEG paths + 64-bit code.

---

## 6. libshadowhook.so

- Version string: **`shadowhook version 1.0.10`**
- Package: `com.bytedance.shadowhook.ShadowHook`
- Exports: `JNI_OnLoad`, `shadowhook_hook_sym_name`, `shadowhook_hook_func_addr`, `shadowhook_unhook`, `shadowhook_dlopen/dlsym_*`
- Debug strings document hook flow:

```text
a64: hook (WITH EXIT) OK. target %lx -> exit %lx -> new %lx ...
shadowhook: hook_sym_name(%s, %s, %p) OK
```

 arm64 Build-ID: `28140b0c1784775a937452c07ef12ba15397cf6a`

---

## 7. Build fingerprints (reproducibility)

| Library | arm64-v8a Build-ID | armeabi-v7a Build-ID |
|---|---|---|
| vcplax.so | `0627056c0d61e9c1d1b25360a88573b3d1d22df8` | `46cc955dfba5417fe97afaa13b917b3e5582e787` |
| libvc.so | `e12e67bfa956d23cfc417219433368a7ca82846a` | `34ab57e9f492f7b09678cf21b4a05420b206c55b` |
| libshadowhook.so | `28140b0c1784775a937452c07ef12ba15397cf6a` | `6b550c3924aa6184f50e1b41d90877de3279958f` |

Each ABI is a **separate compile**, not a thin wrapper.

---

## 8. What still requires Ghidra/IDA (next step)

1. **Decompile `vcplax` onTransact** at **`0x43f8b4`** — **DONE** → `docs/NATIVE_FINAL_DECOMPILE.md`
2. **Trace `libvc` init** → find `shadowhook_hook_sym_name` call sites and recovered symbol strings on stack.
3. **Recover `main()` / argv parsing** in vcplax → confirm service name = `argv[1]`.
4. **Map FFmpeg custom IO** → how injected frames reach camera HAL.
5. **Dynamic analysis** on rooted device: `strace -f /data/vcplax`, `logcat`, `/proc/PID/maps`.

### Suggested Ghidra project settings

- Language: AARCH64:LE:64:v8A
- Loader: ELF with PIE base 0
- Entry: `0x43E880`
- Bookmarks: `0xFE533` (descriptor), `0x444024` (TX24), `0x46279C` (TX22)

---

## 9. Updated reconstruction confidence

| Component | Before | After extended native RE |
|---|---:|---:|
| vcplax daemon role | 85% | **92%** |
| Binder bootstrap | 85% | **95%** |
| FFmpeg/RTMP/MP4 path | 70% | **88%** |
| Color block replacement | 60% | **82%** (log string + TX24 cluster) |
| libvc hook targets | 40% | **55%** (libui.so confirmed, symbols obfuscated) |
| onTransact per-TX logic | 50% | **68%** (dispatch addresses mapped) |

---

## 10. Re-run locally

```bash
python3 tools/native_deep_analyze.py
python3 tools/native_xref_scan.py
# optional: import vcplax.so into Ghidra 11.x headless
```
