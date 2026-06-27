package com.bytedance.shadowhook;

/* loaded from: classes.dex */
public final class ShadowHook {
    private static native String nativeGetArch();

    private static native boolean nativeGetDebuggable();

    private static native int nativeGetInitErrno();

    private static native int nativeGetMode();

    private static native boolean nativeGetRecordable();

    private static native String nativeGetRecords(int i);

    private static native String nativeGetVersion();

    private static native int nativeInit(int i, boolean z2);

    private static native void nativeSetDebuggable(boolean z2);

    private static native void nativeSetRecordable(boolean z2);

    private static native String nativeToErrmsg(int i);
}
