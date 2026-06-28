# Native RE — финальный отчёт (Ghidra + r2)

Дата: 2025-06-27  
APK: `testicecam2.apk`  
Бинарники: `decompiled/raw/lib/arm64-v8a/{vcplax.so,libvc.so,libshadowhook.so}`

---

## 1. Статус Ghidra: vcplax импортирован успешно

| Параметр | Значение |
|---|---|
| Проект | `re-workspace/ghidra/icecam_native` |
| Бинарник | `/vcplax.so` (arm64-v8a) |
| Анализ | **249 с**, `DONE_EXIT=0` |
| Функций | **30 941** |
| Image base | **`0x00100000`** (важно!) |
| `.text` в Ghidra | `0x0053e880` – `0x00bd296f` |
| Entry (ELF) | file VA `0x43e880` → Ghidra **`0x0053e880`** |

Лог импорта: `re-workspace/logs/ghidra_vcplax_arm64.console`

Также в проекте: `libvc.so`, `libshadowhook.so` (анализ headless).

### Исправление адресов

Ранние экспорты падали с `No function at 0043e880`, потому что скрипт передавал **file VA**, а Ghidra сдвигает секции на **+0x100000**.

Исправлено в `tools/ghidra/DecompileAt.java`: автоматический `resolveQueryAddress()` — если функции нет по прямому адресу, пробует `imageBase + fileVA`.

---

## 2. Архитектура vcplax (подтверждено декомпиляцией)

```text
main (0x43e880)
  └─ FUN_0053e894()           # реальный init
       └─ dlopen path (0x43f05c)
            ├─ defaultServiceManager()
            ├─ addService(String16(argv[1]), BBinder*)
            ├─ readlink /proc/self/exe → unlink self
            ├─ dlopen libvc++.so + libvc.so (paths рядом с exe)
            └─ ProcessState::startThreadPool(); joinThreadPool()

BBinder::onTransact @ 0x43f8b4 (FUN_0053f8b4)
  ├─ enforceInterface("com.xiaomi.vlive.IMyBinderService")
  └─ switch(code) → TX handlers
```

Полная декомпиляция диспетчера: `re-workspace/exports/vcplax.so_binder_ontransact.c`

---

## 3. Таблица Binder TX (native ↔ Java)

Java: `app/src/main/java/dev/icecam/app/VliveBinderClient.java`

| Code | Java TX | File VA handler | Назначение (из Ghidra) |
|---:|---|---|---|
| `0x0b` | 11 PLAY_SOURCE | `0x43fedc` | Запуск источника: копирует путь сервиса, собирает struct 0x14c, вызывает media pipeline |
| `0x0c` | 12 STOP_OR_QUERY | `0x4402b0` | writeNoException + ack |
| `0x0d` | 13 POLL_STATE | `0x440324` | Возвращает 5× int32 (состояние очереди/кодека) |
| `0x0e` | 14 SET_MODE | `0x4403d4` | readInt32 mode + readString16 path → переконфигурация demux/decode |
| `0x0f` | 15 GET_STATUS | inline | `FUN_00541f7c(ctx, 0, -1)` — сброс/статус seek |
| `0x10` | 16 AUTO_ROTATE | inline | `readBool` → `ctx+0xa8` |
| `0x11` | 17 LOOP | inline | `readBool` → `ctx+0x98` |
| `0x12` | 18 ANGLE | inline | `readInt32` → `ctx+0xac`; допустимо 0, 0x5a, 0xb4, 0x10e |
| `0x13` | 19 MIRROR | inline | `readBool` → `ctx+0xb0`, сброс `ctx+0xa8` |
| `0x14` | — | `0x440624` | **Не в Java client**: readString16 → writeString16 ×3 (internal IPC) |
| `0x15` | — | `0x440908` | **Не в Java client**: write 3 string16 из локальных буферов |
| `0x16` | 22 SEEK_RANGE | inline | `readInt64` ×2 → `FUN_00541f7c(ctx, start, end)` µs |
| `0x17` | — | `0x440ad0` | **Не в Java client** |
| `0x18` | 24 TRANSFORM | inline | `readInt32` + **4× float** + `readInt32` → `ctx+0x160..0x174` (**три-color injection**) |
| `0x19` | 25 HARD_RECOVERY | inline | `*(byte*)(this+0x44) ^= 1` — toggle recovery |
| `0x32` | — | `0x440c98` | Extended handler |
| `0x33` | — | `0x440e38` | Extended handler |

