# V21 Stable Canvas Auto Apply

Observed from device logs/video:
- v20 required manual Apply after every transform, which is not usable for floating controls.
- Legacy baked replay changed output dimensions on rotate: 1080x2400 <-> 2400x1080.
- Changing the replayed media dimensions while an external camera consumer is active can destabilize the native stream and crash consumers.

Changes:
- Transform buttons auto-apply again, but through a quiet-window coalescer.
- Main and floating controls use an 850 ms quiet window and a 450 ms post-replay cooldown.
- Baked output now uses a stable session canvas stored in prefs as StableOutputWidth/StableOutputHeight.
- Rotation is rendered inside the locked canvas instead of swapping output width/height.
- JPEG quality reduced to 88 and baked cache capped to 12 files.
- Logging now records stable canvas lock and baked source/decoded/output dimensions.

This is still a legacy TX14/TX11 replay pipeline. The correct long-term fix remains realtime Surface/GL/MediaCodec rendering without JPEG replay.
