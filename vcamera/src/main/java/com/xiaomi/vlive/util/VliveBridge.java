package com.xiaomi.vlive.util;

import android.content.Context;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.vlive.App;
import com.xiaomi.vlive.binder.VcBinderClient;

import java.lang.reflect.Method;

/**
 * High-level bridge to the native daemon (clean-room rebuild of the merged
 * AbstractC0330t helpers recovered in the reconstruction).
 *
 * Responsibilities:
 *  - resolve the (randomly named) Binder service via ServiceManager reflection;
 *  - toggle SELinux around the lookup;
 *  - cache the proxy and reset it on binderDied;
 *  - expose typed wrappers used by the UI / floating panel.
 */
public final class VliveBridge {

    private static VcBinderClient cached;
    private static int failCount;

    private VliveBridge() {}

    /** Resolve (and cache) the daemon proxy. Returns null if unavailable. */
    public static VcBinderClient service() {
        try {
            if (cached != null) return cached;
            RootShell.exec("setenforce 0");
            String name = App.instance().getServerName();
            IBinder binder = getServiceManagerService(name);
            if (binder == null) {
                if (++failCount > 5) {
                    if (!RootShell.exec("id").contains("uid=0")) {
                        toast("\u7a0b\u5e8f\u83b7\u53d6root\u6743\u9650\u5931\u8d25"); // failed to get root
                    } else if (RootShell.exec("getenforce").contains("Permissive")) {
                        toast("\u65e0\u6cd5\u4e0e\u8fdb\u7a0b\u901a\u4fe1\n\u8bf7\u91cd\u542f\u540e\u91cd\u8bd5"); // cannot talk to daemon
                    } else {
                        toast("\u7cfb\u7edf\u8bbe\u7f6e\u5931\u8d25"); // system setup failed
                    }
                }
                return null;
            }
            failCount = 0;
            try {
                binder.linkToDeath(() -> cached = null, 0);
            } catch (RemoteException ignored) {}
            RootShell.exec("setenforce 1");
            cached = new VcBinderClient(binder);
            return cached;
        } catch (Exception e) {
            return null;
        }
    }

    private static IBinder getServiceManagerService(String name) {
        try {
            Method getService = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            return (IBinder) getService.invoke(null, name);
        } catch (Throwable t) {
            return null;
        }
    }

    // ─── typed wrappers ────────────────────────────────────────────────────

    public static void setPlayRange(long beginUs, long endUs) {
        VcBinderClient s = service();
        if (s == null) return;
        try { s.setPlayRange(beginUs, endUs); } catch (Exception ignored) {}
    }

    public static boolean isPlaying() {
        VcBinderClient s = service();
        if (s == null) return false;
        try { return s.getPlayState() == 5; } catch (Exception e) { return false; }
    }

    public static boolean selectVideo(String path, int index) {
        VcBinderClient s = service();
        if (s == null) return false;
        try { return s.selectVideo(index, path) == 4; } catch (Exception e) { return false; }
    }

    public static void setLoop(boolean loop) {
        VcBinderClient s = service();
        if (s == null) return;
        try { s.setFlagA(loop); } catch (Exception ignored) {}
    }

    public static void setMirror(boolean mirror) {
        VcBinderClient s = service();
        if (s == null) return;
        try { s.setMirror(mirror); } catch (Exception ignored) {}
    }

    public static void setAngle(int degrees) {
        VcBinderClient s = service();
        if (s == null) return;
        try { s.setPlayAngle(degrees); } catch (Exception ignored) {}
    }

    public static boolean setAutoColor(int mode) {
        VcBinderClient s = service();
        if (s == null) return false;
        App app = App.instance();
        try {
            return s.setAutoColor(mode,
                    app.prefs().getFloat("AutoColor_X", 50f),
                    app.prefs().getFloat("AutoColor_Y", 50f),
                    app.prefs().getFloat("AutoColor_intensity", 0.3f),
                    app.prefs().getFloat("AutoColor_diameter", 0.6f),
                    app.prefs().getInt("PlayAutoColor_mode", 1)) == 14;
        } catch (Exception e) {
            return false;
        }
    }

    public static void replace() {
        VcBinderClient s = service();
        if (s == null) return;
        try { s.replaceCommand(); } catch (Exception ignored) {}
    }

    // ─── UI helper: rounded dark toast (matches original f()) ───────────────

    public static void toast(String msg) {
        Context ctx = App.instance().getApplicationContext();
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setPadding(36, 36, 36, 36);
        box.setGravity(Gravity.CENTER_VERTICAL);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#CC000000"));
        bg.setCornerRadius(24f);
        box.setBackground(bg);
        TextView tv = new TextView(ctx);
        tv.setText(msg);
        tv.setTextColor(Color.WHITE);
        box.addView(tv);
        Toast toast = new Toast(ctx);
        toast.setView(box);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
