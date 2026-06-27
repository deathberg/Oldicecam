# IceCam v14 — camera restore and visible runtime status

This build focuses on safe operational state handling:

- visible runtime state in the main screen and floating panel;
- `ReplacementActive` flag persisted in app preferences;
- `IceCamState` phase tracking: `IDLE`, `STARTING`, `SERVICE_READY`, `REPLACEMENT_ACTIVE`, `RESTORING_CAMERA`, `CAMERA_RESTORED`, `SERVICE_ERROR`, `PLAY_ERROR`;
- `Restore camera` button now performs a root-level daemon shutdown and verifies whether `privsam_service` disappeared;
- ordinary stop no longer sends TX25 by default because prior logs showed TX25 can leave camera clients black;
- floating panel Restore uses the same process-level restore path;
- native libraries are unchanged.

Recommended test sequence:

1. Start service.
2. Select media.
3. Play selected.
4. Open camera client and verify replacement.
5. Press Restore camera.
6. Fully close/reopen target camera client if it kept an already hooked Surface/session.
7. Verify state becomes CAMERA_RESTORED and Binder no longer connects.

Note: if a target app already loaded a hooked graphics/camera path, killing the daemon may not refresh that app's existing camera session. Reopen the camera screen or force-stop that specific target app during debugging.
