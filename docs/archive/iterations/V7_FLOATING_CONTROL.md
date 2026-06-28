# v7 Floating transform control

Changes:

- Added real overlay service `FloatService`.
- Floating window has zoom +/- controls, X/Y movement arrows, 90 degree rotation, reset, mirror/loop/autorotate toggles.
- Overlay writes recovered original keys: `AutoColor_X`, `AutoColor_Y`, `Scale`, `PlayAngle`, `PlayMirror`, `PlayisLoop`, `PlayAutoRotate`.
- Overlay sends TX24 after every transform change and can send TX11/TX14 via `Play file`.
- Overlay permission is requested through Android system settings when needed.
- Existing root/Binder/native launch flow is left unchanged.

Limitation:

TX24 support depends on `/data/vcplax` actually publishing the reconstructed Binder service and accepting the recovered transform parcel. If native ignores zoom/pan, the UI still works and logs TX24 results, but native-side rendering must be patched next.
