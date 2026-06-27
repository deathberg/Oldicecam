# Полный отчёт по реверс-инжинирингу testicecam2.apk

APK в репозитории: `testicecam2.apk` (~19 MB, дата сборки 2025-12-09).

Инструменты: **jadx 1.5.1**, **apktool 2.11.1**, `strings`, `readelf`.

Декомпилированные артефакты (локально, не в git): `decompiled/jadx/`, `decompiled/apktool/`, `decompiled/raw/`.

---

## 1. Идентификация приложения

| Поле | Значение |
|---|---|
| Manifest package (обфускация) | `com.potplayer.music` |
| Реальный код приложения | `com.xiaomi.vlive.*` |
| Application class | `com.xiaomi.vlive.App` |
| Label | `新相机(1926内核)` — «Новая камера (ядро 1926)» |
| compileSdk | 35 (Android 15) |
| min/target | из apktool framework |

Компоненты:

- `com.xiaomi.vlive.MainActivity` — LAUNCHER
- `com.xiaomi.vlive.FloatService` — плавающая панель
- `com.xiaomi.vlive.MediaProjectionForegroundService` — foreground для MediaProjection

---

## 2. Нативные библиотеки

| Файл | arm64-v8a | armeabi-v7a | Назначение |
|---|---:|---:|---|
| `vcplax.so` | 12 027 424 B | 11 326 456 B | Root-демон, Binder-сервис, FFmpeg, MediaCodec |
| `libvc.so` | 1 291 296 B | 825 892 B | Hook/inject в camera pipeline |
| `libshadowhook.so` | 74 584 B | 57 332 B | ByteDance ShadowHook |

### vcplax.so (arm64)

- Тип: **PIE ELF64**, entry `0x43e880`
- NEEDED: `libbinder.so`, `libmediandk.so`, `libEGL.so`, `libGLESv2.so`, `libandroid.so`, …
- Символы: `android::BBinder`, `AMediaCodec_*`, встроенный **FFmpeg**
- Запуск: `/data/vcplax <ServerName>&` (аргумент = имя сервиса в ServiceManager)

### libvc.so

- Загружается как `/data/libvc.so`
- `libshadowhook.so` копируется как `/data/libvc++.so` (маскировка имени)
- Использует `GraphicBuffer`, `shadowhook_hook_sym_name`, Binder

### Деплой (из `App.onCreate`)

```text
1. Распаковать .so из APK в filesDir/lib/{abi}/
2. cp libvc.so           -> /data/libvc.so
3. cp libshadowhook.so   -> /data/libvc++.so
4. cp vcplax.so          -> /data/vcplax
5. chmod 700 /data/vcplax
6. /data/vcplax <ServerName>&
7. killall vcplax при повторном старте
```

ABI выбирается командой `file /system/bin/cameraserver` (32-bit → armeabi-v7a, иначе arm64-v8a).

---

## 3. Binder IPC — `com.xiaomi.vlive.IMyBinderService`

Прокси: `p059d1.C1391f` → реконструкция: `reconstructed/original-apk/.../binder/MyBinderClient.java`

| TX | Метод | Parcel (write) | Success | Назначение |
|---:|---|---|---|---|
| 11 | playSource | String path, int 0, int loop | int==4 | Загрузить/сменить источник (MP4/RTMP) |
| 12 | stopOrQuery | — | int | Остановка / запрос состояния |
| 13 | pollState | — | int[] | Периодический опрос (1 Hz из App) |
| 14 | setMode | int mode, String value | int==4 | Режим/путь (type 1 = file) |
| 15 | getStatus | — | int | **5 = воспроизведение активно** |
| 16 | setAutoRotate | int bool | int | «画面纠正» — авто-поворот |
| 17 | setLoop | int bool | int | Цикл воспроизведения |
| 18 | setAngle | int degrees | int | Поворот 0/90/180/270 |
| 19 | setMirror | int bool | int | Зеркало |
| 22 | seekRange | long startUs, long endUs | int | Seek по микросекундам (действия лица) |
| 24 | transform | int mode, 4×float, int flags | int==14 | Геометрия / «三色» auto-color |
| 25 | hardRecovery | — | int | Жёсткое восстановление (кнопка «替») |

Подключение (`AbstractC0330t.m837E`):

```text
setenforce 0
binder = ServiceManager.getService(App.serverName())
linkToDeath + setenforce 1
writeInterfaceToken("com.xiaomi.vlive.IMyBinderService")
```

`ServerName` — random: берётся случайное имя из `ServiceManager.listServices()` + 1–3 случайные буквы, либо 5–12 букв при ошибке.

---

## 4. SharedPreferences (`app_config`)

| Ключ | Тип | Default | Описание |
|---|---|---|---|
| ServerName | String | random | Имя Binder-сервиса vcplax |
| PlayFileType | int | 1 | 1=MP4, 2=RTMP |
| PlayFileMp4 | String | "" | Путь к видео |
| PlayRtmpUrl | String | rtmp://ns8.indexforce.com/home/mystream | RTMP URL |
| PlayisLoop | bool | false | Цикл |
| PlayMirror | bool | false | Зеркало |
| PlayAngle | int | 0 | Угол (+=90 по кнопке) |
| PlayAutoRotate | bool | false | Авто-коррекция (TX16) |
| PlayAutoColor_mode | int | 1 | Режим «三色» |
| AutoColor_X/Y | float | 50/50 | Координаты % |
| AutoColor_intensity | float | 0.3 | Интensity |
| AutoColor_diameter | float | 0.6 | Диаметр |
| MonitorTargetX/Y | int | 55/380 | Точка экрана для color inject |
| ActionRangebgin{N} | long | см. settings | Начало действия N (µs) |
| ActionRangeEnd{N} | long | см. settings | Конец действия N (µs) |
| FloatingTool | bool | false | Показ float overlay |

