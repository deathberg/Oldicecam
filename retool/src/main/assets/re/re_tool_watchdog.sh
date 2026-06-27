#!/system/bin/sh
# Early-inject watchdog: polls vcplax every 20ms and injects Frida on first sight.
DIR=/data/local/tmp
FLAG="$DIR/re_tool_session_active"
LOG=/sdcard/Download/re_tool_capture.log
SCRIPT="$DIR/frida_hook_libvc.js"
INJECT="$DIR/frida-inject"
MARKER="$DIR/re_tool_injected_pids"

: > "$MARKER"

log() {
  echo "[watchdog] $*" >> "$LOG"
}

log "started pid=$$"

while [ -f "$FLAG" ]; do
  # Snapshot binaries before vcplax self-deletes (race window)
  for pair in "/data/vcplax:$DIR/vcplax.real" "/data/libvc.so:$DIR/libvc.so.real" "/data/libvc++.so:$DIR/libvc++.so.real"; do
    SRC="${pair%%:*}"
    DST="${pair#*:}"
    if [ -r "$SRC" ] && [ ! -s "$DST" ]; then
      cp -f "$SRC" "$DST" 2>/dev/null && log "saved $SRC -> $DST"
    fi
  done

  for PID in $(pidof vcplax 2>/dev/null); do
    if ! grep -qx "$PID" "$MARKER" 2>/dev/null; then
      echo "$PID" >> "$MARKER"
      log "NEW vcplax pid=$PID -> frida-inject"
      echo "CAPTURE_WATCHDOG_INJECT pid=$PID" >> "$LOG"
      nohup "$INJECT" -p "$PID" --runtime=qjs -s "$SCRIPT" >> "$LOG" 2>&1 &
    fi
  done
  sleep 0.02
done

log "exit"
