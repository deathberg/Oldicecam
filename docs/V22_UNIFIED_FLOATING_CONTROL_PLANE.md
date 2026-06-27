# V22 Unified Floating Control Plane

## Problem

The v21 floating overlay still owned its own transform apply path: it could bake JPEG frames and enqueue backend replay independently from MainActivity. During an active camera consumer session this created a second control plane, so overlay commands could collide with the main controller and destabilize the native replacement stream.

## Fix

V22 introduces `TransformController` as the single process-wide owner for transform commands and legacy render/apply commits.

- `MainActivity` routes transform changes through `TransformController`.
- `FloatService` no longer owns `RootBootstrap`, `VliveBinderClient`, `backendLock`, bake workers, or direct TX calls.
- Floating buttons are state-only by default.
- Floating `Commit` uses the same `TransformController.commit()` path as the main app `Commit / Apply` button.
- `BackendApplyQueue` remains the only serialized legacy TX14/TX11 replay queue.

## Control model

```text
MainActivity buttons ┐
                     ├─ TransformController ── MediaTransformer.bakeImage ── BackendApplyQueue ── TX14 -> TX11
FloatService buttons ┘
```

There is no separate floating Binder client anymore.

## Floating defaults

`FLOAT_AUTO_COMMIT=false`

Floating controls update `TransformState` only. This avoids replaying the backend while another camera consumer is foregrounded. Use `Commit` from the overlay only when an explicit backend update is needed.

## Expected logs

Look for:

```text
[float] command source=FLOAT op=... routed=TransformController
[txctl] state source=FLOAT reason=...
[txctl] commit requested source=FLOAT reason=float-commit
[applyq] queued source=txctl-float-float-commit
```

There should be no direct `[float] replay start`, no `[float] TX14`, and no `[float] TX11` path in v22.
