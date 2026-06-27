# Reverse-engineering workspace

Локальное окружение для дожатия native RE `testicecam2.apk`.

## Быстрый старт

```bash
bash tools/setup-re-env.sh      # один раз: deps + extract APK
bash tools/re-analyze.sh        # LIEF/Capstone отчёты (~30s)
bash tools/r2/analyze_native.sh # radare2 функции/строки (~2-5 min)
bash tools/ghidra/analyze_native.sh  # Ghidra headless (vcplax ~30-60 min)
```

## Структура

```text
re-workspace/
  libs/          -> symlink decompiled/raw/lib
  decompiled/    -> symlink decompiled/
  ghidra/        # проект Ghidra (icecam_native) — большой, в .gitignore
  r2/            # вывод radare2 (info, afl, strings, hits)
  exports/       # сюда складывать декompil из Ghidra
  logs/          # ghidra_analyze.log
```

## Установленные инструменты

| Tool | Path / command |
|---|---|
| jadx 1.5.1 | `tools/bin/jadx` |
| apktool 2.11.1 | `java -jar tools/apktool.jar` |
| Ghidra 11.3.1 | `tools/ghidra_11.3.1_PUBLIC/support/analyzeHeadless` |
| radare2 5.9.8 | `r2`, `radare2` |
| LIEF, Capstone | `python3` |
| aarch64 objdump | `aarch64-linux-gnu-objdump` |
| armhf objdump | `arm-linux-gnueabihf-objdump` |

## Ghidra — статус (arm64 vcplax)

**Импорт: УСПЕХ** (`re-workspace/logs/ghidra_vcplax_arm64.console`, анализ 249s, 30941 функций).

| Параметр | Значение |
|---|---|
| Image base | `0x100000` — file VA + 0x100000 = Ghidra VA |
| onTransact | **`0x43f8b4`** (не 0x43f8fc — это mid-function) |
| Экспорт C | `bash tools/ghidra/export_batch.sh` → `re-workspace/exports/` |

Финальный отчёт с псевдокодом: **`docs/NATIVE_FINAL_DECOMPILE.md`**

## Ghidra — bookmarks (устаревшие адреса TX — см. финальный отчёт)

| Bookmark | Address | Meaning |
|---|---|---|
| entry | `0x43e880` | PIE entry |
| binder | `0xfe533` | `IMyBinderService` rodata |
| tx14 | `0x43fa08` | setMode |
| tx24 | `0x444024` | transform/color |
| tx22 | `0x46279c` | seek µs |
| tx25 | `0x4456c0` | hard recovery |

Открыть GUI (если есть дисплей):

```bash
tools/ghidra_11.3.1_PUBLIC/ghidraRun
# File → Open Project → re-workspace/ghidra/icecam_native
```

Headless export decompile (после analyze):

```bash
bash tools/ghidra/export_decomp.sh
# или вручную:
GH=tools/ghidra_11.3.1_PUBLIC/support/analyzeHeadless
$GH re-workspace/ghidra icecam_native -process vcplax.so \
  -scriptPath tools/ghidra -postScript DecompileAt.java 0x444024 tx24
```

## radare2 — быстрые команды

```bash
r2 -A re-workspace/libs/arm64-v8a/libvc.so
# внутри r2:
#   afl~shadow    — функции с shadow в имени
#   iz~IMyBinder  — строки
#   s 0x444024; pdf — disasm (vcplax)
```

## Python scripts

- `tools/native_deep_analyze.py` — JSON + `docs/native_analysis/NATIVE_DEEP_ANALYSIS.md`
- `tools/native_xref_scan.py` — ADRP xrefs, TX #11-25 addresses

## Следующие шаги (чеклист)

1. [ ] Ghidra: decompile `vcplax` @ `0x444024` (TX24) → `bash tools/ghidra/export_decomp.sh`
2. [x] r2: `shadowhook_hook_sym_name` callers → `sym.init` @ `0x777fc`, `0x77834` (libvc arm64)
3. [ ] Ghidra vcplax import — см. `re-workspace/logs/ghidra_vcplax_arm64.console` (фон)
4. [ ] Runtime (устройство): `strace -f /data/vcplax test_service 2>&1 | tee re-workspace/logs/strace.txt`

## Документация

- [docs/NATIVE_REVERSE_MAX.md](../docs/NATIVE_REVERSE_MAX.md)
- [docs/APK_FULL_REVERSE_ENGINEERING.md](../docs/APK_FULL_REVERSE_ENGINEERING.md)
