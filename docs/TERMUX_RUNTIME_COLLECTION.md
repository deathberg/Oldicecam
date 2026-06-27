# Сбор данных с телефона (Termux + root)

Если нет ПК — можно собрать runtime-данные на устройстве и прислать архив агенту или в GitHub Issue.

## Что понадобится

- Termux (уже есть)
- Root (уже есть)
- Приложение установлено и **хотя бы раз запущено** (чтобы `/data/vcplax` появился)

## Шаг 1 — базовый сбор (без Frida)

В Termux:

```bash
pkg update && pkg install -y tsu termux-tools

# скачать скрипт (или скопировать tools/termux/collect_runtime.sh вручную)
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/collect_runtime.sh
chmod +x collect_runtime.sh

# 1) открой приложение, дождись что камера/сервис работает
# 2) запусти сбор:
./collect_runtime.sh
```

Скрипт создаст архив вида `~/icecam_runtime_YYYYMMDD_HHMMSS.tar.gz`.

Скопируй в Download:

```bash
cp ~/icecam_runtime_*.tar.gz /sdcard/Download/
```

**Как прислать:** Telegram / Google Drive / вложение в GitHub Issue / любой файлообменник.  
Можно также вставить в чат **текстом** содержимое маленьких файлов: `device.txt`, `vcplax_cmdline.txt`, `service_list_grep.txt`.

## Шаг 2 — Frida (самое ценное для libvc XOR)

Нужно, чтобы раскрыть **реальные имена** hook-символов в `libvc.so`.

```bash
pkg install -y python
pip install frida-tools

# frida-server (версия = frida --version на телефоне)
# скачай frida-server-*-android-arm64.xz с GitHub releases frida
# распакуй в Termux и запусти от root:
tsu -c "./frida-server -D &"
```

Запусти приложение, найди процесс:

```bash
tsu -c "pidof vcplax"
```

Hook (скачай `frida_hook_libvc.js` из `tools/termux/`):

```bash
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_hook_libvc.js

frida -U -n vcplax -l frida_hook_libvc.js -o ~/libvc_hooks.log
```

В приложении: смени источник, включи трансform/TX24, перезапусти сервис.  
Ctrl+C → снова `./collect_runtime.sh` (подхватит `libvc_hooks.log`).

## Что особенно полезно прислать

| Приоритет | Файл / данные | Зачем |
|---:|---|---|
| ★★★ | `deployed/vcplax`, `libvc.so`, `libvc++.so` | runtime-бинарники с устройства |
| ★★★ | `libvc_hooks.log` (Frida) | имена hook-символов |
| ★★☆ | `vcplax_cmdline.txt` | **ServerName** (аргумент `/data/vcplax`) |
| ★★☆ | `vcplax_maps.txt` | куда mapped libs |
| ★★☆ | `service_list_grep.txt` | зарегистрирован ли Binder |
| ★☆☆ | `logcat_grep.txt` | ошибки native |
| ★☆☆ | `apk/*.apk` | installed split APK |

## Быстрые команды без скрипта

```bash
# ServerName vcplax
tsu -c "tr '\\0' ' ' < /proc/$(pidof vcplax)/cmdline; echo"

# Список сервисов
tsu -c "service list" | grep -i vlive

# Копия native
tsu -c "cp /data/vcplax /sdcard/Download/vcplax"
tsu -c "cp /data/libvc.so /sdcard/Download/libvc.so"
```

## Если Frida не ставится

Пришли хотя бы **шаг 1** — этого уже хватит для:
- сравнения `/data/vcplax` с APK
- `ServerName`
- logcat + maps
- проверки Binder

Frida можно добавить позже.

## Конфиденциальность

В архиве могут быть пути, логи, имена процессов. Если нужно — удали `pm_dump.txt` и `logcat_tail.txt` перед отправкой.
