# V23 Neon Control Panel

This build fixes the v22 UX/control-plane regression.

## Architectural rules

1. `TransformController` is the only transform command executor.
2. `MainActivity` and `FloatService` dispatch commands, never direct TX operations.
3. UI preview is updated immediately from `TransformState`.
4. Backend replay is debounced and serialized through `BackendApplyQueue`.
5. Start/Restore is a single state-aware action.
6. `BuildInfo` is used by UI, logs and diagnostic snapshots.

## Runtime states

The status screen exposes:

- Backend: `READY` / `OFF`
- Replacement: `ACTIVE` / `OFF`
- Transform: dirty/rendering/applying/replacement state
- Source slot and current transform summary

## Floating controls

Floating controls are now a remote controller for the same app-level control path. They use debounced auto-commit through `TransformController` and do not call Binder, root or native TX methods directly.
