# Source Reconstruction Report — `testicecam2.apk`

> Branch: `cursor/re-testicecam2-e3a1`
> Generated from full decompilation (jadx 1.5.1 + apktool 2.11.1) of the shipped APK.
> Recovered sources live in [`reconstructed/decompiled-src/`](../reconstructed/decompiled-src) and decoded
> resources in [`reconstructed/resources/`](../reconstructed/resources). The binder contract is
> reconstructed in [`reconstructed/aidl/com/xiaomi/vlive/IMyBinderService.aidl`](../reconstructed/aidl/com/xiaomi/vlive/IMyBinderService.aidl).

---

## 1. What the app is

| Field | Value |
|-------|-------|
| Package id | `com.potplayer.music` |
| Launcher label | `新相机(1926内核)` ("New Camera (1926 kernel)") |
| `app_name` resource | `离线版本` ("Offline version") |
| versionName / versionCode | `19.26` / `1` (manifest) — UI shows `19.26 兼容模式` ("compatibility mode") |
| minSdk / targetSdk | 29 / 35 |
| Main package of app code | `com.xiaomi.vlive` (note: differs from the application id) |
| License module | `xyz.vcxm.vmxplay` |
| Native payload | `lib/<abi>/vcplax.so`, `libvc.so`, `libshadowhook.so` |

**Function.** It is a **rooted virtual-camera ("fake camera") injection tool**. It ships a native
ELF daemon (`vcplax`) plus a hooking library (`libvc.so` + ByteDance `libshadowhook.so`) that are
copied to `/data` and launched as root. The daemon registers an Android **Binder** service under a
randomized name and hooks the system camera pipeline so that a user-supplied video file (e.g.
`/sdcard/Movies/1.mp4`) is fed into other apps' camera preview/capture in place of the real sensor.
The Java app is just the **control panel**: it deploys the native payload, talks to it over Binder,
and exposes a UI + floating overlay to drive playback (ranges, angle, mirror, loop, auto-color, video
selection).

This is a **cracked/"offline" build**: the `xyz.vcxm.vmxplay.ActivationManager` license gate is
neutered (`__ACTIVATED = true`, `startJsonCheckFlow()` is empty), so the online activation against
`vcxm.liuzhou.shop` is bypassed.

---

## 2. Reconstruction method & fidelity

| Layer | Tool | Output | Fidelity |
|-------|------|--------|----------|
| Java (DEX → .java) | jadx 1.5.1 (`--deobf --show-bad-code`) | 3,480 classes, 4,338 `.java` files | High for app code; library code obfuscated (R8) |
| Resources / manifest | apktool 2.11.1 | `AndroidManifest.xml`, `res/`, `resources.arsc` | Exact |
| Native daemon/hook | strings/nm + existing `docs/native_analysis/` | symbol & string maps | Partial (no full C source) |

The app was processed with **R8 / ProGuard** (renaming + horizontal class merging) over 5 DEX files
(~12 MB). Consequences for the recovery:

- App classes that kept real names: everything under `com.xiaomi.vlive.*`, `com.potplayer.music.R`,
  `com.bytedance.shadowhook.ShadowHook`, `com.kusu.loadingbutton.LoadingButton`, and the whole
  `xyz.vcxm.vmxplay.*` module (shipped in the least-obfuscated `classes5.dex`, with full method names
  and string literals intact).
- App **logic** that got renamed/merged into single-letter packages. The two important ones were
  recovered and are kept under `reconstructed/decompiled-src/_app_logic_merged/`:
  - `U/AbstractC0330t.java` — R8 merged the app's **root-shell + Binder bridge** (`exec("su")`,
    `ServiceManager.getService`, all the proxy call wrappers) into one giant class together with
    unrelated AndroidX/Material/Kotlin helpers. The app-relevant static methods are documented in §5.
  - `d1/*` — the **Binder proxy** (`C1391f`), interface (`InterfaceC1393h`), stub base
    (`AbstractBinderC1392g`), death recipient (`C1394i`), and the **floating panel** glue
    (`C1390e`, `ViewOnClickListenerC1387b`, `ViewOnTouchListenerC1388c`).

