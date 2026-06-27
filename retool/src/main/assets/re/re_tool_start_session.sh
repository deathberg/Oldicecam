#!/system/bin/sh
# Auto capture session: watchdog + force-stop + launch target app.
# Invoked as root from RE Tool (ReToolEngine.startAutoSession).
set -u

DIR=/data/local/tmp
FLAG="$DIR/re_tool_session_active"
LOG=/sdcard/Download/re_tool_capture.log
WATCHDOG="$DIR/re_tool_watchdog.sh"
FRIDA_SERVER="$DIR/frida-server"

TARGET_PKG=""
for PKG in com.potplayer.music com.xiaomi.vlive; do
  if pm path "$PKG" >/dev/null 2>&1; then
    TARGET_PKG="$PKG"
    break
  fi
done

if [ -z "$TARGET_PKG" ]; then
  echo "ERR_NO_TARGET_APK"
  exit 10
fi

PREFS="/data/data/$TARGET_PKG/shared_prefs/app_config.xml"
SERVER=""

if [ -f "$PREFS" ]; then
  SERVER=$(sed -n 's/.*<string name="ServerName">\([^<]*\)<\/string>.*/\1/p' "$PREFS" | head -1)
fi

if [ -z "$SERVER" ] && pidof vcplax >/dev/null 2>&1; then
  SERVER=$(tr '\0' '\n' < "/proc/$(pidof vcplax)/cmdline" | sed -n '2p')
fi

if [ -z "$SERVER" ]; then
  echo "WARN_NO_SERVER_NAME will read after app boot"
fi

setenforce 0 2>/dev/null || true

# Stop previous session
rm -f "$FLAG"
pkill -f re_tool_watchdog.sh 2>/dev/null || true
pkill -f frida-inject 2>/dev/null || true
pkill frida-server 2>/dev/null || true
killall vcplax 2>/dev/null || true
sleep 1

am force-stop "$TARGET_PKG" 2>/dev/null || true
sleep 0.5

mkdir -p /sdcard/Download
: > "$LOG"
echo "CAPTURE_AUTO_SESSION_START" >> "$LOG"
echo "target_pkg=$TARGET_PKG" >> "$LOG"
echo "server_name=$SERVER" >> "$LOG"
date >> "$LOG"

if [ ! -x "$FRIDA_SERVER" ] || [ ! -x "$DIR/frida-inject" ]; then
  echo "ERR_FRIDA_MISSING run Setup first"
  exit 11
fi

if [ ! -f "$DIR/frida_hook_libvc.js" ]; then
  echo "ERR_SCRIPT_MISSING run Setup first"
  exit 12
fi

nohup "$FRIDA_SERVER" -D >"$DIR/frida-server.log" 2>&1 &
sleep 2

if [ -x /data/vcplax ]; then
  cp -f /data/vcplax "$DIR/vcplax.real" 2>/dev/null || true
  ls -l "$DIR/vcplax.real" 2>/dev/null || true
fi

touch "$FLAG"
chmod 755 "$WATCHDOG"
nohup sh "$WATCHDOG" >"$DIR/watchdog.log" 2>&1 &
echo "watchdog_pid=$!"
sleep 0.3

echo "launching $TARGET_PKG ..."
am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p "$TARGET_PKG" 2>&1

# Refresh server name after app may have created prefs
sleep 2
if [ -f "$PREFS" ]; then
  SERVER2=$(sed -n 's/.*<string name="ServerName">\([^<]*\)<\/string>.*/\1/p' "$PREFS" | head -1)
  if [ -n "$SERVER2" ]; then
    echo "server_name_after_launch=$SERVER2" >> "$LOG"
  fi
fi

echo "vcplax_pid=$(pidof vcplax 2>/dev/null || echo none)"
echo "SESSION_OK use target app then Stop+Share in RE Tool"
