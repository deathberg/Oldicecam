# IceCam Core v18 — Reliable apply queue

v18 fixes the v17 issue where transform actions could bake a new image file but fail to replay it through the backend.

## Changes

- Added a dedicated serialized apply queue.
- Every baked frame now queues a backend replay path.
- If backend is busy, only the latest path is kept and applied next.
- Manual Apply forces `bake -> TX14 -> TX11`.
- Floating controls use the same reliable apply queue.
- Binder death triggers cache clear, daemon restart, and one replay retry.
- Logs now include `[apply] queued/completed` records.

## Expected log pattern

After any applied transform, logs should show:

```text
[bake] image baked high-quality ... -> /files/baked/...
[transform] ... baked/replay ...
[apply] queued source=transform-...
[ui] media apply start source=transform-...
[binder] TX14 -> 4
[binder] TX11 -> 1
[ui] media apply done ... active=true
```

If TX11/14 fails, v18 retries once after restarting the native daemon.
