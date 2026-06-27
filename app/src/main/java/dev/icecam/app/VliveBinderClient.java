package dev.icecam.app;

import android.os.IBinder;
import android.os.Parcel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VliveBinderClient {
    public static final String DESCRIPTOR = "com.xiaomi.vlive.IMyBinderService";
    public static final int TX_PLAY_SOURCE = 11, TX_STATUS = 12, TX_INT_ARRAY = 13, TX_MODE_STRING = 14,
            TX_GET_INT = 15, TX_ZERO_16 = 16, TX_ZERO_17 = 17, TX_INT_18 = 18, TX_ZERO_19 = 19,
            TX_RANGE = 22, TX_TRANSFORM = 24, TX_25 = 25;

    private final AppLogger log;
    private String preferredService = RootBootstrap.FIXED_SERVICE_NAME;
    private String lastError = "not connected";
    private IBinder cachedBinder = null;
    private String cachedName = null;

    // Only exact recovered/native names. Do not fall back to random Xiaomi services: they accept a different interface token.
    private final List<String> candidates = new ArrayList<>(Arrays.asList(
            RootBootstrap.FIXED_SERVICE_NAME,
            "com.xiaomi.vlive.IMyBinderService",
            "vlive",
            "vlive_service",
            "vcplax",
            "MyBinderService"));

    public VliveBinderClient(AppLogger logger) { log = logger; }
    public void setPreferredService(String s) {
        if (s != null && s.trim().length() > 0) {
            String n = s.trim();
            if (!n.equals(preferredService)) { cachedBinder = null; cachedName = null; }
            preferredService = n;
        }
    }
    public String preferredService() { return preferredService; }
    public String lastError() { return lastError; }
    public void clearCache() { cachedBinder = null; cachedName = null; lastError = "cache cleared"; }

    public String[] listServices() {
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            Method m = sm.getDeclaredMethod("listServices");
            String[] arr = (String[]) m.invoke(null);
            if (arr != null) return arr;
        } catch (Throwable t) { lastError = "listServices: " + t; }
        return new String[0];
    }

    private IBinder getServiceByName(String name) {
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            Method m = sm.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m.invoke(null, name);
            if (b != null && b.isBinderAlive()) return b;
        } catch (Throwable t) { lastError = "getService(" + name + "): " + t; }
        return null;
    }

    public IBinder service() {
        // v11: do not re-probe every transaction. The native service may return an empty
        // descriptor and some probe transactions are stateful. If we already used a live
        // binder once, keep it until it dies.
        if (cachedBinder != null && cachedBinder.isBinderAlive()) {
            preferredService = cachedName != null ? cachedName : preferredService;
            lastError = "connected cached service=" + preferredService;
            return cachedBinder;
        }

        ArrayList<String> names = new ArrayList<>();
        names.add(preferredService);
        for (String c : candidates) if (!names.contains(c)) names.add(c);
        for (String name : names) {
            IBinder b = getServiceByName(name);
            if (b == null) continue;

            // The recovered daemon usually registers with a random/custom service name and
            // an empty descriptor in servicemanager. Accept exact known daemon names without
            // descriptor blocking; the interface token is still written for every transact.
            if (RootBootstrap.FIXED_SERVICE_NAME.equals(name) || "vcplax".equals(name) || name.equals(preferredService)) {
                cachedBinder = b;
                cachedName = name;
                preferredService = name;
                lastError = "connected raw service=" + name;
                log.log("binder", lastError);
                return b;
            }

            if (probeDescriptor(b, name)) {
                cachedBinder = b;
                cachedName = name;
                preferredService = name;
                lastError = "connected probed service=" + name;
                log.log("binder", lastError);
                return b;
            }
            log.log("binder", "reject service=" + name + " descriptor/probe mismatch");
        }
        lastError = "VLive binder not found. service=" + preferredService + " not available";
        return null;
    }

    private boolean probeDescriptor(IBinder b, String name) {
        // First check interface descriptor. Some native services return null until first transact, so allow TX12 probe too.
        try {
            String d = b.getInterfaceDescriptor();
            if (DESCRIPTOR.equals(d)) return true;
            if (d != null && d.length() > 0 && !d.equals(DESCRIPTOR)) {
                lastError = "service " + name + " has descriptor " + d;
                return false;
            }
        } catch (Throwable ignored) {}
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            boolean ok = b.transact(TX_STATUS, data, reply, 0);
            if (!ok) return false;
            reply.readException();
            return true;
        } catch (Throwable t) {
            lastError = "probe(" + name + "): " + t.getClass().getSimpleName() + ": " + t.getMessage();
            return false;
        } finally { data.recycle(); reply.recycle(); }
    }

    public boolean connected() { IBinder b = service(); return b != null && b.isBinderAlive(); }

    private int transactInt(int code, Parcel data) {
        Parcel reply = Parcel.obtain();
        try {
            IBinder b = service();
            if (b == null) { log.log("binder", "TX" + code + " skipped: " + lastError); return -999; }
            boolean ok = b.transact(code, data, reply, 0);
            if (!ok) { lastError = "transact returned false code=" + code; return -997; }
            reply.readException();
            int value = reply.dataAvail() >= 4 ? reply.readInt() : 0;
            log.log("binder", "TX" + code + " -> " + value + " via " + preferredService);
            return value;
        } catch (Throwable t) {
            lastError = "TX" + code + ": " + t.getClass().getSimpleName() + ": " + t.getMessage();
            log.log("binder", lastError);
            return -998;
        } finally { reply.recycle(); data.recycle(); }
    }

    public int playSource(String path, boolean mirrorFlagIgnoredByOriginal, boolean loopFlag) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeString(path);
        p.writeInt(0);
        p.writeInt(loopFlag ? 1 : 0);
        return transactInt(TX_PLAY_SOURCE, p);
    }

    public int setModeString(int mode, String value) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeInt(mode);
        p.writeString(value);
        return transactInt(TX_MODE_STRING, p);
    }
    public int statusCode() { Parcel p = Parcel.obtain(); p.writeInterfaceToken(DESCRIPTOR); return transactInt(TX_STATUS, p); }
    public int getInt15() { Parcel p = Parcel.obtain(); p.writeInterfaceToken(DESCRIPTOR); return transactInt(TX_GET_INT, p); }
    public int setRange(long start, long end) { Parcel p = Parcel.obtain(); p.writeInterfaceToken(DESCRIPTOR); p.writeLong(start); p.writeLong(end); return transactInt(TX_RANGE, p); }
    public int setTransform(int mode, float panX, float panY, float zoomX, float zoomY, int flags) {
        Parcel p = Parcel.obtain();
        p.writeInterfaceToken(DESCRIPTOR);
        p.writeInt(mode);
        p.writeFloat(panX);
        p.writeFloat(panY);
        p.writeFloat(zoomX);
        p.writeFloat(zoomY);
        p.writeInt(flags);
        log.log("tx24", String.format(java.util.Locale.US,
                "send mode=%d pan=(%.2f,%.2f) zoom=(%.2f,%.2f) flags=0x%08X",
                mode, panX, panY, zoomX, zoomY, flags));
        return transactInt(TX_TRANSFORM, p);
    }
    public int setTransform(TransformState s) { return setTransform(s.mode, s.panX, s.panY, s.zoomX, s.zoomY, s.flags); }
    public int sendBoolCode(int code, boolean v) { Parcel p = Parcel.obtain(); p.writeInterfaceToken(DESCRIPTOR); p.writeInt(v ? 1 : 0); return transactInt(code, p); }
    public int sendIntCode(int code, int v) { Parcel p = Parcel.obtain(); p.writeInterfaceToken(DESCRIPTOR); p.writeInt(v); return transactInt(code, p); }
    public int simple(int code) { Parcel p = Parcel.obtain(); p.writeInterfaceToken(DESCRIPTOR); return transactInt(code, p); }

    public String diagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("preferred=").append(preferredService).append('\n');
        sb.append("connected=").append(connected()).append('\n');
        sb.append("lastError=").append(lastError).append('\n');
        sb.append("expected descriptor=").append(DESCRIPTOR).append('\n');
        sb.append("candidate services=\n");
        for (String c : candidates) sb.append("  ").append(c).append('\n');
        sb.append("filtered Android services=\n");
        for (String s : listServices()) {
            String lo = s.toLowerCase();
            if (lo.contains("vlive") || lo.contains("camera") || lo.contains("media") || lo.contains("vcplax") || lo.contains("ice")) sb.append("  ").append(s).append('\n');
        }
        return sb.toString();
    }
}
