# AGENTS.md

## Cursor Cloud specific instructions

This branch (`cursor/re-testicecam2-e3a1`) is the **reverse-engineering + clone** workspace for
`testicecam2.apk`. There is no web/desktop UI to run. The product surfaces are:

| Component | What it is | Build / run | Notes |
|-----------|-----------|-------------|-------|
| `:retool` | Android RE Tool app (the CI target) | `gradle --no-daemon :retool:assembleDebug` → `retool/build/outputs/apk/debug/retool-debug.apk` | Lint: `gradle :retool:lintDebug`. No unit tests exist. |
| `clone/` | Native injection lib + `vcplax` clone (CMake) | see below | Only `libvc_clone` is host-buildable. |
| `tools/*.py` | Native RE analysis (LIEF/Capstone) | `bash tools/re-analyze.sh` | Requires extracted APK libs first (see below). |

### Environment (already provisioned in the VM snapshot)
- **JDK 17** at `/usr/lib/jvm/java-17-openjdk-amd64` (CI uses 17; AGP 8.9 needs 17). The VM also has
  JDK 21 — always build with `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.
- **Gradle 8.11.1** on `PATH` (no `gradlew` wrapper in this repo — use the system `gradle`).
- **Android SDK** at `/opt/android-sdk` (`platform-35`, `build-tools;35.0.0`, `platform-tools`,
  `ndk;27.2.12479018`, `cmake;3.22.1`). `ANDROID_HOME`/`ANDROID_SDK_ROOT` + `JAVA_HOME` are exported
  from `~/.bashrc`; `local.properties` (`sdk.dir=/opt/android-sdk`) is present (gitignored).
- Python RE deps (`lief`, `capstone`, `pyelftools`, `unicorn`) — refreshed by the update script.

### Gotchas
- **No emulator possible**: `/dev/kvm` is absent and the CPU has no virtualization extensions, so the
  `:retool` APK cannot be launched on an Android emulator here. Dev verification = build + lint (this
  matches CI, which only builds the debug APK).
- **`clone/` native build**: `cmake` must be invoked with the NDK toolchain, e.g.
  `cmake -S clone -B clone/build -DCMAKE_TOOLCHAIN_FILE=$ANDROID_HOME/ndk/27.2.12479018/build/cmake/android.toolchain.cmake -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-33 -DCMAKE_MAKE_PROGRAM=$ANDROID_HOME/cmake/3.22.1/bin/ninja -GNinja`,
  then `cmake --build clone/build --target libvc_clone`. The `vcplax_clone` target links AOSP
  `libbinder`/`libutils`, which are **not** in the public NDK — it only compiles inside an AOSP
  platform/on-device toolchain, so building the whole project (`cmake --build clone/build`) is
  expected to fail on that target. Build the `libvc_clone` target only.
- **Python RE tools** read native libs from `decompiled/raw/lib/` (gitignored). Extract them first:
  `unzip -o testicecam2.apk "lib/*" -d decompiled/raw`, then run `bash tools/re-analyze.sh`.
  Full RE bootstrap (jadx/apktool/Ghidra/r2 downloads) lives in `tools/setup-re-env.sh`.
