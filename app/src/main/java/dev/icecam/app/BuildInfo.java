package dev.icecam.app;

public final class BuildInfo {
    private BuildInfo() {}
    public static final int VERSION_CODE = 26;
    public static final String VERSION_NAME = "0.26-v26";
    public static final String BUILD_LABEL = "IceCam Pro v26";
    public static final String BUILD_FLAVOR = "pro";
    /** Native stack: ShadowHook 2.x + vcplax/libvc in jniLibs; Android 7–16 (API 23–36). */
    public static final String NATIVE_STACK_NOTE = "SH2 · API23–36";
}
