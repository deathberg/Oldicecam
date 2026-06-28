#!/data/data/com.termux/files/usr/bin/bash
# Spawn vcplax under Frida BEFORE libvc init — required for HOOK_SYM / XOR capture.
# Usage (Termux, root):
#   tsu -c 'setenforce 0; bash /data/local/tmp/frida_spawn_vcplax.sh'
set -euo pipefail

VCPLAX="${VCPLAX:-/data/vcplax}"
SERVICE_NAME="${SERVICE_NAME:-vlive}"
FRIDA_INJECT="${FRIDA_INJECT:-/data/local/tmp/frida-inject}"
SCRIPT="${SCRIPT:-/data/local/tmp/frida_hook_libvc.js}"
LOG="${LOG:-/data/local/tmp/libvc_symbols.log}"

if [[ ! -x "$FRIDA_INJECT" ]]; then
  echo "Missing frida-inject at $FRIDA_INJECT" >&2
  exit 1
fi
if [[ ! -f "$SCRIPT" ]]; then
  echo "Missing $SCRIPT — copy app/src/main/assets/re/frida_hook_libvc.js to /data/local/tmp/" >&2
  exit 1
fi

echo "[*] stopping old vcplax"
pidof vcplax 2>/dev/null | xargs -r kill -9 || true
sleep 1

echo "[*] spawn: $FRIDA_INJECT -f $VCPLAX --runtime=qjs -s $SCRIPT -- $SERVICE_NAME"
# --no-pause: run immediately; dlopen hooks install before libvc loads
exec "$FRIDA_INJECT" -f "$VCPLAX" --runtime=qjs -s "$SCRIPT" --no-pause -- \
  "$VCPLAX" "$SERVICE_NAME" 2>&1 | tee "$LOG"
