#!/usr/bin/env bash
# Bootstrap reverse-engineering environment for testicecam2.apk native libs.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TOOLS="$ROOT/tools"
APK="$ROOT/testicecam2.apk"
RAW="$ROOT/decompiled/raw"
LIBS="$RAW/lib"

echo "[re-env] workspace=$ROOT"

mkdir -p "$ROOT/re-workspace"/{ghidra,r2,exports,logs}
mkdir -p "$RAW" "$ROOT/decompiled/jadx" "$ROOT/docs/native_analysis"

# --- Python deps ---
python3 -m pip install -q --upgrade pip
python3 -m pip install -q lief capstone pyelftools unicorn

# --- JADX ---
if [[ ! -x "$TOOLS/bin/jadx" ]]; then
  echo "[re-env] downloading jadx..."
  wget -q -O "$TOOLS/jadx.zip" "https://github.com/skylot/jadx/releases/download/v1.5.1/jadx-1.5.1.zip"
  unzip -q -o "$TOOLS/jadx.zip" -d "$TOOLS"
fi

# --- apktool ---
if [[ ! -f "$TOOLS/apktool.jar" ]]; then
  echo "[re-env] downloading apktool..."
  wget -q -O "$TOOLS/apktool.jar" "https://github.com/iBotPeaches/Apktool/releases/download/v2.11.1/apktool_2.11.1.jar"
fi

# --- Ghidra (headless) ---
GHIDRA_VER="11.3.1"
GHIDRA_DIR="$TOOLS/ghidra_${GHIDRA_VER}_PUBLIC"
if [[ ! -x "$GHIDRA_DIR/support/analyzeHeadless" ]]; then
  echo "[re-env] downloading Ghidra ${GHIDRA_VER} (~430MB)..."
  wget -q -O "$TOOLS/ghidra.zip" \
    "https://github.com/NationalSecurityAgency/ghidra/releases/download/Ghidra_${GHIDRA_VER}_build/ghidra_${GHIDRA_VER}_PUBLIC_20250219.zip"
  unzip -q -o "$TOOLS/ghidra.zip" -d "$TOOLS"
fi

# --- radare2 (system package fallback) ---
if ! command -v r2 >/dev/null 2>&1; then
  echo "[re-env] installing radare2 deb..."
  wget -q -O /tmp/radare2.deb "https://github.com/radareorg/radare2/releases/download/5.9.8/radare2_5.9.8_amd64.deb"
  sudo dpkg -i /tmp/radare2.deb || sudo apt-get install -f -y -qq
fi

# --- cross binutils (optional but useful) ---
if command -v apt-get >/dev/null 2>&1; then
  sudo apt-get install -y -qq binutils-aarch64-linux-gnu binutils-arm-linux-gnueabihf binwalk 2>/dev/null || true
fi

# --- Extract APK natives + decode resources if missing ---
if [[ -f "$APK" ]]; then
  if [[ ! -f "$LIBS/arm64-v8a/vcplax.so" ]]; then
    echo "[re-env] extracting native libs from APK..."
    unzip -q -o "$APK" "lib/*" -d "$RAW"
  fi
  if [[ ! -d "$ROOT/decompiled/apktool" ]]; then
    echo "[re-env] apktool decode (first run may take ~1 min)..."
    java -jar "$TOOLS/apktool.jar" d "$APK" -o "$ROOT/decompiled/apktool" -f
  fi
  if [[ ! -d "$ROOT/decompiled/jadx/sources" ]]; then
    echo "[re-env] jadx decompile (first run may take ~1 min)..."
    "$TOOLS/bin/jadx" -d "$ROOT/decompiled/jadx" --show-bad-code --deobf "$APK"
  fi
fi

# --- Symlinks for convenience ---
ln -sfn "$LIBS" "$ROOT/re-workspace/libs"
ln -sfn "$ROOT/decompiled" "$ROOT/re-workspace/decompiled"

cat <<EOF

[re-env] Ready.

Quick commands:
  bash tools/re-analyze.sh              # LIEF/Capstone reports
  bash tools/ghidra/analyze_native.sh   # Ghidra headless (slow for vcplax)
  bash tools/r2/analyze_native.sh       # radare2 analysis + function list
  r2 -A -q -c 'afl' re-workspace/libs/arm64-v8a/libvc.so

Docs:
  docs/NATIVE_REVERSE_MAX.md
  re-workspace/README.md
EOF
