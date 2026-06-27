#!/usr/bin/env bash
# Export Ghidra decompilation for known IceCam addresses into re-workspace/exports/
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
GH="$ROOT/tools/ghidra_11.3.1_PUBLIC/support/analyzeHeadless"
PROJECT="$ROOT/re-workspace/ghidra"
EXPORT="$ROOT/re-workspace/exports"
SCRIPT="$ROOT/tools/ghidra"
mkdir -p "$EXPORT"

decompile() {
  local addr="$1" label="$2" bin="$3"
  echo "Decompiling $bin @ $addr ($label) ..."
  "$GH" "$PROJECT" icecam_native \
    -process "$bin" \
    -scriptPath "$SCRIPT" \
    -postScript DecompileAt.java "$addr" "$label" "${bin%.so}_${label}" \
    2>"$EXPORT/${bin}_${label}.ghidra.log" | tail -3
}

if [[ -f "$PROJECT/icecam_native.lock" ]]; then
  echo "WARN: Ghidra project may be locked (vcplax import running?)."
  echo "      Wait for: tail -f re-workspace/logs/ghidra_vcplax_arm64.console"
  echo "      Or stop tmux session ghidra-vcplax before export."
fi

# Requires vcplax.so already analyzed in Ghidra project
decompile "0x444024" "tx24_transform" "vcplax.so"
decompile "0x46279c" "tx22_seek" "vcplax.so"
decompile "0x4456c0" "tx25_recovery" "vcplax.so"
decompile "0x43fa08" "tx14_setmode" "vcplax.so"

# libvc init installs hooks via shadowhook (r2: sym.init @ 0x777fc, 0x77834)
decompile "0x777fc" "init_hook_1" "libvc.so"
decompile "0x77834" "init_hook_2" "libvc.so"

echo "Exports in $EXPORT"
