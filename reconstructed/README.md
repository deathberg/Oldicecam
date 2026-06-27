# Reconstructed source — `testicecam2.apk`

Maximal source recovery for the shipped APK. Two layers are provided:

| Path | What it is | Fidelity |
|------|------------|----------|
| [`decompiled-src/`](decompiled-src) | **Faithful** jadx 1.5.1 decompilation of the *app-specific* packages (R8-renamed but behavior-exact) | High — this is the actual recovered bytecode-to-Java |
| [`resources/`](resources) | **Exact** apktool 2.11.1 decode of the app manifest, layouts, navigation, menu, and app strings | Exact |
| [`aidl/`](aidl) | **Reconstructed** Binder contract `com.xiaomi.vlive.IMyBinderService` (codes exact, names inferred) | Contract exact, names inferred |
| [`original-apk/`](original-apk) | Earlier **clean-room** readable interpretation (semantic names) | Reference only |

Full analysis: [`../docs/SOURCE_RECONSTRUCTION_REPORT.md`](../docs/SOURCE_RECONSTRUCTION_REPORT.md).

## App identity

- Application id: `com.potplayer.music` — launcher label `新相机(1926内核)` ("New Camera (1926 kernel)").
- App code package: `com.xiaomi.vlive` (differs from the application id).
- License module: `xyz.vcxm.vmxplay` (gate is **disabled** in this "offline" build).
- It is a **rooted virtual-camera injection tool**: deploys native `vcplax`/`libvc`/`libshadowhook`
  to `/data`, runs `vcplax` as root, and drives it over a randomly-named Binder service to replace the
  device camera feed with a user video.

## Recovered app packages (`decompiled-src/`)

```
com/xiaomi/vlive/            App, MainActivity, FloatService, MediaProjectionForegroundService,
                            p057ui/{home,controller,settings}/*Fragment
com/potplayer/music/R.java
com/bytedance/shadowhook/ShadowHook.java        (JNI to libshadowhook.so)
com/kusu/loadingbutton/LoadingButton.java       (3rd-party widget)
xyz/vcxm/vmxplay/           ActivationManager, LicenseClient, DeviceFingerprint, Sntp,
                            tls/PkixAtDateTrust, patch/PreviewPatcher, ...
_app_logic_merged/          R8-merged app logic recovered from obfuscated packages:
  U/AbstractC0330t.java       root-shell exec("su") + ServiceManager Binder bridge + proxy wrappers
  d1/*.java                   Binder proxy (C1391f), interface/stub, death recipient, floating panel
```

> `_app_logic_merged/` holds classes R8 renamed into single-letter packages (`p037U`, `p059d1`). They
> are kept under readable folder names (`U`, `d1`) but the package declarations inside are unchanged,
> so they are documentation, not a drop-in build tree.

## Regenerating the full dump

The complete decompilation (all ~4,338 files incl. AndroidX/Kotlin/OkHttp) is **not** committed (size +
third-party). Regenerate locally:

```bash
bash tools/setup-re-env.sh                 # downloads jadx/apktool, extracts the APK
tools/bin/jadx -d decompiled/jadx --deobf --show-bad-code testicecam2.apk
java -jar tools/apktool.jar d testicecam2.apk -o decompiled/apktool -f
```
