# vcamera — working clone of the testicecam2 control app

A clean-room, **buildable** reimplementation of the control side of `testicecam2.apk`
(`com.potplayer.music` / app code `com.xiaomi.vlive`) — the rooted virtual-camera
("fake camera") tool reverse-engineered in this repo.

This module reproduces the **app**: UI, floating control overlay, root payload
deployment, and the exact Binder protocol used to drive the native daemon. It is a
framework-only Android app (no AndroidX), so it builds with the same toolchain as
`:retool`.

## Build

```bash
gradle :vcamera:assembleDebug        # -> vcamera/build/outputs/apk/debug/vcamera-debug.apk
gradle :vcamera:lintDebug
```

The NDK (`27.2.12479018`) builds the bundled native injection library
`libvc_clone.so` (arm64-v8a + armeabi-v7a) from the shared sources in
[`../clone`](../clone) via `src/main/cpp/CMakeLists.txt`.

## What it implements (fully working app side)

| Piece | File | Notes |
|-------|------|-------|
| Binder proxy (wire-compatible) | `binder/VcBinderClient.java` | hand-written Parcel proxy, **pinned** tx codes 11–25 |
| AIDL contract (doc/codegen) | `aidl/com/xiaomi/vlive/IMyBinderService.aidl` | not used for IPC (AIDL renumbers codes) |
| Service discovery + root | `util/VliveBridge.java`, `util/RootShell.java` | `ServiceManager.getService(randomName)`, `setenforce`, `su` |
| App lifecycle + deploy | `App.java` | extracts native libs → `/data`, runs `/data/vcplax <name> &` |
| Floating control panel | `FloatService.java` + `res/layout/float_layout.xml` | 4×4 grid 眼/↑/嘴/←/正/→/播/↓/循/转/翻/关 + 1/2/3/替 |
| Tabs: Home/Controller/Settings | `MainActivity.java` | video select, play/loop/mirror/rotate, action-range editor |

## Scope / what still needs a device

The **native daemon `vcplax`** (FFmpeg demux + `AMediaCodec` decode + GraphicBuffer
hooks via ShadowHook) is the camera-injection engine. Its Binder-server skeleton lives
in [`../clone/native/libvc_clone.cpp`](../clone/native/libvc_clone.cpp), but a runnable
executable links AOSP `libbinder`/`libutils` (not in the public NDK) and requires an
AOSP/on-device toolchain plus **root** to run. To get an end-to-end functional clone:

1. Cross-compile the daemon in an AOSP tree (see `../docs/CLONE_ROADMAP.md`).
2. Drop `vcplax`, `libvc_clone.so`, `libshadowhook.so` into
   `vcamera/src/main/jniLibs/<abi>/` as `vcplax.so`, `libvc_clone.so`, `libshadowhook.so`.
3. Install on a rooted device; `App` deploys them to `/data` and launches the daemon,
   then the UI/overlay drives it over the pinned Binder protocol.

Without root/device, the app installs and runs; the daemon deploy step is a logged
no-op and the Binder calls fail gracefully.