jadx reported 29 method-level decompile failures out of 3,480 classes (~0.8%), none in app-specific
code.

---

## 3. Recovered file map

```
reconstructed/
  decompiled-src/
    com/xiaomi/vlive/
      App.java                         # Application: deploys native libs, runs /data/vcplax, floating panel
      MainActivity.java                # BottomNav + NavController (home/controller/settings)
      FloatService.java                # Foreground service hosting the floating control overlay
      MediaProjectionForegroundService.java
      p057ui/home/HomeFragment.java        # version/notice text; warns about rival fake-cams in /data
      p057ui/controller/ControllerFragment.java  # main control UI (Camera2 + MediaProjection capture)
      p057ui/settings/SettingsFragment.java      # action-range editor + monitor XY picker + color type
    com/potplayer/music/R.java
    com/bytedance/shadowhook/ShadowHook.java     # JNI bindings to libshadowhook.so
    com/kusu/loadingbutton/LoadingButton.java    # 3rd-party UI widget
    xyz/vcxm/vmxplay/
      ActivationManager.java           # license gate (neutered in this build)
      LicenseClient.java               # POST to vcxm.liuzhou.shop activate.php (+ NTP-pinned TLS)
      DeviceFingerprint.java           # SHA-256(Build.*|android_id) -> base32, 26 chars
      Sntp.java / tls/PkixAtDateTrust.java / ActivationException.java / C2096R.java
      patch/PreviewPatcher.java        # hijacks host "camera_preview" button to show real Camera2 preview
    _app_logic_merged/
      U/AbstractC0330t.java            # root shell `q()` + Binder bridge `E()` + proxy wrappers
      d1/{C1391f,InterfaceC1393h,AbstractBinderC1392g,C1394i,C1390e,ViewOnClickListenerC1387b,ViewOnTouchListenerC1388c}.java
  resources/
    AndroidManifest.xml
    apktool.yml
    res/layout/{activity_main,float_layout,fragment_home,fragment_settings,fragment_controller,custom_dialog}.xml
    res/navigation/mobile_navigation.xml
    res/menu/bottom_nav_menu.xml
    res/values/strings-app.xml
  aidl/com/xiaomi/vlive/IMyBinderService.aidl   # reconstructed Binder contract
```

---

## 4. Runtime architecture

```
                 ┌──────────────────────────────────────────────┐
                 │  com.xiaomi.vlive.App (Application.onCreate)   │
                 │  1. killall vcplax                             │
                 │  2. unzip lib/<abi>/*.so -> filesDir           │
                 │  3. su: cp libvc.so      -> /data/libvc.so     │
                 │         cp libshadowhook -> /data/libvc++.so   │
                 │         cp vcplax.so     -> /data/vcplax       │
                 │         chmod 700 /data/vcplax                 │
                 │  4. su: /data/vcplax <ServerName> &            │
                 └───────────────┬──────────────────────────────┘
                                 │ launches (root)
                                 ▼
   ┌───────────────────────────────────────────────────────────────┐
   │ /data/vcplax  (native, decompiled from vcplax.so)               │
   │  • android::defaultServiceManager()->addService(<ServerName>)   │
   │  • implements onTransact for "com.xiaomi.vlive.IMyBinderService"│
   │  • decodes user video via AMediaCodec / bundled FFmpeg (h264..) │
   │  • injects frames into camera pipeline using libvc + shadowhook │
   └───────────────┬───────────────────────────────────────────────┘
                   ▲ IBinder.transact(code, Parcel)
                   │  (token "com.xiaomi.vlive.IMyBinderService")
   ┌───────────────┴───────────────────────────────────────────────┐
   │ U.AbstractC0330t.E()  → ServiceManager.getService(ServerName)  │
   │ d1.C1391f (proxy)     → setPlayRange / setMirror / selectVideo │
   │ UI: HomeFragment · ControllerFragment · SettingsFragment       │
   │ Overlay: FloatService / App floating GridLayout (眼↑嘴←正→...) │
   └────────────────────────────────────────────────────────────────┘
```

