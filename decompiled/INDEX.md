# Decompiled artifact index

Generated locally from `testicecam2.apk`. Not committed in full (size + third-party libs).

## Paths

| Path | Tool | Contents |
|---|---|---|
| `decompiled/jadx/sources/` | jadx 1.5.1 | 3480 Java files |
| `decompiled/apktool/` | apktool 2.11.1 | smali, decoded XML, native libs |
| `decompiled/raw/` | unzip | binary APK extract |

## App-specific entry points (jadx)

```text
com/xiaomi/vlive/App.java
com/xiaomi/vlive/MainActivity.java
com/xiaomi/vlive/FloatService.java
com/xiaomi/vlive/MediaProjectionForegroundService.java
com/xiaomi/vlive/ui/home/HomeFragment.java
com/xiaomi/vlive/ui/controller/ControllerFragment.java
com/xiaomi/vlive/ui/settings/SettingsFragment.java
p037U/AbstractC0330t.java          — RootShell + VliveBridge
p059d1/C1391f.java                  — Binder proxy
p059d1/C1390e.java                  — Float panel
p065f1/C1439a.java, C1442d.java      — Controller listeners
xyz/vcxm/vmxplay/                   — License + PreviewPatcher (classes5.dex)
```

## Regenerate

```bash
tools/bin/jadx -d decompiled/jadx --show-bad-code --deobf testicecam2.apk
java -jar tools/apktool.jar d testicecam2.apk -o decompiled/apktool -f
unzip -o testicecam2.apk -d decompiled/raw
```

See `docs/APK_FULL_REVERSE_ENGINEERING.md` for the full analysis.
