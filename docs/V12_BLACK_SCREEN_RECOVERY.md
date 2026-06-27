# v12 black-screen recovery

Changes based on runtime logs:

- `privsam_service` is stable and Binder connects correctly.
- Black screen appeared after destructive `TX25` stop and after automatic `TX24` calls.
- `TX24` currently returns `14`, so it is no longer auto-sent unless `Enable TX24 transform calls` is enabled.
- Normal media apply now uses the last known working legacy order: `TX14 -> TX11`.
- `TX22` is not sent during normal source switching.
- `TX25` is disabled for Soft stop; use `Restart daemon` for hard reset.

If replacement works again, enable TX24 manually only for transform testing.