Key detail: the service name is **randomized** (`App.getServerName()` /`m1779d`), picking a real
`ServiceManager.listServices()` entry name + random suffix and caching it in the `ServerName` pref.
This is anti-detection: the fake-camera service masquerades under a plausible system service name.
`AbstractC0330t.E()` also toggles SELinux (`setenforce 0/1`) around the lookup.

---

## 5. Binder protocol (`com.xiaomi.vlive.IMyBinderService`)

Recovered from the proxy `d1/C1391f` (each method writes the interface token then `transact`s). The
native daemon is the server; the APK has **no Stub** (server is native code).

| tx code | proxy method | reconstructed signature | call site / meaning |
|--------:|--------------|--------------------------|---------------------|
| 11 | `m1797j` | `setVideoSource(String path, boolean loop, boolean flag)` | — |
| 12 | `m1798k` | `command12()` | — |
| 13 | `m1790c` | `int[] getStatusArray()` | — |
| 14 | `m1796i` | `selectVideo(int index, String path)` → `==4` ok | `a0()` ← play `/sdcard/Movies/N.mp4` |
| 15 | `m1793f` | `getPlayState()` → `==5` playing | `T()` ← "播放" play button |
| 16 | `m1789b` | `setFlagB(boolean)` | `i()` |
| 17 | `m1788a` | `setFlagA(boolean)` | `e()` ← loop toggle (循) |
| 18 | `m1794g` | `setPlayAngle(int deg)` | `d0()` ← rotate (转), 0/90/180/270/360 |
| 19 | `m1791d` | `setMirror(boolean)` | `c0()` ← flip (翻), pref `PlayMirror` |
| 22 | `m1792e` | `setPlayRange(long beginUs, long endUs)` | `R()` ← action-range buttons |
| 24 | `m1795h` | `setAutoColor(int on, float x, float y, float intensity, float diameter, int mode)` → `==14` ok | `Y()` ← color settings |
| 25 | `m1799l` | `replaceCommand()` | "替" button (f34) |

Transaction codes are `FIRST_CALL_TRANSACTION + n`; gaps (20,21,23) exist in the proxy and were not
referenced by the recovered Java.

---

## 6. Configuration model (`SharedPreferences "app_config"`)

| Key | Type | Default | Meaning |
|-----|------|---------|---------|
| `ServerName` | String | random | cached masquerade name of the Binder service |
| `ActionRangebgin{1,2,3,4,5,6,8}` | long | see below | play-range **begin** (µs) per preset |
| `ActionRangeEnd{1,2,3,4,5,6,8}` | long | see below | play-range **end** (µs) per preset |
| `PlayAngle` | int | 0 | rotation, stepped +90 (wraps after 360) |
| `PlayMirror` | boolean | false | horizontal mirror |
| `PlayisLoop` | boolean | false | loop playback |
| `MonitorTargetX` / `MonitorTargetY` | int | 55 / 380 | screen coords picked in Settings ("选择坐标") |
| `PlayAutoColor_mode` | int | 1 | color/skin mode (radio group 1/2/3) |
| `AutoColor_X/Y/intensity/diameter` | float | 50/50/0.3/0.6 | auto-color region |

Default action ranges (begin → end, µs), from `App`/`FloatService`/`SettingsFragment`:

| preset | label | begin | end |
|-------:|-------|------:|----:|
| 1 | 眼 (eye)   | 0 | 1,170,000 |
| 2 | ↑ (up)     | 5,000,000 | 5,900,000 |
| 3 | 嘴 (mouth) | 2,000,000 | 3,200,000 |
| 4 | ← (left)   | 3,200,000 | 4,000,000 |
| 5 | 正 (front) | 4,000,000 | 4,000,000 |
| 6 | → (right)  | 4,000,000 | 5,000,000 |
| 8 | ↓ (down)   | 5,600,000 | 6,800,000 |

---

## 7. Floating control panel (`float_layout.xml`)

4×4 `GridLayout`, toggled by tapping the app-icon `main_button`. Button → action:

