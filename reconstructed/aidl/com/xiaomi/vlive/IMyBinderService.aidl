// Reconstructed AIDL contract for the native virtual-camera daemon.
//
// Interface token: "com.xiaomi.vlive.IMyBinderService"
// Implemented by the native ELF executable `/data/vcplax` (decompiled from
// lib/<abi>/vcplax.so), which registers itself in ServiceManager under a
// RANDOM service name (see App.getServerName() / "ServerName" pref).
//
// The Java side is the *proxy only*: there is no Stub in the APK, because the
// server lives in native code. Transaction codes below are the exact codes
// observed in the decompiled proxy (d1.f / C1391f), recovered from
// IBinder.transact(code, ...) calls. They start at FIRST_CALL_TRANSACTION+10
// (android.os.IBinder.FIRST_CALL_TRANSACTION == 1, i.e. 0x01), so e.g. tx 11.
//
// Method names are reconstructed from call sites in U/AbstractC0330t (the
// merged root+binder bridge). Return values are status ints from the daemon.
//
// This .aidl is for documentation/regeneration of the proxy; it is NOT the
// original build artifact (the original used a hand-written Parcel proxy).

package com.xiaomi.vlive;

interface IMyBinderService {
    // tx 11 -- setVideoSource(path, loop, mirror?) -> status
    //          proxy: C1391f.m1797j(String, boolean, boolean)
    int setVideoSource(String path, boolean loop, boolean flag);

    // tx 12 -- noArgCommand (likely stop / reset) -> status
    //          proxy: C1391f.m1798k()
    int command12();

    // tx 13 -- getStatusArray() -> int[]   proxy: C1391f.m1790c()
    int[] getStatusArray();

    // tx 14 -- selectVideo(index, path) -> status (==4 on success)
    //          proxy: C1391f.m1796i(String path, int index)
    //          NOTE: parcel order is writeInt(index) then writeString(path)
    int selectVideo(int index, String path);

    // tx 15 -- getPlayState() -> int (==5 means "playing")
    //          proxy: C1391f.m1793f()
    int getPlayState();

    // tx 16 -- setFlagB(boolean) -> status   proxy: C1391f.m1789b(boolean)
    int setFlagB(boolean enabled);

    // tx 17 -- setFlagA(boolean) -> status   proxy: C1391f.m1788a(boolean)
    int setFlagA(boolean enabled);

    // tx 18 -- setPlayAngle(degrees) -> status   proxy: C1391f.m1794g(int)
    int setPlayAngle(int degrees);

    // tx 19 -- setMirror(boolean) -> status   proxy: C1391f.m1791d(boolean)
    int setMirror(boolean mirror);

    // tx 22 -- setPlayRange(beginUs, endUs) -> status
    //          proxy: C1391f.m1792e(long, long)
    int setPlayRange(long beginUs, long endUs);

    // tx 24 -- setAutoColor(mode, x, y, intensity, diameter, colorMode) -> status (==14 ok)
    //          proxy: C1391f.m1795h(int, float, float, float, float, int)
    int setAutoColor(int onOff, float x, float y, float intensity, float diameter, int colorMode);

    // tx 25 -- replaceCommand (the "替" button) -> status   proxy: C1391f.m1799l()
    int replaceCommand();
}