### Действия лица (по умолчанию, µs)

| ID | UI | begin | end |
|---:|---|---:|---:|
| 1 | 眨眼 (глаз) | 0 | 1 170 000 |
| 2 | 抬头 | 5 000 000 | 5 900 000 |
| 3 | 张嘴 | 2 000 000 | 3 200 000 |
| 4 | 左转头 | 3 200 000 | 4 000 000 |
| 5 | 回正 | 4 000 000 | 4 000 000 |
| 6 | 右转头 | 4 000 000 | 5 000 000 |
| 8 | 点头 | 5 600 000 | 6 800 000 |

---

## 5. UI архитектура

```text
MainActivity (BottomNavigation)
├── HomeFragment      — уведомления, проверка /data/camera
├── ControllerFragment — основной UI замены камеры
└── SettingsFragment  — таймкоды действий, координаты монитора

Float overlay (FloatService / App.m1774h)
├── F1-F8: seek по ActionRange
├── F9: loop, F10: rotate, F11: mirror, F12: close
├── 1/2/3: /sdcard/Movies/{1,2,3}.mp4
└── 替: TX25 recovery
```

### ControllerFragment — ключевая логика

1. **Выбор источника**: MP4 (SAF → cache/play.mp4) или RTMP URL → TX11/TX14
2. **«替换相机»**: hook камеры через vcplax
3. **«前置预览»**: `PreviewPatcher` (Camera2 TextureView)
4. **«三色注入»**: MediaProjection → ImageReader → анализ пикселя (MonitorTargetX/Y) → TX24
5. **Polling**: App TimerThread каждую 1 с вызывает TX13, обновляет LiveData

### HomeFragment

- Предупреждение если `/data/camera/libshadowhook.so` или `/data/samera/` существуют (конкурирующие virtual cam)
- Очистка: `chattr -i`, `rm -r /data/camera`, `rm -r /data/samera`

---

## 6. Root shell

`AbstractC0330t.m863q(cmd)` — persistent `su` process:

- `Runtime.exec("su")` + DataOutputStream
- Маркер EOF через `echo EOF_MARK_<timestamp>`
- Используется для cp/chmod/killall/setenforce

---

## 7. Лицензирование (classes5.dex)

Пакет `xyz.vcxm.vmxplay`:

- `ActivationManager` — POST на `https://vcxm.liuzhou.shop/vc.php`
- `DeviceFingerprint`, `LicenseClient`, `PreviewPatcher`
- `startJsonCheckFlow` в декompile помечен как unreachable (проверка отключена/обфусцирована)
- `__ACTIVATED = true` hardcoded в статике

---

## 8. DEX структура

| DEX | ~размер | Содержимое |
|---|---:|---|
| classes.dex | 1.4 MB | com.xiaomi.vlive + app logic |
| classes2.dex | 10.3 MB | AndroidX, Material, OkHttp |
| classes3.dex | 477 KB | Navigation, lifecycle |
| classes4.dex | 2.5 KB | minimal |
| classes5.dex | 37 KB | xyz.vcxm.vmxplay (license + preview patch) |

Всего **3480** Java-классов в jadx (29 ошибок декompиляции).

---

## 9. Сравнение с IceCam Core (текущая реконструкция)

| Аспект | Оригинал (APK) | IceCam Core (app/) |
|---|---|---|
| ServerName | Random из listServices | Fixed `privsam_service` |
| Deploy path | `/data/vcplax`, `/data/libvc.so` | + `/data/camera/*` копии |
| UI | 3 Fragment + Material | Single MainActivity preview-first |
| State | LiveData + SharedPreferences | StateStore/CommandBus runtime |
| TX24 | Используется для 三色 | Зарезервирован, TX14 для geometry |
| License | vcxm activation server | Нет |

Текущий `app/` — **улучшенная clean-room реконструкция**, не побайтовая копия.

---

## 10. Уровень восстановления

| Область | Оценка |
|---|---:|
| Manifest, permissions, components | 100% |
| Binder TX 11–25 сигнатуры | 95% |
| SharedPreferences / defaults | 95% |
| Root bootstrap / deploy | 92% |
| UI layouts (apktool) | 90% |
| ControllerFragment logic | 85% |
| Native vcplax internals | 65–75% |
| Точный исходный Java/Kotlin | невозможно (ProGuard/R8, синтетика) |

---

## 11. Реконструированный код

Читаемая реконструкция оригинала: **`reconstructed/original-apk/`**

Содержит деobfuscated Java для:

- `App`, `MainActivity`, `FloatService`, `MediaProjectionForegroundService`
- `binder/MyBinderClient`, `util/RootShell`, `util/VliveBridge`
- `ui/*` фрагменты (логика без Material boilerplate)
- `config/AppConfigKeys`, `config/ActionRanges`

---

## 12. Расширенный native RE

См. [docs/NATIVE_REVERSE_MAX.md](NATIVE_REVERSE_MAX.md) — LIEF, Capstone, xref-скан, адреса TX11–25, строка `%8ld: Replaced %5d blocks by color %X`.

## 13. Как повторить декompиляцию

```bash
# jadx
tools/bin/jadx -d decompiled/jadx --show-bad-code --deobf testicecam2.apk

# apktool (ресурсы + smali)
java -jar tools/apktool.jar d testicecam2.apk -o decompiled/apktool -f

# native strings
strings decompiled/raw/lib/arm64-v8a/vcplax.so | less
```
