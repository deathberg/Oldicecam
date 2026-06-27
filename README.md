# IceCam Core v23

Ready-to-upload Android project for GitHub Actions.

## APK reverse engineering (testicecam2.apk)

- Full report: [docs/APK_FULL_REVERSE_ENGINEERING.md](docs/APK_FULL_REVERSE_ENGINEERING.md)
- Extended native RE: [docs/NATIVE_REVERSE_MAX.md](docs/NATIVE_REVERSE_MAX.md)
- RE workspace setup: [re-workspace/README.md](re-workspace/README.md)
- Auto native dump: [docs/native_analysis/](docs/native_analysis/)
- Reconstructed original source (readable): [reconstructed/original-apk/](reconstructed/original-apk/)
- Decompile index: [decompiled/INDEX.md](decompiled/INDEX.md)

## v23 focus

`v23-neon-control-panel` rebuilds the app-side control plane and UI around one source of truth:

- `BuildInfo` centralizes version labels for UI/logs/diagnostics.
- `TransformController` is the only owner of transform commands from MainActivity and FloatService.
- Main UI is preview-first: transforms update the preview immediately, then legacy backend apply is debounced.
- Floating controls use the same controller path and no longer own Binder/TX/bake logic.
- Start and Restore are merged into one state-aware button.
- Status panel shows backend/replacement/transform/source state.
- Media slots are displayed as thumbnail cards with `+` replace buttons.
- Buttons use neon pressed/selected states for touch feedback.

Legacy backend path remains `TX14 -> TX11`; `TX24` is still not used for geometry transform and `TX25` remains reserved for hard recovery only.


## v24 compact-stable-ui
- Compact controls (~1/3 shorter buttons).
- Advanced panel toggles instead of duplicating.
- Main/floating transform buttons are preview-first; PLAY / COMMIT applies to legacy backend.
- Cached thumbnails and lower-cost preview rendering reduce UI stalls.
- Version bumped to 0.24-v24-compact-stable-ui.
