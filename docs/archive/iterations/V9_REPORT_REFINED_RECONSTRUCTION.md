# IceCam v9 — report-refined reconstruction

This build is based on all previous APK/native reports and runtime feedback from v8.

## Important correction from the latest report

The term `TX24` appears in two unrelated contexts:

1. **Binder TX24** — transaction code 24, used by the Java/Binder control layer as:
   `TX24(mode, panX, panY, zoomX, zoomY, flags)`.
2. **RGB24/TX24 pixel format** — color conversion / YUV→RGB24 path inside libjpeg/libvc-style native code.

v9 keeps using **Binder TX24** for transform control. It does not assume the RGB24 report section is the transform protocol.

## Native-flow decision

Native libraries are not modified. v9 keeps the working native flow from v8:

- native libs are extracted from the APK,
- copied to `/data` and `/data/camera`,
- `/data/vcplax <ServerName>` is launched through root,
- Binder control is performed through `com.xiaomi.vlive.IMyBinderService` token and recovered transaction codes.

## UI changes

- English-only UI.
- Removed multi-tab clutter.
- Single-page workflow:
  - status,
  - media selection,
  - TX24 transform controls,
  - floating menu,
  - service name / Binder diagnostics,
  - runtime logs.

## Floating menu remap

Original semantic layout is remapped as:

- Eye → Zoom +
- Face → Zoom -
- Mouth → Center
- Arrows → pan image
- Rotate → 90° rotation
- Mirror → horizontal mirror
- Crop → cycles crop presets
- Fit/Fill → cycles aspect strategy

## Transform model

`TX24(mode, panX, panY, zoomX, zoomY, flags)`

- `mode`: 0 FREE, 1 FIT, 2 FILL, 3 STRETCH, 4 NATIVE
- `panX/panY`: normalized movement, clamped -8..+8
- `zoomX/zoomY`: 0.05..32.0
- `flags`:
  - bit 0: mirror horizontal
  - bit 1: mirror vertical
  - bits 2..3: rotation quadrant
  - bit 5: auto-rotate
  - bit 6: lock aspect
  - bit 7 + bits 8..15: crop preset

## Diagnostics

v9 logs:

- root bootstrap output,
- selected ABI,
- daemon PID,
- Binder service state,
- selected media path,
- every TX24 payload/result,
- `/data/camera/vcplax.log`,
- `/data/camera/vcplax.err`,
- filtered logcat.
