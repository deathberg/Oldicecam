#!/system/bin/sh
# Comprehensive runtime artifact pull for testicecam2 RE.
# Handles deleted /data/vcplax via /proc/PID/exe and /proc/PID/mem.
set -u

OUT=/sdcard/Download/re_tool
mkdir -p "$OUT/deployed" "$OUT/apk_native" "$OUT/prefs" "$OUT/proc" "$OUT/system"

log() { echo "[pull] $*"; }

TARGET_PKG=""
for PKG in com.potplayer.music com.xiaomi.vlive; do
  if pm path "$PKG" >/dev/null 2>&1; then
    TARGET_PKG="$PKG"
    break
  fi
done

echo "pulled_at=$(date)" > "$OUT/README.txt"
echo "target_pkg=$TARGET_PKG" >> "$OUT/README.txt"

# --- APK natives (always available) ---
if [ -n "$TARGET_PKG" ]; then
  pm path "$TARGET_PKG" > "$OUT/apk_paths.txt" 2>&1 || true
  APK=$(grep '^package:' "$OUT/apk_paths.txt" 2>/dev/null | head -1 | cut -d: -f2)
  echo "apk=$APK" >> "$OUT/README.txt"
  if [ -n "$APK" ] && [ -r "$APK" ]; then
    cp -f "$APK" "$OUT/deployed/target.apk" 2>/dev/null || cat "$APK" > "$OUT/deployed/target.apk"
    for abi in arm64-v8a armeabi-v7a; do
      for lib in vcplax.so libvc.so libshadowhook.so; do
        unzip -p "$APK" "lib/$abi/$lib" > "$OUT/apk_native/${abi}_${lib}" 2>/dev/null && \
          log "apk $abi/$lib $(wc -c < "$OUT/apk_native/${abi}_${lib}") bytes" || true
      done
    done
  fi
  PREFS="/data/data/$TARGET_PKG/shared_prefs/app_config.xml"
  if [ -f "$PREFS" ]; then
    cp -f "$PREFS" "$OUT/prefs/app_config.xml"
    sed -n 's/.*<string name="ServerName">\([^<]*\)<\/string>.*/ServerName=\1/p' "$PREFS" >> "$OUT/README.txt"
  fi
  APP_FILES="/data/data/$TARGET_PKG/files"
  if [ -d "$APP_FILES" ]; then
    find "$APP_FILES" -name '*.so' -o -name 'vcplax*' 2>/dev/null | head -20 > "$OUT/app_files_list.txt"
    find "$APP_FILES" \( -name 'vcplax.so' -o -name 'libvc.so' -o -name 'libshadowhook.so' \) 2>/dev/null | while read -r f; do
      b=$(basename "$f")
      cp -f "$f" "$OUT/deployed/app_files_$b" 2>/dev/null && log "app_files $b" || true
    done
  fi
fi

# --- /data copies (may exist briefly) ---
for f in /data/vcplax /data/libvc.so /data/libvc++.so \
         /data/camera/vcplax /data/camera/libvc.so /data/camera/libshadowhook.so \
         /data/local/tmp/vcplax.real /data/local/tmp/libvc.so.real /data/local/tmp/libvc++.so.real; do
  if [ -r "$f" ]; then
    b=$(basename "$f" | tr '/' '_')
    cp -f "$f" "$OUT/deployed/data_$b" 2>/dev/null || cat "$f" > "$OUT/deployed/data_$b"
    log "file $f -> data_$b ($(wc -c < "$OUT/deployed/data_$b") bytes)"
  else
    echo "MISSING $f" >> "$OUT/deployed/MISSING.txt"
  fi
done

# --- vcplax process dump ---
PID=$(pidof vcplax 2>/dev/null || true)
echo "vcplax_pid=$PID" > "$OUT/vcplax_pid.txt"
if [ -n "$PID" ]; then
  tr '\0' ' ' < "/proc/$PID/cmdline" > "$OUT/vcplax_cmdline.txt"
  cat "/proc/$PID/maps" > "$OUT/vcplax_maps.txt" 2>/dev/null || true
  cat "/proc/$PID/status" > "$OUT/vcplax_status.txt" 2>/dev/null || true
  ls -la "/proc/$PID/fd" > "$OUT/vcplax_fd.txt" 2>/dev/null || true

  # /proc/PID/exe works even when path shows (deleted)
  if [ -r "/proc/$PID/exe" ]; then
    cat "/proc/$PID/exe" > "$OUT/proc/vcplax_exe_dump" 2>/dev/null && \
      log "proc exe dump $(wc -c < "$OUT/proc/vcplax_exe_dump") bytes" || \
      echo "FAIL /proc/$PID/exe" >> "$OUT/deployed/MISSING.txt"
  fi

  # Summarize mapped paths
  awk '{print $1,$2,$6}' "/proc/$PID/maps" 2>/dev/null | grep -iE 'vcplax|libvc|shadow|/data/' \
    > "$OUT/proc/maps_interesting.txt" || true

  # Dump first r-xp vcplax segment header (for verify)
  if [ -r "/proc/$PID/mem" ]; then
    START=$(awk '/vcplax/ && /r-xp/ {print $1; exit}' "/proc/$PID/maps" | cut -d- -f1)
    if [ -n "$START" ]; then
      START_DEC=$((16#$START))
      dd if="/proc/$PID/mem" bs=4096 skip=$((START_DEC / 4096)) count=4 \
        of="$OUT/proc/vcplax_rx_header.bin" 2>/dev/null && \
        log "mem header @0x$START" || echo "FAIL mem dd" >> "$OUT/deployed/MISSING.txt"
    fi
  fi
fi

# --- Binder services ---
service list 2>/dev/null | grep -iE 'vlive|vc|camera|priv|pot|data' > "$OUT/service_list_grep.txt" || true
service list 2>/dev/null > "$OUT/system/service_list_full.txt" || true

# --- Data dirs listing ---
ls -laR /data/camera /data/local/tmp 2>/dev/null > "$OUT/deployed/ls_data_dirs.txt" || true

# --- Capture log copy ---
if [ -f /sdcard/Download/re_tool_capture.log ]; then
  cp -f /sdcard/Download/re_tool_capture.log "$OUT/re_tool_capture_copy.log"
  wc -c "$OUT/re_tool_capture_copy.log" >> "$OUT/README.txt"
fi

du -sh "$OUT" 2>/dev/null >> "$OUT/README.txt" || true
log "DONE $OUT"
echo "PULL_OK dir=$OUT"
