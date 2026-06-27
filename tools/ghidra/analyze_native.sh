#!/usr/bin/env bash
# Import native libs into Ghidra headless project with bookmarks for known TX sites.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
GHIDRA="$ROOT/tools/ghidra_11.3.1_PUBLIC"
HEADLESS="$GHIDRA/support/analyzeHeadless"
PROJECT_DIR="$ROOT/re-workspace/ghidra"
PROJECT_NAME="icecam_native"
LOG="$ROOT/re-workspace/logs/ghidra_analyze.log"
LIBS="$ROOT/decompiled/raw/lib"

mkdir -p "$PROJECT_DIR" "$(dirname "$LOG")"

if [[ ! -x "$HEADLESS" ]]; then
  echo "Ghidra not found. Run: bash tools/setup-re-env.sh" >&2
  exit 1
fi

analyze_one() {
  local abi="$1"
  local file="$2"
  local path="$LIBS/$abi/$file"
  local proc lang

  if [[ ! -f "$path" ]]; then
    echo "skip missing $path" | tee -a "$LOG"
    return 0
  fi

  case "$abi" in
    arm64-v8a) lang="AARCH64:LE:64:v8A" ;;
    armeabi-v7a) lang="ARM:LE:32:v8" ;;
    *) echo "unknown abi $abi" >&2; return 1 ;;
  esac

  echo "=== Ghidra: $abi/$file ($lang) ===" | tee -a "$LOG"
  # -overwrite re-analyzes; omit -deleteProject to keep cumulative project
  "$HEADLESS" "$PROJECT_DIR" "$PROJECT_NAME" \
    -import "$path" \
    -processor "$lang" \
    -overwrite \
    -analysisTimeoutPerFile 3600 \
    -log "$ROOT/re-workspace/logs/ghidra_${abi}_${file}.log" \
    2>&1 | tee -a "$LOG"

  # Post-process bookmarks for vcplax arm64
  if [[ "$file" == "vcplax.so" && "$abi" == "arm64-v8a" ]]; then
    "$HEADLESS" "$PROJECT_DIR" "$PROJECT_NAME" \
      -process "$file" \
      -scriptPath "$ROOT/tools/ghidra" \
      -postScript BookmarkIcecam.java \
      2>&1 | tee -a "$LOG" || true
  fi
}

: > "$LOG"

# Order: small → large
for pair in \
  "arm64-v8a libshadowhook.so" \
  "arm64-v8a libvc.so" \
  "arm64-v8a vcplax.so" \
  "armeabi-v7a libshadowhook.so" \
  "armeabi-v7a libvc.so" \
  "armeabi-v7a vcplax.so"; do
  set -- $pair
  analyze_one "$1" "$2"
done

echo "Done. Project: $PROJECT_DIR/$PROJECT_NAME" | tee -a "$LOG"
echo "Open in GUI: $GHIDRA/ghidraRun (import existing project)" | tee -a "$LOG"
