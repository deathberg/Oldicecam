# v5 notes, superseded by v6

v5 reproduced part of the original UI surface but incorrectly used Chinese labels and still launched `/data/vcplax` without the recovered `ServerName` argument.

v6 fixes both issues:

- English UI only;
- original-style native extraction from APK;
- ABI selection via `/system/bin/cameraserver`;
- `/data/vcplax <ServerName> &` launch;
- Binder connects to `ServerName`, while using `com.xiaomi.vlive.IMyBinderService` only as the interface token.
