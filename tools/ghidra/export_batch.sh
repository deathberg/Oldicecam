#!/usr/bin/env bash
# Sequential Ghidra decompile export (no pipefail issues).
set -uo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
GH="$ROOT/tools/ghidra_11.3.1_PUBLIC/support/analyzeHeadless"
PROJECT="$ROOT/re-workspace/ghidra"
SCRIPT="$ROOT/tools/ghidra"
EXPORT="$ROOT/re-workspace/exports"
LOG="$ROOT/re-workspace/logs/export_batch.log"
mkdir -p "$EXPORT" "$(dirname "$LOG")"

decompile() {
  local bin="$1" addr="$2" label="$3" base="${4:-${bin%.so}_${label}}"
  echo ">>> $bin @ $addr ($label)" | tee -a "$LOG"
  rm -f "$ROOT/re-workspace/ghidra/icecam_native.lock" "$ROOT/re-workspace/ghidra/icecam_native.lock~"
  if "$GH" "$PROJECT" icecam_native -process "$bin" -noanalysis \
      -scriptPath "$SCRIPT" \
      -postScript DecompileAt.java "$addr" "$label" "$base" \
      >>"$LOG" 2>&1; then
    if [[ -f "$EXPORT/${base}.c" ]]; then
      echo "OK $base.c ($(wc -c < "$EXPORT/${base}.c") bytes)" | tee -a "$LOG"
    else
      echo "FAIL missing $base.c" | tee -a "$LOG"
    fi
  else
    echo "FAIL ghidra exit for $base" | tee -a "$LOG"
  fi
}

: >"$LOG"

echo "=== vcplax startup ===" | tee -a "$LOG"
decompile vcplax.so 0x43e880 entry_main vcplax.so_entry_main
decompile vcplax.so 0x43f8b4 binder_ontransact vcplax.so_binder_ontransact
decompile vcplax.so 0x43f8fc binder_descriptor_setup vcplax.so_binder_descriptor
decompile vcplax.so 0x43f204 dlopen_libvc vcplax.so_dlopen_libvc
decompile vcplax.so 0x43eb00 argv_parse vcplax.so_argv_parse

echo "=== vcplax TX handlers (from onTransact switch) ===" | tee -a "$LOG"
decompile vcplax.so 0x43fedc tx11_play_source vcplax.so_tx11_play
decompile vcplax.so 0x4402b0 tx12_stop_query vcplax.so_tx12_stop
decompile vcplax.so 0x440324 tx13_poll_state vcplax.so_tx13_poll
decompile vcplax.so 0x4403d4 tx14_setmode vcplax.so_tx14_setmode
decompile vcplax.so 0x440624 tx20_unknown vcplax.so_tx20
decompile vcplax.so 0x440908 tx21_unknown vcplax.so_tx21
decompile vcplax.so 0x440ad0 tx23_unknown vcplax.so_tx23
decompile vcplax.so 0x440c98 tx26_unknown vcplax.so_tx26
decompile vcplax.so 0x440e38 tx27_unknown vcplax.so_tx27
decompile vcplax.so 0x441f7c tx15_seek_helper vcplax.so_tx15_seek_helper
decompile vcplax.so 0x447354 tx22_large vcplax.so_tx22_large

echo "=== libvc hooks ===" | tee -a "$LOG"
decompile libvc.so 0x777fc init_hook_1 libvc.so_init_hook_1
decompile libvc.so 0x77834 init_hook_2 libvc.so_init_hook_2
decompile libvc.so 0x754b0 libvc_entry libvc.so_entry

echo "=== libshadowhook ===" | tee -a "$LOG"
decompile libshadowhook.so 0x6e7c shadowhook_jni libshadowhook.so_jni_onload

echo "BATCH_DONE" | tee -a "$LOG"
