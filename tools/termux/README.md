# Termux RE helpers

Frida hook script canonical copy: **`app/src/main/assets/re/frida_hook_libvc.js`**

Copy to device before attach/spawn:

```bash
cp app/src/main/assets/re/frida_hook_libvc.js /data/local/tmp/
```

Or use in-app **RE Capture** (`ReCaptureActivity`) which deploys the same asset automatically.
