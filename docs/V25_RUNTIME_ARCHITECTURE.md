# IceCam Core v25 Runtime Architecture

## What changed

v25 introduces a single runtime state pipeline:

Command -> Reducer -> AppState -> Persistence -> SideEffectRunner

MainActivity and FloatService no longer own separate transform/apply paths. Both route through the process-wide CommandBus.

## Important behavior

- Transform taps are reducer commands and update TransformState only.
- Preview is realtime via ImageView Matrix in RealtimePreviewView.
- Transform taps do not create JPEG files.
- Commit/start are side effects and run off the UI thread.
- BUSY is OperationState(activeOperations) keyed by opId, not a Boolean.
- Flight Recorder writes commands.log, timeline.log, state.log, performance.log and device.txt into IceCam_Report.zip.
- Marker overlay is exposed as LastMarkerId and rendered as #N in MainActivity/FloatService status.

## Compatibility

BackendApplyQueue remains as a legacy adapter for the native backend boundary. It is no longer used for preview transforms.
