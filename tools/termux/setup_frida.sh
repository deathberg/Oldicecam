#!/data/data/com.termux/files/usr/bin/bash
# Install Frida on Termux (phone-only, rooted). Do NOT use pip for frida core.
set -euo pipefail

echo "=== Frida setup for Termux (root) ==="

pkg update -y
pkg install -y root-repo tsu wget xz curl which termux-tools

# Official Termux way (see: frida command hint after failed pip)
pkg install -y frida frida-python 2>/dev/null || pkg install -y frida-python

if ! command -v frida >/dev/null 2>&1; then
  echo "ERROR: frida CLI not found after pkg install."
  echo "Try manually: pkg install root-repo && pkg update && pkg install frida frida-python"
  exit 1
fi

FRIDA_VER="$(frida --version)"
echo "Frida CLI version: $FRIDA_VER"

ABI="$(getprop ro.product.cpu.abi)"
case "$ABI" in
  arm64-v8a) FRIDA_ARCH="android-arm64" ;;
  armeabi-v7a|armeabi) FRIDA_ARCH="android-arm" ;;
  x86_64) FRIDA_ARCH="android-x86_64" ;;
  x86) FRIDA_ARCH="android-x86" ;;
  *) echo "Unknown ABI: $ABI"; exit 1 ;;
esac
echo "Device ABI: $ABI -> $FRIDA_ARCH"

SERVER="frida-server-${FRIDA_VER}-${FRIDA_ARCH}"
URL="https://github.com/frida/frida/releases/download/${FRIDA_VER}/${SERVER}.xz"

mkdir -p "$HOME/frida"
cd "$HOME/frida"

if [[ ! -x "$HOME/frida/frida-server" ]]; then
  echo "Downloading $URL ..."
  wget -O "${SERVER}.xz" "$URL"
  xz -d -f "${SERVER}.xz"
  mv "$SERVER" frida-server
  chmod 755 frida-server
fi

# Install to location executable from root
tsu -c "cp '$HOME/frida/frida-server' /data/local/tmp/frida-server && chmod 755 /data/local/tmp/frida-server"

# Kill old server
tsu -c "pkill -9 frida-server" 2>/dev/null || true
sleep 1

# Start as root (required for vcplax)
tsu -c "/data/local/tmp/frida-server -D &"
sleep 2

if tsu -c "pidof frida-server" >/dev/null 2>&1; then
  echo "OK: frida-server running"
else
  echo "WARN: frida-server may not be running — try manually:"
  echo "  tsu -c '/data/local/tmp/frida-server -D &'"
fi

echo ""
frida-ps -U 2>/dev/null || frida-ps -H 127.0.0.1 || true

cat <<'EOF'

=== Next steps ===
1) Open the camera app (vcplax must run):
     tsu -c "pidof vcplax"

2) Download hook script:
     curl -LO https://raw.githubusercontent.com/deathberg/Oldicecam/cursor/apk-full-reverse-e3a1/tools/termux/frida_hook_libvc.js

3) Attach (native-only, NO Java — use qjs runtime):
     tsu -c "setenforce 0"
     PID=$(tsu -c "pidof vcplax")
     frida -U -p $PID -l frida_hook_libvc.js -o ~/libvc_hooks.log --runtime=qjs
     # or frida-inject:
     # /data/local/tmp/frida-inject -p $PID -s frida_hook_libvc.js --runtime=qjs > /data/local/tmp/libvc_hooks.log

4) Use the app 30-60 sec (play source, transform), Ctrl+C, send ~/libvc_hooks.log

Restart server after reboot:
  tsu -c "/data/local/tmp/frida-server -D &"
EOF
