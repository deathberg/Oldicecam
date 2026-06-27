package com.xiaomi.vlive.util;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.xiaomi.vlive.App;
import com.xiaomi.vlive.binder.MyBinderClient;

import java.lang.reflect.Method;

/** Reconstructed Binder helpers from {@code p037U.AbstractC0330t}. */
public final class VliveBridge {
    private static MyBinderClient client;
    private static int connectFailures;

    private VliveBridge() {}

    public static MyBinderClient service() {
        if (client != null) return client;
        try {
            RootShell.exec("setenforce 0");
            Class<?> sm = Class.forName("android.os.ServiceManager");
            Method get = sm.getMethod("getService", String.class);
            IBinder binder = (IBinder) get.invoke(null, App.getInstance().getServerName());
            if (binder == null) {
                connectFailures++;
                if (connectFailures > 5) showConnectError();
                return null;
            }
            connectFailures = 0;
            binder.linkToDeath(() -> client = null, 0);
            RootShell.exec("setenforce 1");
            client = new MyBinderClient(binder);
            return client;
        } catch (Exception e) {
            return null;
        }
    }

    private static void showConnectError() {
        String id = RootShell.exec("id");
        String selinux = RootShell.exec("getenforce");
        if (!id.contains("uid=0")) {
            toast("程序获取root权限失败");
        } else if (selinux.contains("Permissive")) {
            toast("无法与进程通信\n请关闭APP后重新启动再试");
        } else {
            toast("系统设置失败,请更换低版本系统");
        }
    }

    public static boolean isPlaying() {
        try {
            MyBinderClient s = service();
            return s != null && s.getStatus() == MyBinderClient.STATUS_PLAYING;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean setSource(String path, int mode) {
        try {
            MyBinderClient s = service();
            return s != null && s.setMode(mode, path) == MyBinderClient.OK_SET_SOURCE;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static void seekRange(long beginUs, long endUs) {
        try {
            MyBinderClient s = service();
            if (s != null) s.seekRange(beginUs, endUs);
        } catch (RemoteException ignored) {}
    }

    public static void setLoop(boolean loop) {
        try {
            MyBinderClient s = service();
            if (s != null) s.setLoop(loop);
        } catch (RemoteException ignored) {}
    }

    public static void setMirror(boolean mirror) {
        try {
            MyBinderClient s = service();
            if (s != null) s.setMirror(mirror);
        } catch (RemoteException ignored) {}
    }

    public static void setAngle(int angle) {
        try {
            MyBinderClient s = service();
            if (s != null) s.setAngle(angle);
        } catch (RemoteException ignored) {}
    }

    public static void setAutoRotate(boolean enabled) {
        try {
            MyBinderClient s = service();
            if (s != null) s.setAutoRotate(enabled);
        } catch (RemoteException ignored) {}
    }

    public static boolean applyAutoColor(int detectedColor, App app) {
        try {
            MyBinderClient s = service();
            if (s == null) return false;
            int rc = s.setTransform(
                    detectedColor,
                    app.getPrefs().getFloat("AutoColor_X", 50f),
                    app.getPrefs().getFloat("AutoColor_Y", 50f),
                    app.getPrefs().getFloat("AutoColor_intensity", 0.3f),
                    app.getPrefs().getFloat("AutoColor_diameter", 0.6f),
                    app.getPrefs().getInt("PlayAutoColor_mode", 1));
            return rc == MyBinderClient.OK_TRANSFORM;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static void hardRecovery() {
        try {
            MyBinderClient s = service();
            if (s != null) s.hardRecovery();
        } catch (RemoteException ignored) {}
    }

    private static void toast(String msg) {
        // Original uses custom Toast UI via AbstractC0330t.m856g
    }
}
