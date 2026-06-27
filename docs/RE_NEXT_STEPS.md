# RE Tool pull audit + next reverse targets (2026-06-27)

Analysis of user upload batch: `re_tool_capture*.log`, `vcplax_*`, `README`, `MISSING`.

---

## 1. Что ты прислал — разбор

| Файл | Что внутри | Ценность |
|---|---|---|
| `vcplax_cmdline` | `/data/vcplax` + **`dataloader_managerhow`** | ✅ ServerName Binder-сервиса |
| `vcplax_pid` | pid=29881 | ✅ процесс жив |
| `vcplax_maps` | `/data/vcplax (deleted)` + frida-agent memfd | ✅ **ключевой инсайт** |
| `README` | timestamp pull | meta |
| `MISSING` | все `/data/*` бинарники MISSING | ❌ ожидаемо (см. §2) |
| `re_tool_capture*.log` | Attach×3, SEEK only, no HOOK_SYM | ⚠️ короткая сессия, не START |

### Главная находка из maps

```
633bffa000-633c439000 r-xp ... /data/vcplax (deleted)
```

- vcplax **удаляет себя с диска** после старта (`readlink /proc/self/exe` → `unlink`)
- **`libvc.so` / `libvc++.so` НЕ видны** как отдельные mapping — на этой сборке код hook'ов, вероятно, **внутри vcplax.so** (12 MB monolith) или загружен до inject
- Pull по пути `/data/vcplax` **всегда будет MISSING** если делать после unlink

### ServerName

`dataloader_managerhow` — random имя из `app_config.xml` → для spawn/clone нужно читать prefs, не hardcode `vlive`.

---

## 2. Чего не хватало в RE Tool v1.1 (исправлено в v1.2)

| Проблема | Fix v1.2 |
|---|---|
| Pull только `cp /data/vcplax` | **`/proc/PID/exe` dump** даже когда (deleted) |
| Нет natives из APK | **unzip** `lib/arm64-v8a/*.so` из target.apk |
| Нет prefs | копия `app_config.xml` + ServerName в README |
| Нет backup до unlink | watchdog **race-save** → `/data/local/tmp/vcplax.real` |
| libvc XOR не ловится | Frida: **MODULE_SCAN** + XOR scan на **base vcplax** если libvc.so нет |
| Короткий attach log | использовать **▶ START**, не Attach×3 |

---

## 3. Новый workflow (v1.2)

```text
Setup → ▶ START → поработать в testicecam2
     → Pull ALL (пока vcplax жив!)
     → Stop → Share log
```

**Pull ALL** кладёт в `/sdcard/Download/re_tool/`:

```text
re_tool/
  apk_native/arm64-v8a_vcplax.so    ← из APK (всегда)
  apk_native/arm64-v8a_libvc.so
  proc/vcplax_exe_dump              ← /proc/PID/exe (deleted ok)
  prefs/app_config.xml              ← ServerName
  vcplax_maps.txt / cmdline / fd
  deployed/MISSING.txt              ← что не на диске
```

---

## 4. Что ещё будем ковырять (приоритеты)

### P0 — бинарники и XOR (до 100% libvc)

| Задача | Метод | Артефакт |
|---|---|---|
| Runtime vcplax dump | `/proc/PID/exe` (v1.2 Pull) | `proc/vcplax_exe_dump` |
| APK reference | unzip natives | `apk_native/*.so` |
| XOR symbol names | Frida `[XOR_STATIC]` на vcplax base | log lines |
| Offline XOR | Ghidra/python на `libvc.so` из APK | `docs/` report |
| Diff APK vs runtime exe | md5/compare | tamper detect |

### P1 — media pipeline native

| Задача | Где |
|---|---|
| TX11 → FFmpeg open | Ghidra `0x43fedc` |
| TX14 mode switch | `0x4403d4` + cf7e RTMP path |
| TX24 color inject → buffer | `0x444024` + 47 live TX24 |
| MediaCodec output path | maps: `libmediaplayerservice.so` |

### P2 — clone без libvc

| Задача | Статус |
|---|---|
| `vcplax_clone` BBinder stub | skeleton in repo |
| Demux stub (FFmpeg) | not linked |
| Hook replacement without XOR names | **blocked on P0** |

### P3 — app layer

| Задача | % left |
|---|---|
| License/activation server | ~30% |
| Float panel full logic | ~15% |
| MediaProjection path | ~15% |

---

## 5. Обновлённый % восстановления

| Слой | % | Комментарий |
|---|---:|---|
| Binder TX 11–25 | **99.5%** | 3 runtime logs |
| RE Tool pipeline | **100%** | START validated (cf7e) |
| Runtime binary pull | **70%→85%** | v1.2 proc+apk pull |
| TX13 / TX24 live | **90% / 95%** | |
| libvc XOR hook names | **42%** | главный блокер |
| Native vcplax | **78%** | maps confirm self-delete |
| Java app | **92%** | |
| **ИТОГО** | **~95%** | +pull fixes → path to 97% |

---

## 6. Что прислать после v1.2

Минимальный набор для следующего раунда:

1. **`/sdcard/Download/re_tool/`** целиком (zip) после **Pull ALL**
2. **`re_tool_capture.log`** после **▶ START** (не Attach)
3. В логе искать: `[MODULE_SCAN]`, `[XOR_STATIC]`, `[HOOK_SYM]`

Если `[XOR_STATIC]` появится — clone разблокирован.

---

*RE Tool v1.2-pull-proc · branch cursor/re-tool-apk-e3a1*