### TX24 — семантика «三色注入»

```cpp
// case 0x18 — внутри onTransact @ 0x43f8b4
int mode = parcel.readInt32();
float f0 = parcel.readFloat();
float f1 = parcel.readFloat();
float f2 = parcel.readFloat();
float f3 = parcel.readFloat();
int color = parcel.readInt32();
ctx->field_160 = mode;
ctx->field_164 = f0;  // вероятно YUV/HSV пороги или ROI
ctx->field_168 = f1;
ctx->field_16c = f2;
ctx->field_170 = f3;
ctx->field_174 = color;  // RGB packed (%X в логе)
parcel.writeNoException();
```

Строка в `.rodata`: `%8ld: Replaced %5d blocks by color %X` — подтверждает **поблочную замену цвета** в нативном кадре (FFmpeg/GraphicBuffer path).

---

## 4. Ключевые функции vcplax (псевдокод)

### 4.1 dlopen libvc (0x43f05c)

```cpp
int daemon_main(int argc, char** argv) {
    if (argc < 2) return 0;
    const char* service_name = argv[1];
    sp<IServiceManager> sm = defaultServiceManager();
    sp<BBinder> svc = new MyBinderService(...);  // FUN_0053f39c
    sm->addService(String16(service_name), svc);
    char exe_path[200];
    readlink("/proc/self/exe", exe_path, sizeof exe_path);
    unlink(exe_path);  // anti-forensics
    build_path(base_dir, "libvc++.so");  // shadowhook
    build_path(base_dir, "libvc.so");
    dlopen_both_and_init();
    ProcessState::self()->startThreadPool();
    IPCThreadState::self()->joinThreadPool(true);
}
```

Экспорт: `re-workspace/exports/vcplax.so_dlopen_libvc.c`

### 4.2 TX11 playSource (0x43fedc)

- Проверяет готовность путей `libvc.so` / `libvc++.so`
- Аллоцирует объект 0x14c байт, копирует имя сервиса из `BBinder+0x58`
- Подтягивает глобальные callback-таблицы из `PTR_DAT_00c729c8`
- Запускает demux/decode thread (`FUN_00541108` и далее)

Экспорт: `re-workspace/exports/vcplax.so_tx11_play.c` (7281 байт)

### 4.3 TX14 setMode (0x4403d4)

```cpp
void tx14_set_mode(BBinder* self, Parcel* in, Parcel* out) {
    int mode = in->readInt32();
    self->mode = mode;
    String16 path16; in->readString16(&path16);
    assign_string(self->path, String8(path16));
    reset_media_context(self->ctx);  // flush buffers, stop decoder
    start_with_path(self->ctx, self->path);
    out->writeNoException();
}
```

Экспорт: `re-workspace/exports/vcplax.so_tx14_setmode.c`

### 4.4 TX13 poll (0x440324)

Возвращает счётчики из глобалов `DAT_00c79bf8`, `DAT_00c79c00`, `DAT_00c79bfc`, `DAT_00c79c04` — мониторинг pipeline.

---

## 5. libvc.so — ShadowHook init (0x777fc region)

Ghidra: функция `init` @ `0x1774b0` (file `0x774b0`), hooks @ `0x1777fc`.

```cpp
int init(LoadedLib* ctx) {
    copy_hook_table(ctx, global_hook_state);
    if (shadowhook_init(SHADOWHOOK_MODE_UNIQUE, 0) != 0) return -1;

    std::string lib = decode_xor(&DAT_0014ae86, len=0x13, key=ctx[8]);
    std::string sym1 = decode_xor(..., len=0x9f, key=ctx[0x7d]);
    // sym2..sym5 — длины 0x9d, 0x99, 0x34, 0x28

    shadowhook_hook_sym_name(lib, sym1, FUN_00177238, &orig1);
    shadowhook_hook_sym_name(lib, sym2, FUN_001772e4, &orig2);
    shadowhook_hook_sym_name(lib, sym3, FUN_00177370, &orig3);
    // on partial failure:
    shadowhook_hook_sym_name(lib, sym4, FUN_001773fc, &orig4);
    shadowhook_hook_sym_name(lib, sym5, FUN_00177408, &orig5);
    pthread_create(..., hook_worker, NULL);
    return 0;
}
```

