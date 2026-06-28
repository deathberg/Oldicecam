# IceCam Core v15

This version is a new IceCam-focused project branch, not another UI patch of the original APK.

## Design decision

The original app's strongest recovered pieces are kept only as a legacy backend:

- root native deployment
- fixed Binder service name `privsam_service`
- `vcplax` process lifecycle
- TX14 -> TX11 local source playback
- restore-camera watchdog

The old assumptions that caused regressions are removed:

- TX24 is **not** treated as zoom/pan anymore.
- TX25 is **not** used for normal stop.
- random service names are removed.
- descriptor-hard rejection is removed for the known native service.

## New IceCam Core layer

The new layer is owned by IceCam:

- `TransformState`: pan/zoom/crop/rotate/mirror/fit-fill
- `MediaTransformer`: baked transform path for photo sources
- `FloatService`: floating controller with clear status and restore action
- `MainActivity`: simplified English UI
- `RootBootstrap`: deploy/start/restore backend
- `VliveBinderClient`: legacy backend IPC wrapper

## Current capability

### Works now

- local media selection
- replacement start through native backend
- camera restore by killing `vcplax`
- visible state: service ready, active, restored/error
- photo transform controls via baked 640x480 camera-compatible JPEG
- floating controls

### Preserved backend

- video source playback through the native backend
- loop flag
- legacy service compatibility

### Still requires next native renderer stage

Realtime video transform cannot be fixed by TX24; runtime tests show TX24 is color-correction/debug. Realtime video zoom/pan/crop needs a new clean renderer stage:

```text
video decode -> GLES transform -> encoder/local source bridge -> legacy/native backend
```

This version is structured so that renderer can be added without rewriting the app shell again.
