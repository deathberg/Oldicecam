# v6 deep reconstruction notes

The main functional error in v4/v5 was the Binder service model.

Recovered from original `App.onCreate`:

```text
file /system/bin/cameraserver
if output contains "32-bit" -> lib/armeabi-v7a
else -> lib/arm64-v8a
extract .so files from APK into app files dir
cp <extracted>/libvc.so /data/libvc.so
cp <extracted>/libshadowhook.so /data/libvc++.so
cp <extracted>/vcplax.so /data/vcplax
chmod 700 /data/vcplax
/data/vcplax <ServerName> &
```

Recovered from `App.d()`:

- preference key: `ServerName`
- if empty, original app generates a random lowercase service name and stores it
- Java Binder proxy still writes interface token `com.xiaomi.vlive.IMyBinderService`

Therefore:

- `ServerName` is the actual ServiceManager name to connect to;
- `com.xiaomi.vlive.IMyBinderService` is the Binder interface token/descriptor;
- launching `/data/vcplax` without the ServerName argument prevents the service from being published.

v6 implements this correction.
