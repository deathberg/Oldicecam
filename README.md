# IceCam Core v26

Android reconstruction of the virtual camera app from `testicecam2.apk` (original package `com.xiaomi.vlive.*`, manifest id `com.potplayer.music`).

**Current build:** `0.26-v26-re-capture` (versionCode 26)

## Project layout

```text
app/                          # Single Gradle module — all runtime code lives here
  src/main/java/dev/icecam/app/
    MainActivity.java           # Main launcher UI
    FloatService.java           # Floating controls + face-action TX22 buttons
    VliveBinderClient.java      # Binder IPC to native vcplax daemon
    runtime/                    # v25 state machine (CommandBus, Reducer, SideEffects)
    recapture/                  # On-device RE log capture (opened from MainActivity)
    config/ActionRanges.java    # Face-action seek ranges (microseconds)
  src/main/jniLibs/             # Native libs from original APK (libvc, shadowhook, vcplax)
  src/main/assets/re/           # Frida hook script for RE Capture

docs/                         # Active RE reports and architecture notes
  V25_RUNTIME_ARCHITECTURE.md   # Current runtime design
  APK_FULL_REVERSE_ENGINEERING.md
  NATIVE_*.md
  archive/iterations/         # Historical version notes (V4–V23)

tools/                        # RE automation scripts (jadx, Ghidra, Frida, Termux)
decompiled/                   # Local decompile output index (artifacts gitignored)
re-workspace/                 # Local Ghidra/r2 workspace index
```

## Build locally

Requirements: JDK 17, Android SDK.

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

CI uses the same Gradle version (8.11.1) via GitHub Actions.

## v26 highlights

- **Single app core** in `app/` — no parallel reference trees or experimental clones.
- **Runtime v25 pipeline:** Command → Reducer → AppState → Persistence → SideEffectRunner.
- **TransformController** owns transform commands from MainActivity and FloatService.
- **Face actions (TX22):** EYE, HEAD, MOUTH, L, CTR, R, NOD in FloatService via `VliveBinderClient.setRange`.
- **MediaProjectionForegroundService** with proper foreground notification channel.
- **RE Capture** available from MainActivity (not a separate launcher icon).

## Backend model

```text
MainActivity / FloatService
  → su bootstrap (RootBootstrap)
  → native deploy (libvc.so, libshadowhook.so, vcplax)
  → Binder: com.xiaomi.vlive.IMyBinderService
  → TX 11/12/13/14/15/16/17/18/19/22/24/25
```

Legacy apply path: `TX14 → TX11`. Geometry preview is client-side; `TX24` is optional for native transform. `TX25` is reserved for hard recovery.

## Reverse engineering references

- Full APK report: [docs/APK_FULL_REVERSE_ENGINEERING.md](docs/APK_FULL_REVERSE_ENGINEERING.md)
- Extended native RE: [docs/NATIVE_REVERSE_MAX.md](docs/NATIVE_REVERSE_MAX.md)
- Runtime architecture: [docs/V25_RUNTIME_ARCHITECTURE.md](docs/V25_RUNTIME_ARCHITECTURE.md)
- RE workspace setup: [re-workspace/README.md](re-workspace/README.md)
- Native analysis: [docs/native_analysis/](docs/native_analysis/)
- Decompile index: [decompiled/INDEX.md](decompiled/INDEX.md)
- Iteration history: [docs/archive/iterations/](docs/archive/iterations/)
