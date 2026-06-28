# IceCam v13 baked image transform

Runtime logs and screenshots showed that Binder TX24 changes color/correction state in this native build, not image geometry. Therefore v13 stops using TX24 for zoom/pan by default.

Changes:
- TX24 renamed in UI to color-correction debug.
- Zoom/pan/rotate/mirror buttons do not call TX24 by default.
- For selected local images, the transform is baked into a new 640x480 JPEG under app external files and then replayed through the known working TX14 -> TX11 sequence.
- For videos, transform state is saved but not applied to the stream because doing that correctly requires a native renderer/FFmpeg transcode stage.
- Native libraries are untouched.

Important:
- If the selected source is a photo, controls should visibly update the replacement after replay.
- If the selected source is a video, controls will log that video geometry transform is not implemented in this Java-only reconstruction.
