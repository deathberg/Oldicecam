# IceCam Core v17 — Queued Transform + Media Slots

Changes:

- Added M1–M4 media slots.
- Tap slot = select / switch. Long press or + replace = choose media for the slot.
- Active media switch replays through the existing backend without restarting the daemon.
- Reworked image transform scheduling. Rapid taps no longer bake/replay every single click.
- Transform buttons update state immediately, then one final baked frame is applied after a quiet window.
- Floating overlay now duplicates Start / Restore / Zoom / Pan / Rotate / Mirror / Fit/Fill / Crop / Apply.
- Backend TX14/TX11 operations are serialized with a lock to avoid overlapping Binder transactions.
- DeadObjectException path clears Binder cache instead of continuing stale calls.
- TX24 color-correction path remains removed from normal controls.

Known backend limitation:

- Photo transforms are still baked/replayed because the recovered native backend does not expose a reliable realtime geometry transaction.
- Video transform state is saved but not rendered in realtime by this legacy backend.
