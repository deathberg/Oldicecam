# IceCam RE v27 — unified reconstruction

## Root cause fix (v26 bug)

**TX24 is color injection (三色), NOT pan/zoom.** Sending pan/zoom via TX24 made the stream darker/lighter.

| Control | Correct path |
|---------|----------------|
| Pan / zoom / fit (photo) | Bake JPEG → **TX14 → TX11** (debounced) |
| Rotate | **TX18** angle (0/90/180/270) |
| Mirror | **TX19** |
| Loop | **TX17** |
| Auto-rotate | **TX16** |
| Color spot inject | **TX24** only (disabled by default) |

## Bundled natives (from mttrq8 RE pull)

```
app/src/main/jniLibs/arm64-v8a/{libvc.so, libshadowhook.so, vcplax.so}
```

Verified identical to runtime pull / original APK.

## UI (kept from v26)

- Design unchanged
- 4 slot previews: **one row, ~68dp height**
- Float: stream D-pad + M1–M4 + colored START/STOP
- SmartLogger built into app (not separate RE tool)

## Build

```bash
gradle :app:assembleDebug
```
