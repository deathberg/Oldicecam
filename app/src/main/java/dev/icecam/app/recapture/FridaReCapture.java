package dev.icecam.app.recapture;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import dev.icecam.app.AppLogger;
import dev.icecam.app.RootBootstrap;
import dev.icecam.app.Shell;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Root-assisted Frida deploy + vcplax capture for reverse-engineering logs.
 * Replaces Termux workflow: one APK writes to a shareable log file.
 */
public final class FridaReCapture {
    public static final String FRIDA_VERSION = "16.5.9";
    public static final String REMOTE_DIR = "/data/local/tmp";
    public static final String LOG_PUBLIC = "/sdcard/Download/icecam_re_capture.log";

    private final Context ctx;
    private final AppLogger log;

    public FridaReCapture(Context c, AppLogger logger) {
        ctx = c.getApplicationContext();
        log = logger;
    }

    public File privateLogFile() {
        return new File(ctx.getExternalFilesDir(null), "icecam_re_capture.log");
    }

    public String publicLogPath() {
        return LOG_PUBLIC;
    }

    /** Copy hook script from APK assets to /data/local/tmp (root). */
    public String deployHookScript() {
        File cached = new File(ctx.getFilesDir(), "frida_hook_libvc.js");
        try {
            copyAsset("re/frida_hook_libvc.js", cached);
        } catch (Throwable t) {
            return "asset copy failed: " + t;
        }
        String script = "set -e\n" +
                "cp -f " + Shell.q(cached.getAbsolutePath()) + " " + REMOTE_DIR + "/frida_hook_libvc.js\n" +
                "chmod 644 " + REMOTE_DIR + "/frida_hook_libvc.js\n" +
                "head -1 " + REMOTE_DIR + "/frida_hook_libvc.js\n" +
                "wc -c " + REMOTE_DIR + "/frida_hook_libvc.js\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("re/deploy-script", r.all());
        return r.all();
    }

    /**
     * Ensure frida-server + frida-inject on device (root dir).
     * Order: already in /data/local/tmp → app cache → download GitHub release.
     */
    public String ensureFridaBinaries() {
        String abi = preferredAbi();
        File localServer = new File(ctx.getFilesDir(), "frida-server-" + FRIDA_VERSION + "-" + abi);
        File localInject = new File(ctx.getFilesDir(), "frida-inject-" + FRIDA_VERSION + "-" + abi);

        String check = Shell.su(
                "test -x " + REMOTE_DIR + "/frida-server && test -x " + REMOTE_DIR + "/frida-inject && echo HAVE_BOTH || echo MISSING"
        ).out.trim();

        if ("HAVE_BOTH".equals(check)) {
            log.log("re/frida", "using existing " + REMOTE_DIR + "/frida-{server,inject}");
            return "already deployed in " + REMOTE_DIR;
        }

        if (!localServer.exists() || localServer.length() < 1_000_000L) {
            String assetServer = "re/frida-server-" + FRIDA_VERSION + "-android-" + abi;
            if (assetExists(assetServer)) {
                try {
                    copyAsset(assetServer, localServer);
                    log.log("re/frida", "extracted " + assetServer + " from APK assets");
                } catch (Throwable t) {
                    log.log("re/frida", "asset server missing/failed: " + t);
                }
            }
        }
        if (!localInject.exists() || localInject.length() < 1_000_000L) {
            String assetInject = "re/frida-inject-" + FRIDA_VERSION + "-android-" + abi;
            if (assetExists(assetInject)) {
                try {
                    copyAsset(assetInject, localInject);
                    log.log("re/frida", "extracted " + assetInject + " from APK assets");
                } catch (Throwable t) {
                    log.log("re/frida", "asset inject missing/failed: " + t);
                }
            }
        }

        if (!localServer.exists() || localServer.length() < 1_000_000L) {
            try {
                downloadFrida("frida-server", FRIDA_VERSION, abi, localServer);
            } catch (Throwable t) {
                return "download frida-server failed: " + t;
            }
        }
        if (!localInject.exists() || localInject.length() < 1_000_000L) {
            try {
                downloadFrida("frida-inject", FRIDA_VERSION, abi, localInject);
            } catch (Throwable t) {
                return "download frida-inject failed: " + t;
            }
        }

        String deploy = "set -e\n" +
                "cp -f " + Shell.q(localServer.getAbsolutePath()) + " " + REMOTE_DIR + "/frida-server\n" +
                "cp -f " + Shell.q(localInject.getAbsolutePath()) + " " + REMOTE_DIR + "/frida-inject\n" +
                "chmod 755 " + REMOTE_DIR + "/frida-server " + REMOTE_DIR + "/frida-inject\n" +
                "ls -l " + REMOTE_DIR + "/frida-server " + REMOTE_DIR + "/frida-inject\n";
        Shell.Result r = Shell.su(deploy);
        log.logBlock("re/deploy-frida", r.all());
        return r.all();
    }

