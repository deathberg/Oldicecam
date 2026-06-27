# testicecam2.apk — reverse engineering & clone

Отдельная ветка **только** для восстановления исходного приложения `testicecam2.apk` и сборки native-клона.  
Модуль IceCam Pro (`app/`, доработки v4–v28) здесь **отсутствует** — он живёт в ветках приложения (`main`, `cursor/recon-unified-e3a1`).

## Содержимое

| Путь | Назначение |
|------|------------|
| `testicecam2.apk` | Исходный APK для RE |
| `decompiled/` | Индекс + локально генерируемый jadx/apktool/raw (см. INDEX) |
| `reconstructed/original-apk/` | Восстановленный читаемый Java-слой оригинала |
| `re-workspace/` | Ghidra/r2/exports/logs (большие артефакты в .gitignore) |
| `tools/` | jadx/apktool/Ghidra/r2/Frida/Termux скрипты |
| `retool/` | Standalone RE Tool APK (pull, Frida session, runtime capture) |
| `clone/` | CMake: `libvc_clone.so`, inject, media pipeline skeleton |
| `docs/` | RE-отчёты (Binder, native, roadmap) |

## Быстрый старт

```bash
bash tools/setup-re-env.sh      # deps + extract APK
bash tools/re-analyze.sh
gradle :retool:assembleDebug
cmake -S clone -B clone/build && cmake --build clone/build
```

## Ключевые документы

- [docs/RE_FINAL_REPORT.md](docs/RE_FINAL_REPORT.md)
- [docs/CLONE_ROADMAP.md](docs/CLONE_ROADMAP.md)
- [docs/APK_FULL_REVERSE_ENGINEERING.md](docs/APK_FULL_REVERSE_ENGINEERING.md)
- [reconstructed/original-apk/README.md](reconstructed/original-apk/README.md)

## Ветки

- **`cursor/re-testicecam2-e3a1`** (эта ветка) — RE + clone + retool
- **`cursor/recon-unified-e3a1`** — IceCam Pro (наша сборка)
