# Max reconstruction report

## What changed from the previous scaffold

The previous APK was only a minimal clean-room Binder test UI. This version is rebuilt around the actual APK/native layout:

- original ABI split is preserved: `arm64-v8a` and `armeabi-v7a`;
- original native filenames are packaged in `jniLibs`;
- root bootstrap mirrors the recovered deployment path;
- `/data/camera` path is used because it appears in the original DEX strings;
- compatibility copies are also written to `/data/vcplax`, `/data/libvc.so`, and `/data/libvc++.so`;
- UI now exposes recovered settings/transactions rather than four generic buttons.

## Recovered native/service model

```text
Android Activity
  -> su bootstrap
  -> copy native libs/executable
  -> start /data/vcplax
  -> Binder: com.xiaomi.vlive.IMyBinderService
  -> TX 11/12/13/14/15/16/17/18/19/22/24/25
  -> MediaCodec / GraphicBuffer / ShadowHook / FFmpeg stack
```

## Current expected behavior

- APK builds on GitHub Actions.
- App asks for root on first launch.
- Native files are copied to root paths.
- `vcplax` is started if executable is compatible with the device/ROM.
- Binder status returns non-null only if `vcplax` successfully registers the service.

## If service stays disconnected

Check app log panel and `/data/camera/vcplax.log`. The most likely causes are:

- binary expects Xiaomi-specific framework/library behavior;
- service manager denies third-party root Binder service registration;
- SELinux blocks Binder registration or file execution;
- ABI mismatch;
- missing vendor dependencies on the target ROM.

## Reconstruction level after full APK integration

| Area | Estimate |
|---|---:|
| Java/root bootstrap behavior | 90-95% |
| Native file deployment | 90-95% |
| Binder descriptor and transaction surface | 85-95% |
| UI/settings surface | 75-85% |
| Native internal media implementation | 70-82% |
| Functional Android-native architecture | 85-92% |
| Exact original source code | impossible |
