# IceCam Pro v26 — приложение

## Что реализовано

### UI / управление потоком
- **Плавающее меню** — D-pad (◀▲●▼▶), zoom ±, center, rotate 90°, mirror X/Y, FIT/FILL, APPLY
- **Слоты M1–M4** в float и в главном экране — переключение медиа на лету
- **START/STOP** — кнопка меняет цвет: зелёная = старт, красная = стоп
- **4 окна превью** (2×2) + большое LIVE-превью активного слота
- Статус: LIVE/IDLE, фаза backend, слот, тип медиа, zoom, transform

### Медиа-движок
- **PNG, JPEG, HEIF/HEIC** — `ImageDecoder` (API 28+)
- **MP4, MOV, MKV, WebM** — GPU preview через `TextureView` + `MediaPlayer`
- **Умная цветокоррекция** — auto WB + histogram stretch (`ColorCorrector`)
- CPU bake для backend (JPEG) с тем же transform pipeline

### Фон / debug
- **StreamForegroundService** — notification при активном потоке
- **SmartLogger** — уровни DEBUG/INFO/WARN, FPS по scope, тайминги, export log

### Native (требует jniLibs)
Положите в `app/src/main/jniLibs/{arm64-v8a,armeabi-v7a}/`:
- `libvc.so` — inject (или `libvc_clone.so` из `clone/`)
- `libshadowhook.so` — **ShadowHook 2.x**
- `vcplax.so` — daemon

Без этих файлов APK соберётся, но root bootstrap не задеплоит backend.

## Сборка

```bash
gradle :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Android 16

- App: `targetSdk 35`, `minSdk 23`, hardwareAccelerated
- Native hooks: при API 36 проверьте символы `libcameraservice.so` (см. `tools/decode_libvc_xor.py`, `clone/include/HookSymbols.h`)
