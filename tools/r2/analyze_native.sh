#!/usr/bin/env bash
# radare2 batch analysis: exports, imports, strings, key functions.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$ROOT/re-workspace/r2"
LIBS="$ROOT/decompiled/raw/lib"
mkdir -p "$OUT"

if ! command -v r2 >/dev/null 2>&1; then
  echo "r2 not found. Run: bash tools/setup-re-env.sh" >&2
  exit 1
fi

analyze() {
  local abi="$1" file="$2"
  local path="$LIBS/$abi/$file"
  local base="$OUT/${abi}_${file}"
  [[ -f "$path" ]] || return 0

  echo "=== r2 $abi/$file ==="
  r2 -q -e bin.cache=true -c "
    aaa
    iI > ${base}_info.txt
    ii > ${base}_imports.txt
    iE > ${base}_exports.txt
    iz > ${base}_strings.txt
    afl > ${base}_functions.txt
    pd 80 @ entry0 > ${base}_entry_disasm.txt 2>/dev/null || true
  " "$path" 2>/dev/null || r2 -q -c "aaa; afl" "$path" > "${base}_functions.txt"

  # Tag interesting strings
  rg -i "IMyBinder|libvc|shadowhook|Replaced.*color|ProcessState|enforceInterface" \
    "${base}_strings.txt" > "${base}_hits.txt" 2>/dev/null || true
}

for pair in \
  "arm64-v8a libshadowhook.so" \
  "arm64-v8a libvc.so" \
  "arm64-v8a vcplax.so"; do
  set -- $pair
  analyze "$1" "$2"
done

echo "r2 outputs -> $OUT"
