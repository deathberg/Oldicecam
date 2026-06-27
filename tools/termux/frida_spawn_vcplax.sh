#!/data/data/com.termux/files/usr/bin/bash
# Spawn vcplax under Frida BEFORE libvc init — required for HOOK_SYM / XOR capture.
#
# Usage (Termux, root):
#   tsu -c 'setenforce 0; bash /data/local/tmp/frida_spawn_vcplax.sh'
#
# Note: frida-inject on Termux does NOT support --no-pause (removed in v4).
set -euo pipefail

VCPLAX="${VCPLAX:-/data/vcplax}"
SERVICE_NAME="${SERVICE_NAME:-vlive}"
FRIDA_INJECT="${FRIDA_INJECT:-/data/local/tmp/frida-inject}"
SCRIPT="${SCRIPT:-/data/local/tmp/frida_hook_libvc.js}"
LOG="${LOG:-/data/local/tmp/libvc_symbols.log}"

die() { echo "[ERR] $*" >&2; exit 1; }

[[ -x "$FRIDA_INJECT" ]] || die "Missing frida-inject at $FRIDA_INJECT"
[[ -f "$SCRIPT" ]] || die "Missing $SCRIPT"
[[ -x "$VCPLAX" ]] || die "Missing vcplax at $VCPLAX — launch app once to deploy"

# Reject curl 404 artifacts
if head -1 "$SCRIPT" 2>/dev/null | grep -q '404'; then
  die "$SCRIPT looks like a 404 page — re-download frida_hook_libvc.js"
fi
if head -1 "$0" 2>/dev/null | grep -q '404'; then
  die "spawn script is corrupt — re-download frida_spawn_vcplax.sh"
fi

echo "[*] SELinux permissive (ignore error if already set)"
su -c "setenforce 0" 2>/dev/null || true

echo "[*] stopping old vcplax"
pidof vcplax 2>/dev/null | xargs -r kill -9 || true
sleep 1

# frida-inject spawn: -f executable, MUST use --runtime=qjs for native daemon
echo "[*] spawn: $FRIDA_INJECT -f $VCPLAX --runtime=qjs -s $SCRIPT -- $VCPLAX $SERVICE_NAME"
echo "[*] log -> $LOG"
echo "[*] then open com.potplayer.music in another window"

exec "$FRIDA_INJECT" -f "$VCPLAX" --runtime=qjs -s "$SCRIPT" -- \
  "$VCPLAX" "$SERVICE_NAME" 2>&1 | tee "$LOG"
