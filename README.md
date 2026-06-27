# IceCam Pro

Android-приложение для управления потоком камеры (v28, RE-unified Binder protocol).

> **Reverse engineering** `testicecam2.apk` (исходник, декompile, tools, clone, retool) вынесен в отдельную ветку  
> [`cursor/re-testicecam2-e3a1`](https://github.com/deathberg/Oldicecam/tree/cursor/re-testicecam2-e3a1)

## Сборка

```bash
gradle :app:assembleDebug
```

## Документация приложения

- [docs/APP_V27_RE_UNIFIED.md](docs/APP_V27_RE_UNIFIED.md) — протокол Binder (TX11–19, bake)
- [docs/V25_RUNTIME_ARCHITECTURE.md](docs/V25_RUNTIME_ARCHITECTURE.md) — runtime architecture

## Ветки

| Ветка | Содержимое |
|-------|------------|
| `cursor/recon-unified-e3a1` | IceCam Pro (текущая разработка) |
| `cursor/re-testicecam2-e3a1` | RE testicecam2.apk + clone + retool |
