# IceCam v19 — Stable Backend Queue and GPU Pipeline Preparation

## Scope

v19 is a stabilization release before the realtime GPU/MediaCodec renderer branch.
It keeps the legacy native backend control path:

```text
TX14(mode=1, path) -> TX11(path, mirrorIgnored, loop)
```

and removes the most dangerous v18 race in the apply queue.

## Backend queue changes

`MainActivity` and `FloatService` no longer own independent `pendingApplyPath`,
`pendingApplySource`, `pendingApplyForce` apply loops. Both delegate to:

```text
dev.icecam.app.BackendApplyQueue
```

The queue uses one immutable request object:

```java
ApplyRequest(path, source, force, sequence, createdAtMs)
```

The worker drains pending work with `AtomicReference.getAndSet(null)`. Rapid clicks
replace the pending request with the latest complete snapshot, so `path`, `source`
and `force` cannot be mixed from different UI events.

## Retry behavior

If the first TX14/TX11 apply fails, v19 restarts/bootstrap-checks the daemon and
then retries the newest available request. If no newer request appeared during
recovery, it retries the failed snapshot.

This fixes the v18 behavior where the log said "latest path" but retry could still
replay an obsolete path captured before backend recovery.

## Baked image cache

`MediaTransformer` now prunes generated files in `externalFilesDir/baked`:

- max retained baked JPEG files: 24
- max baked JPEG age: 6 hours

This is a containment layer for the legacy bake/replay path. The production target
remains realtime rendering without JPEG bake.

## Realtime renderer direction

The next branch should add a render plane independent from the backend command queue:

```text
MediaCodec decoder output Surface
 -> SurfaceTexture / OES texture
 -> OpenGL ES matrix transform
 -> encoder input Surface or YUV shared-memory bridge
 -> native backend / RTMP session
```

Required components:

```text
TransformController
GpuFrameRenderer
MediaCodecDecodeSession
EncoderSurfaceBridge or YuvSharedMemoryBridge
BackendSessionSupervisor
```

Transform UI should update atomic `TransformState` only. It must not enqueue
TX14/TX11 on every tap once realtime rendering is active.
