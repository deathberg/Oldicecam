# IceCam v16 — Stability and UI cleanup

## Goals
- Keep the working legacy native backend.
- Remove unsafe/noisy UI controls.
- Remove service-name editing from the main screen.
- Remove runtime log from the main screen; keep Export Log only.
- Remove TX24/color-correction UI from normal workflow.
- Make the floating overlay a simple Start/Restore switch only.
- Improve photo quality by baking frames at source resolution when possible.
- Protect the backend from rapid button taps using debounce and single-flight queues.

## Important behavior
- TX24 is not used for pan/zoom/crop. Runtime tests showed it behaves like color correction.
- Photo transforms are baked into a new high-quality JPEG and replayed through the working TX14 -> TX11 sequence.
- Video transforms are saved as state only. Real-time video transforms require a dedicated renderer stage.
- Restore camera is process-level: stop/kill vcplax and verify that the service disappears.

## Stability changes
- Rapid image-control taps are coalesced into the latest transform.
- Transform rendering uses a debounce delay before baking/replay.
- Start/Restore are protected with single-flight guards.
- Floating overlay no longer triggers transform/replay loops.

## Quality changes
- Baked images now use the decoded source dimensions where possible.
- Maximum output dimension is capped at 2560 px for memory safety.
- JPEG quality is 100.
- Rotation can swap output dimensions.
