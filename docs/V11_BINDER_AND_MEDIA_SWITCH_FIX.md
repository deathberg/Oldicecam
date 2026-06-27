# IceCam v11 binder/media switch fix

Fixes regression from v10:

- `privsam_service` is kept as stable service name.
- Binder client now accepts the native service even when `getInterfaceDescriptor()` is empty.
- Binder instance is cached after first successful connection; no stateful TX12 probing before every command.
- Normal media switching no longer sends TX25 first, because TX25 can close/reset the native endpoint on some builds.
- TX25 is kept only for manual Stop.
- Media apply sequence is now: TX22 range reset -> TX14 source mode -> TX11 source/play -> TX24 current transform.

Expected log after fix:

```
[binder] connected raw service=privsam_service
[ui] soft media apply done TX22=... TX14=... TX11=... TX24=...
```
