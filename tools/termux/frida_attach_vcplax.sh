#!/data/data/com.termux/files/usr/bin/bash
# Attach Frida to running vcplax (Binder + TX13 + late XOR attempt).
# Usage:
#   bash frida_attach_vcplax.sh
set -euo pipefail

FRIDA_INJECT="${FRIDA_INJECT:-/data/local/tmp/frida-inject}"
SCRIPT="${SCRIPT:-/data/local/tmp/frida_hook_libvc.js}"
LOG="${LOG:-/data/local/tmp/libvc_symbols.log}"

die() { echo "[ERR] $*" >&2; exit 1; }

[[ -x "$FRIDA_INJECT" ]] || die "Missing $FRIDA_INJECT"
[[ -f "$SCRIPT" ]] || die "Missing $SCRIPT"

su -c "setenforce 0" 2>/dev/null || true

PID="$(pidof vcplax 2>/dev/null || true)"
if [[ -z "$PID" ]]; then
  echo "[*] vcplax not running — start com.potplayer.music first, wait 5s..."
  sleep 5
  PID="$(pidof vcplax 2>/dev/null || true)"
fi
[[ -n "$PID" ]] || die "vcplax still not found. Open app, then retry."

echo "[*] attach pid=$PID -> $LOG"
exec "$FRIDA_INJECT" -p "$PID" --runtime=qjs -s "$SCRIPT" 2>&1 | tee "$LOG"