    /** Full setup: SELinux, deploy script + binaries. */
    public String setupAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== setup start ===\n");
        sb.append(Shell.su("id\ngetenforce 2>/dev/null || true\nsetenforce 0 2>/dev/null || true\n").all());
        sb.append(deployHookScript()).append('\n');
        sb.append(ensureFridaBinaries()).append('\n');
        sb.append("=== setup end ===\n");
        return sb.toString();
    }

    /** Attach to running vcplax — Binder + TX13 + late XOR attempt. */
    public String startAttachCapture() {
        truncatePublicLog();
        String server = RootBootstrap.FIXED_SERVICE_NAME;
        String script = "set -e\n" +
                "setenforce 0 2>/dev/null || true\n" +
                "pkill -f frida-inject 2>/dev/null || true\n" +
                "pkill frida-server 2>/dev/null || true\n" +
                "sleep 1\n" +
                "nohup " + REMOTE_DIR + "/frida-server -D >" + REMOTE_DIR + "/frida-server.log 2>&1 &\n" +
                "sleep 2\n" +
                "PID=$(pidof vcplax 2>/dev/null || true)\n" +
                "if [ -z \"$PID\" ]; then echo ERR_NO_VCPLAX; exit 2; fi\n" +
                "echo VCPLAX_PID=$PID\n" +
                "echo CAPTURE_ATTACH_START >> " + LOG_PUBLIC + "\n" +
                "nohup " + REMOTE_DIR + "/frida-inject -p $PID --runtime=qjs \\\n" +
                "  -s " + REMOTE_DIR + "/frida_hook_libvc.js \\\n" +
                "  >> " + LOG_PUBLIC + " 2>&1 &\n" +
                "echo INJECT_PID=$!\n" +
                "echo server_name=" + server + "\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("re/attach", r.all());
        return r.all();
    }

    /** Spawn vcplax under Frida — best for HOOK_SYM / XOR before libvc init. */
    public String startSpawnCapture() {
        truncatePublicLog();
        String server = RootBootstrap.FIXED_SERVICE_NAME;
        String script = "set -e\n" +
                "setenforce 0 2>/dev/null || true\n" +
                "pkill -f frida-inject 2>/dev/null || true\n" +
                "killall vcplax 2>/dev/null || true\n" +
                "pkill frida-server 2>/dev/null || true\n" +
                "sleep 1\n" +
                "nohup " + REMOTE_DIR + "/frida-server -D >" + REMOTE_DIR + "/frida-server.log 2>&1 &\n" +
                "sleep 2\n" +
                "EXEC=/data/vcplax\n" +
                "[ -x /data/vcplax ] || EXEC=/data/camera/vcplax\n" +
                "if [ ! -x \"$EXEC\" ]; then echo ERR_NO_VCPLAX_BIN; exit 3; fi\n" +
                "echo CAPTURE_SPAWN_START >> " + LOG_PUBLIC + "\n" +
                "nohup " + REMOTE_DIR + "/frida-inject -f \"$EXEC\" --runtime=qjs \\\n" +
                "  -s " + REMOTE_DIR + "/frida_hook_libvc.js -- \\\n" +
                "  \"$EXEC\" " + server + " \\\n" +
                "  >> " + LOG_PUBLIC + " 2>&1 &\n" +
                "echo SPAWN_INJECT_PID=$!\n" +
                "echo server_name=" + server + "\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("re/spawn", r.all());
        return r.all();
    }

    public String stopCapture() {
        String script = "pkill -f frida-inject 2>/dev/null || true\n" +
                "pkill frida-server 2>/dev/null || true\n" +
                "echo CAPTURE_STOPPED >> " + LOG_PUBLIC + " 2>/dev/null || true\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("re/stop", r.all());
        return r.all();
    }

    public String tailPublicLog(int lines) {
        Shell.Result r = Shell.su("tail -n " + lines + " " + LOG_PUBLIC + " 2>/dev/null || echo '(log empty)'");
        return r.out;
    }

    public String status() {
        return Shell.su(
                "echo ---root---\n id\n" +
                "echo ---selinux---\n getenforce 2>/dev/null || true\n" +
                "echo ---vcplax---\n pidof vcplax 2>/dev/null || echo none\n" +
                "echo ---frida---\n ps -A 2>/dev/null | grep -E 'frida|inject' || ps | grep -E 'frida|inject' || true\n" +
                "echo ---binaries---\n ls -l " + REMOTE_DIR + "/frida-server " + REMOTE_DIR + "/frida-inject " +
                REMOTE_DIR + "/frida_hook_libvc.js 2>&1\n" +
                "echo ---log-size---\n wc -c " + LOG_PUBLIC + " 2>/dev/null || echo 0\n"
        ).all();
    }

    private void truncatePublicLog() {
        Shell.su("mkdir -p /sdcard/Download\n: > " + LOG_PUBLIC + "\n");
    }

    private static String preferredAbi() {
        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) return "arm64";
            if ("armeabi-v7a".equals(abi)) return "arm";
        }
        return "arm64";
    }

    private boolean assetExists(String path) {
        try {
            InputStream in = ctx.getAssets().open(path);
            in.close();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void copyAsset(String assetPath, File dest) throws Exception {
        dest.getParentFile().mkdirs();
        try (InputStream in = new BufferedInputStream(ctx.getAssets().open(assetPath));
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
        }
        log.log("re/asset", "copied " + assetPath + " -> " + dest.getAbsolutePath() + " (" + dest.length() + " bytes)");
    }

    private void downloadFrida(String tool, String version, String abi, File dest) throws Exception {
        String name = tool + "-" + version + "-android-" + abi;
        String urlStr = "https://github.com/frida/frida/releases/download/" + version + "/" + name;
        log.log("re/download", "GET " + urlStr);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.connect();
        if (conn.getResponseCode() != 200) {
            throw new IllegalStateException("HTTP " + conn.getResponseCode() + " for " + urlStr);
        }
        dest.getParentFile().mkdirs();
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[65536];
            int n;
            long total = 0;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
                total += n;
            }
            log.log("re/download", name + " ok " + total + " bytes");
        } finally {
            conn.disconnect();
        }
    }
}
