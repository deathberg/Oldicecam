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

**Не используй `pip install frida-tools`** — на Termux Python 3.13 сборка падает (как в твоём логе).

### Авто-установка (рекомендуется)

```bash
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/setup_frida.sh
chmod +x setup_frida.sh
./setup_frida.sh
```

### Ручная установка

```bash
pkg update && pkg install -y root-repo tsu wget xz which
pkg install -y frida frida-python

frida --version          # запомни версию, напр. 16.5.9
getprop ro.product.cpu.abi   # arm64-v8a

# скачай frida-server ТОЙ ЖЕ версии (пример для 16.5.9 arm64):
cd ~
wget https://github.com/frida/frida/releases/download/16.5.9/frida-server-16.5.9-android-arm64.xz
xz -d frida-server-16.5.9-android-arm64.xz
mv frida-server-16.5.9-android-arm64 frida-server
chmod 755 frida-server

tsu -c "cp ~/frida-server /data/local/tmp/frida-server && chmod 755 /data/local/tmp/frida-server"
tsu -c "pkill frida-server; /data/local/tmp/frida-server -D &"
sleep 2
frida-ps -U
```

### Hook vcplax (native-only daemon — NO Java)

**Важно:** `vcplax` — нативный C/C++ процесс без ART/JVM.  
Не используй `Java.perform`. Для `frida-inject` обязателен **`--runtime=qjs`**, иначе internal-agent падает с `invalid address` (java-bridge).

```bash
# SELinux (частая причина invalid address)
tsu -c "setenforce 0"

# frida-server от root
tsu -c "pkill frida-server; /data/local/tmp/frida-server -D &"
sleep 2

# приложение открыто!
PID=$(tsu -c "pidof vcplax")
echo "PID=$PID"

curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_hook_libvc.js

# способ A — frida CLI (рекомендуется)
frida -U -p "$PID" -l frida_hook_libvc.js -o ~/libvc_hooks.log --runtime=qjs

# способ B — frida-inject на устройстве
cp ~/frida_hook_libvc.js /data/local/tmp/
/data/local/tmp/frida-inject -p "$PID" \
  -s /data/local/tmp/frida_hook_libvc.js --runtime=qjs \
  > /data/local/tmp/libvc_hooks.log
```

Поиграй в приложении 30–60 сек, **Ctrl+C**, проверь лог:

```bash
grep -E 'HOOK_SYM|BINDER_TX|shadowhook_init|dlopen' ~/libvc_hooks.log /data/local/tmp/libvc_hooks.log 2>/dev/null
cp ~/libvc_hooks.log /sdcard/Download/ 2>/dev/null
cp /data/local/tmp/libvc_hooks.log /sdcard/Download/ 2>/dev/null
```

Ожидаемые строки:

```
HOOK_SYM lib=libui.so sym=_ZN7android13GraphicBuffer... replace=0x...
BINDER_TX code=11 (0xb)
```

### Частые ошибки

| Ошибка | Решение |
|---|---|
| `pip` / `Building wheel for frida` | Не pip! Только `pkg install frida frida-python` |
| `Unable to connect` | `tsu -c "/data/local/tmp/frida-server -D &"` |
| `Process not found` | Сначала открой приложение, проверь `pidof vcplax` |
| `version mismatch` | Версия server = `frida --version` |
| `which: not found` при pip | `pkg install which` (но pip всё равно не нужен) |

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
