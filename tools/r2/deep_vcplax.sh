#!/usr/bin/env bash
# Deep radare2 analysis for vcplax arm64.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BIN="$ROOT/decompiled/raw/lib/arm64-v8a/vcplax.so"
OUT="$ROOT/re-workspace/r2/vcplax_deep"
mkdir -p "$OUT"

r2cmd() {
  r2 -q -B 0x100000 -e bin.cache=true -c "$1" "$BIN" 2>/dev/null
}

echo "Analyzing $BIN ..."

r2cmd "aaa" >/dev/null

# Key string xrefs
for s in "com.xiaomi.vlive.IMyBinderService" "libvc.so" "libvc++.so" "Replaced" "ProcessState" "enforceInterface" "defaultServiceManager" "joinThreadPool" "dlopen" "signalEndOfInputStream"; do
  echo "=== xrefs: $s ===" >> "$OUT/xrefs_strings.txt"
  r2cmd "iz~$s; /r $s" >> "$OUT/xrefs_strings.txt" 2>&1 || true
  echo >> "$OUT/xrefs_strings.txt"
done

# Disasm at TX cluster addresses
for addr in 0x43e880 0x43f8fc 0x43fa08 0x444024 0x4456c0 0x46279c 0x4469f4 0x43fb9c; do
  r2cmd "s $addr; pdf" > "$OUT/disasm_${addr}.txt" 2>&1 || true
done

# Find onTransact-like: functions calling enforceInterface import
r2cmd "afl~onTransact" > "$OUT/functions_onTransact.txt" 2>&1 || true
r2cmd "afl~Transact" >> "$OUT/functions_onTransact.txt" 2>&1 || true
r2cmd "afl~Binder" > "$OUT/functions_binder.txt" 2>&1 || true
r2cmd "afl~init" > "$OUT/functions_init.txt" 2>&1 || true

# Imports summary
r2cmd "ii" > "$OUT/imports.txt"
r2cmd "iE~JNI" > "$OUT/exports_jni.txt" 2>&1 || true

echo "Deep r2 output -> $OUT"
