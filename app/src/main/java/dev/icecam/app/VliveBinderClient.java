package dev.icecam.app;

import android.os.IBinder;
import android.os.Parcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recovered Binder client — matches com.xiaomi.vlive.IMyBinderService / C1391f (RE verified).
 * TX24 = color injection (mode, x, y, intensity, diameter, colorMode) — NOT geometry.
 */
public final class VliveBinderClient {
    public static final String DESCRIPTOR = "com.xiaomi.vlive.IMyBinderService";

    public static final int TX_PLAY_SOURCE = 11;
    public static final int TX_STOP = 12;
    public static final int TX_POLL = 13;
    public static final int TX_SET_MODE = 14;
    public static final int TX_GET_STATUS = 15;
    public static final int TX_AUTO_ROTATE = 16;
    public static final int TX_LOOP = 17;
    public static final int TX_ANGLE = 18;
    public static final int TX_MIRROR = 19;
    public static final int TX_SEEK = 22;
    public static final int TX_COLOR_INJECT = 24;
    public static final int TX_RECOVERY = 25;

    private final AppLogger log;
    private String preferredService = RootBootstrap.FIXED_SERVICE_NAME;
    private String lastError = "not connected";
    private IBinder cachedBinder;
    private String cachedName;

    private final List<String> candidates = new ArrayList<>(Arrays.asList(
            RootBootstrap.FIXED_SERVICE_NAME, "vcplax", "dataloader_managerhow"));

    public VliveBinderClient(AppLogger logger) { log = logger; }

    public void setPreferredService(String s) {
        if (s != null && !s.trim().isEmpty() && !s.equals(preferredService)) {
            cachedBinder = null;
            cachedName = null;
            preferredService = s.trim();
        }
    }

    public String lastError() { return lastError; }
    public void clearCache() { cachedBinder = null; cachedName = null; lastError = "cache cleared"; }
    public boolean connected() { IBinder b = service(); return b != null && b.isBinderAlive(); }

    public IBinder service() {
        if (cachedBinder != null && cachedBinder.isBinderAlive()) return cachedBinder;
        for (String name : candidates) {
            IBinder b = getServiceByName(name);
            if (b == null) continue;
            cachedBinder = b;
            cachedName = name;
            preferredService = name;
            lastError = "connected " + name;
            log.log("binder", lastError);
            return b;
        }
        lastError = "service not found";
        return null;
    }

    private IBinder getServiceByName(String name) {
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            IBinder b = (IBinder) sm.getDeclaredMethod("getService", String.class).invoke(null, name);
            if (b != null && b.isBinderAlive()) return b;
        } catch (Throwable t) { lastError = String.valueOf(t); }
        return null;
    }

    public int playSource(String path, boolean loop) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeString(path);
        p.writeInt(0);
        p.writeInt(loop ? 1 : 0);
        return transactInt(TX_PLAY_SOURCE, p);
    }

    public int setMode(int mode, String path) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeInt(mode);
        p.writeString(path);
        return transactInt(TX_SET_MODE, p);
    }

    public int setLoop(boolean on) { return transactBool(TX_LOOP, on); }
    public int setAutoRotate(boolean on) { return transactBool(TX_AUTO_ROTATE, on); }
    public int setMirror(boolean on) { return transactBool(TX_MIRROR, on); }

    public int setAngle(int degrees) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeInt(degrees);
        return transactInt(TX_ANGLE, p);
    }

    public int seekRange(long beginMs, long endMs) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeLong(beginMs);
        p.writeLong(endMs);
        return transactInt(TX_SEEK, p);
    }

    /** TX24 — color injection only (三色). Do not use for pan/zoom. */
    public int setColorInject(int colorMode, float x, float y, float intensity, float diameter, int playMode) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeInt(colorMode);
        p.writeFloat(x);
        p.writeFloat(y);
        p.writeFloat(intensity);
        p.writeFloat(diameter);
        p.writeInt(playMode);
        log.log("binder", String.format("TX24 color mode=%d xy=(%.1f,%.1f) int=%.2f dia=%.2f pm=%d",
                colorMode, x, y, intensity, diameter, playMode));
        return transactInt(TX_COLOR_INJECT, p);
    }

    public int stop() {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        return transactInt(TX_STOP, p);
    }

    public int getStatus() {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        return transactInt(TX_GET_STATUS, p);
    }

    public int[] pollState() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder b = service();
            if (b == null) return new int[0];
            data.writeInterfaceToken(DESCRIPTOR);
            b.transact(TX_POLL, data, reply, 0);
            reply.readException();
            return reply.createIntArray();
        } catch (Throwable t) {
            log.log("binder", "TX13 poll: " + t);
            return new int[0];
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private int transactBool(int code, boolean v) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeInt(v ? 1 : 0);
        return transactInt(code, p);
    }

    private int transactInt(int code, Parcel data) {
        Parcel reply = Parcel.obtain();
        try {
            IBinder b = service();
            if (b == null) { log.log("binder", "TX" + code + " skip: " + lastError); return -999; }
            if (!b.transact(code, data, reply, 0)) return -997;
            reply.readException();
            return reply.dataAvail() >= 4 ? reply.readInt() : 0;
        } catch (Throwable t) {
            lastError = "TX" + code + ": " + t;
            log.log("binder", lastError);
            return -998;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public String diagnostics() {
        return "preferred=" + preferredService + " connected=" + connected() + " err=" + lastError;
    }
}
