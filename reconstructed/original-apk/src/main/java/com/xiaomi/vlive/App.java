package com.xiaomi.vlive;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.xiaomi.vlive.config.ActionRanges;
import com.xiaomi.vlive.config.AppConfigKeys;
import com.xiaomi.vlive.float.FloatingControlPanel;
import com.xiaomi.vlive.util.RootShell;
import com.xiaomi.vlive.util.VliveBridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reconstructed from {@code com.xiaomi.vlive.App}.
 * Boots native vcplax daemon and owns global preferences / float panel singleton.
 */
public class App extends Application {
    private static App instance;
    private static FloatingControlPanel floatingPanel;

    private SharedPreferences prefs;
    private HandlerThread timerThread;
    private Handler timerHandler;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable statePoller;

    public static App getInstance() {
        return instance;
    }

    public static void ensureFloatingPanel() {
        if (floatingPanel == null) {
            floatingPanel = new FloatingControlPanel(getInstance());
        }
        floatingPanel.showIfNeeded();
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public long getActionRangeBegin(int actionId, long defaultUs) {
        return prefs.getLong(AppConfigKeys.ACTION_RANGE_BEGIN_PREFIX + actionId, defaultUs);
    }

    public long getActionRangeEnd(int actionId, long defaultUs) {
        return prefs.getLong(AppConfigKeys.ACTION_RANGE_END_PREFIX + actionId, defaultUs);
    }

    public void setActionRangeBegin(int actionId, long us) {
        prefs.edit().putLong(AppConfigKeys.ACTION_RANGE_BEGIN_PREFIX + actionId, us).apply();
    }

    public void setActionRangeEnd(int actionId, long us) {
        prefs.edit().putLong(AppConfigKeys.ACTION_RANGE_END_PREFIX + actionId, us).apply();
    }

    public boolean isLoopEnabled() {
        return prefs.getBoolean(AppConfigKeys.PLAY_IS_LOOP, false);
    }

    public void setLoopEnabled(boolean loop) {
        prefs.edit().putBoolean(AppConfigKeys.PLAY_IS_LOOP, loop).apply();
    }

    /** Random service name persisted on first run — matches original m1779d(). */
    public String getServerName() {
        String name = prefs.getString(AppConfigKeys.SERVER_NAME, "");
        if (!name.isEmpty()) return name;
        String generated;
        try {
            Method list = Class.forName("android.os.ServiceManager").getDeclaredMethod("listServices");
            list.setAccessible(true);
            String[] services = (String[]) list.invoke(null);
            if (services != null && services.length > 0) {
                generated = services[new Random().nextInt(services.length)] + randomLetters(1, 3);
            } else {
                generated = randomLetters(5, 12);
            }
        } catch (Exception e) {
            generated = randomLetters(5, 12);
        }
        prefs.edit().putString(AppConfigKeys.SERVER_NAME, generated).apply();
        return generated;
    }

    private static String randomLetters(int min, int max) {
        Random r = new Random();
        int len = r.nextInt(max - min + 1) + min;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz".charAt(r.nextInt(26)));
        }
        return sb.toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences(AppConfigKeys.PREFS, MODE_PRIVATE);

        timerThread = new HandlerThread("TimerThread");
        timerThread.start();
        timerHandler = new Handler(timerThread.getLooper());

        if (VliveBridge.service() != null) {
            RootShell.exec("killall vcplax");
        }
        deployNativeBinaries();
        startVcplaxDaemon();
        startStatePoller();
    }

    private void deployNativeBinaries() {
        Context ctx = getApplicationContext();
        String outDir = ctx.getFilesDir().getAbsolutePath();
        String abi = RootShell.exec("file /system/bin/cameraserver").contains("32-bit")
                ? "lib/armeabi-v7a" : "lib/arm64-v8a";
        String libPrefix = abi;

        try {
            ApplicationInfo ai = ctx.getApplicationInfo();
            File base = new File(outDir);
            if (!base.exists()) base.mkdirs();

            ZipFile zip = new ZipFile(ai.sourceDir);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(libPrefix) && name.endsWith(".so")) {
                    File out = new File(outDir, name);
                    File parent = out.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();
                    try (InputStream in = zip.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(out)) {
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
                    }
                }
            }
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String server = getServerName();
        RootShell.exec("cp " + outDir + "/" + libPrefix + "/libvc.so /data/libvc.so");
        RootShell.exec("cp " + outDir + "/" + libPrefix + "/libshadowhook.so /data/libvc++.so");
        RootShell.exec("cp " + outDir + "/" + libPrefix + "/vcplax.so /data/vcplax");
        RootShell.exec("chmod 700 /data/vcplax");
        RootShell.exec("/data/vcplax " + server + "&");
    }

    private void startVcplaxDaemon() {
        // deployNativeBinaries already launches vcplax; kept split for clarity vs decompiled flow
    }

    /** Poll TX13 every 1s — RunnableC0063i case 9 */
    private void startStatePoller() {
        statePoller = new Runnable() {
            @Override
            public void run() {
                try {
                    int[] state = VliveBridge.service().pollState();
                    if (state != null) {
                        mainHandler.post(() -> onPollState(state));
                    }
                } catch (Exception ignored) {}
                timerHandler.postDelayed(this, 1000L);
            }
        };
        timerHandler.post(statePoller);
    }

    protected void onPollState(int[] state) {
        // Original pushes into LiveData observers on ControllerFragment
    }

    @Override
    public void onTerminate() {
        if (statePoller != null) timerHandler.removeCallbacks(statePoller);
        super.onTerminate();
    }

    /** Convenience for float panel seek buttons */
    public void triggerActionSeek(int actionId) {
        long begin = getActionRangeBegin(actionId, ActionRanges.defaultBegin(actionId));
        long end = getActionRangeEnd(actionId, ActionRanges.defaultEnd(actionId));
        VliveBridge.seekRange(begin, end);
    }
}
