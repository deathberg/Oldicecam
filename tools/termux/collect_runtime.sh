#!/data/data/com.termux/files/usr/bin/bash
# Collect runtime RE artifacts from rooted phone (Termux).
# Usage (in Termux):
#   curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/collect_runtime.sh
#   chmod +x collect_runtime.sh
#   ./collect_runtime.sh
#
# Or copy this file manually into Termux $HOME.
set -uo pipefail

OUT="${HOME}/icecam_runtime_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$OUT"

log() { echo "[collect] $*"; }

run_root() {
  if command -v tsu >/dev/null 2>&1; then
    tsu -c "$1" 2>/dev/null
  else
    su -c "$1" 2>/dev/null
  fi
}

log "output -> $OUT"

{
  echo "=== device ==="
  date
  getprop ro.product.model
  getprop ro.product.cpu.abi
  getprop ro.build.version.release
  getprop ro.build.version.sdk
  id
} >"$OUT/device.txt"

log "finding app package..."
pm list packages 2>/dev/null | grep -iE 'potplayer|vlive|icecam|camera' >"$OUT/packages_grep.txt" || true

# Known obfuscated package from RE; fallback to user env
PKG="${ICECAM_PKG:-com.potplayer.music}"
if ! pm path "$PKG" >/dev/null 2>&1; then
  PKG="$(pm list packages 2>/dev/null | grep -i potplayer | head -1 | cut -d: -f2)"
fi
echo "$PKG" >"$OUT/package.txt"
log "package=$PKG"

pm path "$PKG" >"$OUT/apk_paths.txt" 2>&1 || true
pm dump "$PKG" >"$OUT/pm_dump.txt" 2>&1 || true

# Pull APK(s)
mkdir -p "$OUT/apk"
while IFS= read -r line; do
  apk="${line#package:}"
  base="$(basename "$apk")"
  run_root "cp '$apk' '/data/local/tmp/$base'" && \
    run_root "chmod 644 '/data/local/tmp/$base'" && \
    cp "/data/local/tmp/$base" "$OUT/apk/$base" 2>/dev/null || \
    run_root "cat '$apk'" >"$OUT/apk/$base" 2>/dev/null || true
done <"$OUT/apk_paths.txt"

log "copying deployed natives..."
mkdir -p "$OUT/deployed"
for f in /data/vcplax /data/libvc.so /data/libvc++.so; do
  b="$(basename "$f")"
  run_root "test -r '$f'" && run_root "cat '$f'" >"$OUT/deployed/$b" 2>/dev/null && \
    log "  ok $f ($(wc -c <"$OUT/deployed/$b") bytes)" || \
    echo "MISSING $f" >>"$OUT/deployed/MISSING.txt"
done

for d in /data/camera /data/local/tmp; do
  run_root "ls -laR '$d' 2>/dev/null" >>"$OUT/deployed/ls_data_dirs.txt" || true
done

log "process + maps..."
run_root "ps -A" >"$OUT/ps_all.txt" 2>&1 || true
run_root "ps -A | grep -E 'vcplax|vlive|potplayer'" >"$OUT/ps_target.txt" 2>&1 || true

VC_PID="$(run_root "pidof vcplax" | tr -d '\r' | awk '{print $1}')"
echo "vcplax_pid=$VC_PID" >"$OUT/vcplax_pid.txt"
if [[ -n "$VC_PID" ]]; then
  run_root "cat /proc/$VC_PID/cmdline | tr '\\0' ' '" >"$OUT/vcplax_cmdline.txt" 2>&1 || true
  run_root "cat /proc/$VC_PID/maps" >"$OUT/vcplax_maps.txt" 2>&1 || true
  run_root "ls -la /proc/$VC_PID/fd" >"$OUT/vcplax_fd.txt" 2>&1 || true
  run_root "cat /proc/$VC_PID/status" >"$OUT/vcplax_status.txt" 2>&1 || true
else
  echo "vcplax not running — open the app first, then re-run." >>"$OUT/vcplax_pid.txt"
fi

log "binder / services..."
run_root "service list 2>/dev/null | grep -iE 'vlive|vc|camera|priv'" >"$OUT/service_list_grep.txt" || true
run_root "service list 2>/dev/null" >"$OUT/service_list_full.txt" || true

log "logcat snapshot..."
logcat -d -t 3000 2>/dev/null | grep -iE 'vcplax|vlive|libvc|binder|shadowhook|potplayer|icecam|MediaProjection' \
  >"$OUT/logcat_grep.txt" || true
logcat -d -t 1500 2>/dev/null >"$OUT/logcat_tail.txt" || true

log "file hashes..."
{
  for f in "$OUT/deployed"/*; do
    [[ -f "$f" ]] || continue
    if command -v md5sum >/dev/null 2>&1; then
      md5sum "$f"
    elif command -v md5 >/dev/null 2>&1; then
      md5 -r "$f"
    fi
  done
} >"$OUT/checksums.txt" 2>&1 || true

# Optional Frida hook log (if user ran hook script separately)
if [[ -f "${HOME}/libvc_hooks.log" ]]; then
  cp "${HOME}/libvc_hooks.log" "$OUT/"
fi

ARCHIVE="${OUT}.tar.gz"
tar -czf "$ARCHIVE" -C "$(dirname "$OUT")" "$(basename "$OUT")" 2>/dev/null || \
  tar -cf "${OUT}.tar" -C "$(dirname "$OUT")" "$(basename "$OUT")"

log "DONE"
echo ""
echo "============================================"
echo " Archive: $ARCHIVE"
echo " Size:    $(wc -c <"$ARCHIVE" 2>/dev/null || echo '?') bytes"
echo ""
echo " Send this file to the agent (Telegram/Drive/GitHub issue)."
echo " Or copy to Download:"
echo "   cp '$ARCHIVE' /sdcard/Download/"
echo "============================================"
