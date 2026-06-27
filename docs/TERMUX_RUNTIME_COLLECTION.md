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
**Никогда** не используй `Java.perform`. Для **любого** `frida-inject` обязателен **`--runtime=qjs`**, иначе `invalid address` (java-bridge / patchCode).

| Процесс | Что ловим | Команда |
|---|---|---|
| **vcplax** | Binder TX, HOOK_SYM, XOR, TX13 | `frida_attach_vcplax.sh` или spawn |
| **com.potplayer.music** | только passive dlopen + подсказки | `frida_attach_app.sh` |

```bash
# SELinux
su -c "setenforce 0"

# frida-server (root)
su -c "pkill frida-server; /data/local/tmp/frida-server -D &"
sleep 2

# скачать скрипты (проверь что не 404: head -1 file)
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_hook_libvc.js
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_spawn_vcplax.sh
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_attach_vcplax.sh
curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_attach_app.sh
chmod +x frida_*.sh
cp frida_hook_libvc.js frida_*.sh /data/local/tmp/

# A) Spawn vcplax (лучше для HOOK_SYM — libvc ещё не загружена)
#    frida-inject НЕ поддерживает --no-pause!
/data/local/tmp/frida_spawn_vcplax.sh

# B) Attach к уже запущенному vcplax
/data/local/tmp/frida_attach_vcplax.sh

# C) Attach к app (passive, без Binder)
/data/local/tmp/frida_attach_app.sh
```

Поиграй 30–60 сек, **Ctrl+C**, проверь лог:

```bash
grep -E 'HOOK_SYM|XOR_STATIC|POLL_STATE|TX seq=' /data/local/tmp/libvc_symbols.log
```

Ожидаемые строки:

```
[OK] hook installed: onTransact @ ...
[HOOK_SYM] lib="libui.so" sym="_ZN7android13GraphicBuffer..."
[POLL_STATE] #42 120,3599,0,0,51200
```

### Spawn vcplax (для HOOK_SYM / XOR — libvc ещё не загружена)

Attach к уже запущенному `vcplax` **не ловит** `shadowhook_hook_sym_name`. Нужен spawn через `frida_spawn_vcplax.sh` (без `--no-pause`).

```bash
cp frida_hook_libvc.js frida_spawn_vcplax.sh /data/local/tmp/
chmod +x /data/local/tmp/frida_spawn_vcplax.sh
tsu -c 'setenforce 0; /data/local/tmp/frida_spawn_vcplax.sh'
grep -E 'HOOK_SYM|XOR_STATIC|POLL_STATE' /data/local/tmp/libvc_symbols.log
```

### Частые ошибки

| Ошибка | Решение |
|---|---|
| `invalid address` / java-bridge | **Всегда** `--runtime=qjs`. Не inject в app без qjs |
| `Unknown option --no-pause` | Убери `--no-pause` — Termux frida-inject не поддерживает |
| `404: Not Found` в скрипте | `curl` скачал HTML-ошибку — `head -1 script.sh`, перекачай |
| `Cannot parse integer ?? for -p` | `PID` пустой — `pidof vcplax` после запуска app |
| `Process not found` | Запусти app, подожди 5s, `pidof vcplax` |
| frida-server Aborted | Запускай server от root: `su -c '/data/local/tmp/frida-server -D &'` |
| HOOK_SYM пустой при attach | libvc уже loaded — используй `frida_spawn_vcplax.sh` |

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