**Имена библиотек/символов XOR-обфuscированы** (ключ — байты из runtime struct `ctx`). Статически декодируются только при знании key bytes из live процесса.

Экспорт: `re-workspace/exports/libvc.so_init_hook_1.c` (9389 байт)

Целевые библиотеки (из строк/import): `libui.so`, `libbinder.so`, `GraphicBuffer`, `lock`, `lockYCbCr`, `AMediaCodec_*`.

---

## 6. libshadowhook.so

- `JNI_OnLoad` @ file `0x6e7c` — регистрация `com.bytedance.shadowhook.ShadowHook`
- Версия: **ShadowHook 1.0.10** (строки в `.rodata`)

---

## 7. radare2 (исправлен base)

Для согласованности с Ghidra использовать:

```bash
r2 -B 0x100000 -e bin.cache=true -c 'aaa; s 0x43f8b4; pdf' vcplax.so
```

Скрипт: `tools/r2/deep_vcplax.sh` (обновлён).

Xrefs (Capstone scan + Ghidra `FindOnTransact.java`):

| Строка | File offset | Code xref |
|---|---|---|
| `com.xiaomi.vlive.IMyBinderService` | `0xfe533` | `0x43f900` in `0x43f8b4` |
| `libvc.so` | `0x102960` | `0x43f204` |
| `libvc++.so` | `0x1177d8` | `0x43f1dc` |

---

## 8. Артефакты (локально, в .gitignore)

| Путь | Содержимое |
|---|---|
| `re-workspace/ghidra/icecam_native.rep/` | Ghidra project DB |
| `re-workspace/exports/*.c` | **22 файла** декомпиляции (~120 KB суммарно) |
| `re-workspace/logs/export_batch.log` | журнал batch-экспорта |
| `re-workspace/r2/vcplax_deep/` | r2 xref/disasm |

### Регенерация экспорта

```bash
# после setup-re-env.sh
bash tools/ghidra/export_batch.sh
# или одиночный:
tools/ghidra_11.3.1_PUBLIC/support/analyzeHeadless \
  re-workspace/ghidra icecam_native -process vcplax.so -noanalysis \
  -scriptPath tools/ghidra \
  -postScript DecompileAt.java 0x43f8b4 onTransact vcplax.so_onTransact
```

---

## 9. Выводы

1. **vcplax.so полностью импортирован и проанализирован** в Ghidra (30k+ функций, включая статический FFmpeg).
2. **Восстановлен полный `BBinder::onTransact`** с маппингом Java TX 11–25 на native handlers.
3. **TX24** — запись 4 float + int color в media context; связана с `%8ld: Replaced %5d blocks by color %X`.
4. **Startup** — classic Android Binder service: `addService`, `dlopen(libvc)`, anti-forensics `unlink(self)`.
5. **libvc** — 5× `shadowhook_hook_sym_name` с XOR-строками; требует dynamic trace для имён символов.
6. Адресная модель: **всегда добавлять `0x100000`** при переходе file VA → Ghidra VA для arm64 PIE.

---

## 10. Скрипты (в git)

| Файл | Назначение |
|---|---|
| `tools/ghidra/DecompileAt.java` | экспорт C с auto imageBase |
| `tools/ghidra/DumpMemoryAndResolve.java` | memory map + resolve |
| `tools/ghidra/FindOnTransact.java` | xref IMyBinderService + large fns |
| `tools/ghidra/export_batch.sh` | batch export всех TX |
| `tools/r2/deep_vcplax.sh` | r2 deep scan |

См. также: `docs/NATIVE_REVERSE_MAX.md`, `docs/APK_FULL_REVERSE_ENGINEERING.md`
