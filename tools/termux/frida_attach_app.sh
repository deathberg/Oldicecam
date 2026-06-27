#!/data/data/com.termux/files/usr/bin/bash
# Attach Frida to com.potplayer.music (passive app profile — polls, no Java hooks).
# For Binder/libvc capture use frida_attach_vcplax.sh instead.
set -euo pipefail

FRIDA_INJECT="${FRIDA_INJECT:-/data/local/tmp/frida-inject}"
SCRIPT="${SCRIPT:-/data/local/tmp/frida_hook_libvc.js}"
LOG="${LOG:-/data/local/tmp/app_passive.log}"

die() { echo "[ERR] $*" >&2; exit 1; }

[[ -x "$FRIDA_INJECT" ]] || die "Missing $FRIDA_INJECT"
[[ -f "$SCRIPT" ]] || die "Missing $SCRIPT"

PID="$(pidof com.potplayer.music 2>/dev/null || true)"
[[ -n "$PID" ]] || die "App not running. am start -n com.potplayer.music/com.potplayer.music.MainActivity"

echo "[*] attach app pid=$PID (passive profile)"
echo "[*] MUST use --runtime=qjs to avoid java-bridge invalid address"
exec "$FRIDA_INJECT" -p "$PID" --runtime=qjs -s "$SCRIPT" 2>&1 | tee "$LOG"