| id | label | action |
|----|-------|--------|
| `butonf1` | 眼 | `setPlayRange` preset 1 |
| `butonf2` | ↑ | preset 2 |
| `butonf3` | 嘴 | preset 3 |
| `butonf4` | ← | preset 4 |
| `butonf5` | 正 | preset 5 |
| `butonf6` | → | preset 6 |
| `butonf7` | 播 | play (`getPlayState`==5 check) |
| `butonf8` | ↓ | preset 8 |
| `butonf9` | 循 | toggle loop (`setFlagA`) |
| `butonf10` | 转 | rotate +90 (`setPlayAngle`) |
| `butonf11` | 翻 | toggle mirror (`setMirror`) |
| `butonf12` | 关 | close overlay / `stopSelf` |
| `butonf31..34` | 1/2/3/替 | select `/sdcard/Movies/{1,2,3}.mp4` (`selectVideo`) / `replaceCommand` |

---

## 8. License / activation module (`xyz.vcxm.vmxplay`)

- `LicenseClient.activate()` → `POST https://vcxm.liuzhou.shop/lic/public/api/activate.php`
  with `{activation_code, device_fp}`; response `data[0]`=expiry, `data[1]`=server-now (epoch s).
- `ActivationManager` (the public entry) also references `https://vcxm.liuzhou.shop/vc.php` for a
  `check`/`activate` JSON flow with an in-app activation-code dialog.
- `DeviceFingerprint` = first 26 chars of base32(SHA-256(`BOARD|BRAND|DEVICE|MANUFACTURER|MODEL|android_id`)),
  cached in `filesDir/vmx_fp.dat`.
- `Sntp` queries `time*.windows.com`; `PkixAtDateTrust` builds an `SSLSocketFactory` that validates the
  server cert **as of NTP time** (handles devices with a wrong clock).
- **In this build the gate is disabled**: `ActivationManager.__ACTIVATED=true`,
  `__EXPIRE_TS=8400000192`, and `startJsonCheckFlow()` has an empty body (unreachable blocks removed by
  jadx) → no network license check is performed at runtime.

`PreviewPatcher.attachToPreviewButton()` (called from `MainActivity.onCreate`) reflectively chains the
host's existing `camera_preview` onClick and opens a **real** front-facing Camera2 preview into the
host `cameraTextureView`, mirrored — i.e. a "preview" affordance layered onto the target UI.

---

## 9. Native components (summary)

| File (in APK) | Deployed as | Role (from symbols/strings) |
|---------------|-------------|------------------------------|
| `vcplax.so` (~12 MB) | `/data/vcplax` (exec, chmod 700) | Binder server (`defaultServiceManager`), video decode via `AMediaCodec_*` + bundled **FFmpeg** (`ff_h264_init_poc`, `mpeg2_mediacodec`, `vp8_mediacodec`), frame injection |
| `libvc.so` | `/data/libvc.so` | camera-pipeline hook payload |
| `libshadowhook.so` | `/data/libvc++.so` | ByteDance **ShadowHook** PLT/inline hooking runtime (`com.bytedance.shadowhook.ShadowHook` JNI) |

Deeper native analysis (Binder transaction immediates, xrefs, function lists) is in
[`docs/native_analysis/`](native_analysis/) and the other RE reports in `docs/`. The clone/skeleton of
the injection lib is under [`clone/`](../clone).

---

## 10. Limitations & notes

- This is a **behavioral** reconstruction of obfuscated bytecode, not the original source tree. Field
  names like `f2586a` are R8-renamed; method names in `U/AbstractC0330t` and `d1/*` are jadx-synthetic
  (`mNNN`). The `IMyBinderService.aidl` method names are *reconstructed from call sites*, not original.
- Library packages (AndroidX, Material, Kotlin, OkHttp) are present in the full jadx dump but are
  intentionally **not** copied into `reconstructed/` (they are third-party and obfuscated).
- The full decompiled dump (all 4,338 files) can be regenerated locally with
  `bash tools/setup-re-env.sh` then jadx/apktool (outputs go to the git-ignored `decompiled/`).
- Legal/ethical: this app is spyware-adjacent (root camera spoofing, anti-detection service-name
  masquerading). This document is for defensive/interop reverse-engineering only.
