package com.xiaomi.vlive;

/**
 * Binder contract for the native virtual-camera daemon ("vcplax").
 *
 * Interface token: "com.xiaomi.vlive.IMyBinderService".
 * The server is the native ELF daemon, registered in ServiceManager under a
 * randomized name. The app only ever holds the proxy.
 *
 * Transaction codes (FIRST_CALL_TRANSACTION + n) are pinned to the values
 * recovered from the original APK so this clone is wire-compatible with the
 * original native daemon AND with clone/native/libvc_clone.cpp:
 *   11 setVideoSource, 12 command12, 13 getStatusArray, 14 selectVideo,
 *   15 getPlayState, 16 setFlagB, 17 setFlagA, 18 setPlayAngle, 19 setMirror,
 *   22 setPlayRange, 24 setAutoColor, 25 replaceCommand.
 *
 * NOTE: AIDL auto-assigns transaction codes sequentially, which would NOT match
 * the pinned codes. Therefore the app does NOT use this generated Stub/Proxy for
 * IPC; it is kept for documentation/codegen. Actual IPC goes through
 * binder.VcBinderClient, which writes the exact codes by hand.
 */
interface IMyBinderService {
    int setVideoSource(String path, boolean loop, boolean flag);   // tx 11
    int command12();                                               // tx 12
    int[] getStatusArray();                                        // tx 13
    int selectVideo(int index, String path);                       // tx 14 (==4 ok)
    int getPlayState();                                            // tx 15 (==5 playing)
    int setFlagB(boolean enabled);                                 // tx 16
    int setFlagA(boolean enabled);                                 // tx 17
    int setPlayAngle(int degrees);                                 // tx 18
    int setMirror(boolean mirror);                                 // tx 19
    int setPlayRange(long beginUs, long endUs);                    // tx 22
    int setAutoColor(int onOff, float x, float y, float intensity, float diameter, int colorMode); // tx 24 (==14 ok)
    int replaceCommand();                                          // tx 25
}
