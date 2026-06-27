package com.xiaomi.vlive;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.xiaomi.vlive.util.RootShell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Application entry point — clean-room rebuild of the original App.
 *
 * On startup it:
 *   1. kills any stale daemon ("killall vcplax");
 *   2. extracts the bundled native payload from its own APK into filesDir;
 *   3. via root, copies them to /data (libvc.so, libvc++.so, vcplax) and chmods;
 *   4. launches the native daemon "/data/vcplax <ServerName> &".
 *
 * The daemon registers a Binder service under {@link #getServerName()} (a randomized
 * name cached in prefs) which the UI then drives through VcBinderClient.
 *
 * The deploy step is a no-op (logged) when there is no root or the payload is
 * missing — the app UI still runs so the build is demonstrable without a device.
 */
public class App extends Application {

    private static App INSTANCE;

    private SharedPreferences prefs;
    private HandlerThread workerThread;
    private Handler worker;
    private final Handler main = new Handler(Looper.getMainLooper());

    public static App instance() { return INSTANCE; }

    public SharedPreferences prefs() { return prefs; }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);

        workerThread = new HandlerThread("TimerThread");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());

        worker.post(this::deployAndLaunchDaemon);
    }

    // ─── preferences: action ranges & playback flags ───────────────────────

    public long rangeBegin(int preset, long def) {
        return prefs.getLong("ActionRangebgin" + preset, def);
    }

    public long rangeEnd(int preset, long def) {
        return prefs.getLong("ActionRangeEnd" + preset, def);
    }

    public void setRangeBegin(int preset, long v) {
        prefs.edit().putLong("ActionRangebgin" + preset, v).apply();
    }

    public void setRangeEnd(int preset, long v) {
        prefs.edit().putLong("ActionRangeEnd" + preset, v).apply();
    }

    public boolean isLoop() { return prefs.getBoolean("PlayisLoop", false); }

    public void setLoop(boolean v) { prefs.edit().putBoolean("PlayisLoop", v).apply(); }

    public boolean isMirror() { return prefs.getBoolean("PlayMirror", false); }

    public void setMirror(boolean v) { prefs.edit().putBoolean("PlayMirror", v).apply(); }

    public int getAngle() { return prefs.getInt("PlayAngle", 0); }

    public void setAngle(int v) { prefs.edit().putInt("PlayAngle", v).apply(); }

    /**
     * Stable, randomized masquerade name for the daemon's Binder service.
     * Picks a real ServiceManager entry name + random suffix (anti-detection),
     * caching it in the "ServerName" pref. Mirrors the original getServerName/d().
     */
    public String getServerName() {
        String name = prefs.getString("ServerName", "");
        if (!name.isEmpty()) return name;
        String generated;
        try {
            Method listServices = Class.forName("android.os.ServiceManager")
                    .getDeclaredMethod("listServices");
            listServices.setAccessible(true);
            String[] services = (String[]) listServices.invoke(null);
            if (services != null && services.length > 0) {
                generated = services[new Random().nextInt(services.length)] + randomLetters(1, 3);
            } else {
                generated = randomLetters(5, 12);
            }
        } catch (Exception e) {
            generated = randomLetters(5, 12);
        }
        prefs.edit().putString("ServerName", generated).apply();
        return generated;
    }

    private static String randomLetters(int min, int max) {
        if (min <= 0 || max < min) return "";
        Random r = new Random();
        int n = r.nextInt(max - min + 1) + min;
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append("abcdefghijklmnopqrstuvwxyz".charAt(r.nextInt(26)));
        return sb.toString();
    }

    // ─── native payload deployment ─────────────────────────────────────────

    private void deployAndLaunchDaemon() {
        if (!RootShell.hasRoot()) {
            // No root in this environment: UI still works, daemon not launched.
            return;
        }
        RootShell.exec("killall vcplax");

        String filesDir = getApplicationContext().getFilesDir().getAbsolutePath();
        boolean is32 = RootShell.exec("file /system/bin/cameraserver").contains("32-bit");
        String abiDir = is32 ? "lib/armeabi-v7a" : "lib/arm64-v8a";

        extractNativeLibs(abiDir, filesDir);

        String serverName = getServerName();
        RootShell.exec("cp " + filesDir + "/" + abiDir + "/libvc_clone.so /data/libvc.so");
        RootShell.exec("cp " + filesDir + "/" + abiDir + "/libshadowhook.so /data/libvc++.so");
        RootShell.exec("cp " + filesDir + "/" + abiDir + "/vcplax.so /data/vcplax");
        RootShell.exec("chmod 700 /data/vcplax");
        RootShell.exec("/data/vcplax " + serverName + "&");
    }

    private void extractNativeLibs(String abiDir, String destBase) {
        try {
            ApplicationInfo info = getApplicationContext().getApplicationInfo();
            File base = new File(destBase);
            if (!base.exists()) base.mkdirs();
            try (ZipFile zip = new ZipFile(info.sourceDir)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = entries.nextElement();
                    String name = e.getName();
                    if (!name.startsWith(abiDir) || !name.endsWith(".so")) continue;
                    File out = new File(destBase, name);
                    File parent = out.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();
                    try (InputStream in = zip.getInputStream(e);
                         FileOutputStream fos = new FileOutputStream(out)) {
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (workerThread != null) workerThread.quitSafely();
    }
}
