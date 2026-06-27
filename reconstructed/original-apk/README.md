# Reconstructed source — testicecam2.apk (com.xiaomi.vlive)

Clean-room readable reconstruction of the **app-specific** code from `testicecam2.apk`.

This is **not** the exact original source (R8/ProGuard, synthetic lambdas, merged DEX). It preserves behavior, Binder contracts, preferences, and UI flow documented in `docs/APK_FULL_REVERSE_ENGINEERING.md`.

## Layout

```text
src/main/java/com/xiaomi/vlive/
  App.java
  MainActivity.java
  FloatService.java
  MediaProjectionForegroundService.java
  binder/MyBinderClient.java
  config/AppConfigKeys.java
  config/ActionRanges.java
  float/FloatingControlPanel.java
  util/RootShell.java
  util/VliveBridge.java
  ui/home/HomeFragment.java
  ui/controller/ControllerFragment.java
  ui/settings/SettingsFragment.java
```

Third-party / license code lives in original DEX as `xyz.vcxm.vmxplay.*` (see report).

## Build note

This tree is for **reference**. The buildable Gradle project is `app/` (IceCam Core) which implements the same backend with a modernized UI/runtime.
