package dev.icecam.retool;

import android.content.Context;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Root companion for testicecam2.apk — Frida deploy, auto-launch capture session.
 */
public final class ReToolEngine {
    public static final String FRIDA_VERSION = "16.5.9";
    public static final String REMOTE_DIR = "/data/local/tmp";
    public static final String LOG_PUBLIC = "/sdcard/Download/re_tool_capture.log";
    public static final String PULL_DIR = "/sdcard/Download/re_tool";
    public static final String SESSION_FLAG = REMOTE_DIR + "/re_tool_session_active";
    public static final String WATCHDOG_SCRIPT = REMOTE_DIR + "/re_tool_watchdog.sh";
    public static final String SESSION_SCRIPT = REMOTE_DIR + "/re_tool_start_session.sh";

    private static final String[] TARGET_PACKAGES = {
            "com.potplayer.music",
            "com.xiaomi.vlive"
    };

    private static final String[] ASSET_SCRIPTS = {
            "re/frida_hook_libvc.js",
            "re/re_tool_watchdog.sh",
            "re/re_tool_start_session.sh"
    };

    private final Context ctx;
    private final AppLogger log;

    public ReToolEngine(Context c, AppLogger logger) {
        ctx = c.getApplicationContext();
        log = logger;
    }

    /** Deploy Frida, hook script, watchdog + session launcher. */
    public String setupAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RE Tool setup ===\n");
        sb.append(Shell.su("id\ngetenforce 2>/dev/null || true\nsetenforce 0 2>/dev/null || true\n").all());
        sb.append(deployAllAssets()).append('\n');
        sb.append(ensureFridaBinaries()).append('\n');
        sb.append("=== setup end ===\n");
        return sb.toString();
    }

    /**
     * One-tap capture: start 20ms watchdog, force-stop target, launch it.
     * Watchdog injects Frida into vcplax the moment the target app starts the daemon.
     */
    public String startAutoSession() {
        String script =
                "set -e\n" +
                "if [ ! -x " + SESSION_SCRIPT + " ]; then echo ERR_RUN_SETUP; exit 1; fi\n" +
                "sh " + SESSION_SCRIPT + "\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("auto-session", r.all());
        return r.all();
    }

    public String detectTargetApp() {
        Shell.Result r = Shell.sh(detectTargetAppShell() + "\n" +
                "pm list packages 2>/dev/null | grep -iE 'potplayer|vlive' || true\n");
        log.logBlock("detect/app", r.all());
        return r.all();
    }

    public String pullRuntimeArtifacts() {
        String script =
                "set -e\n" +
                "OUT=" + Shell.q(PULL_DIR) + "\n" +
                "mkdir -p \"$OUT/deployed\" \"$OUT/proc\"\n" +
                "echo pulled_at=$(date) > \"$OUT/README.txt\"\n" +
                detectTargetAppShell() + "\n" +
                "for f in /data/vcplax /data/libvc.so /data/libvc++.so /data/camera/vcplax /data/camera/libvc.so " +
                REMOTE_DIR + "/vcplax.real; do\n" +
                "  if [ -r \"$f\" ]; then\n" +
                "    cp -f \"$f\" \"$OUT/deployed/$(basename \"$f\")\" 2>/dev/null || " +
                "cat \"$f\" > \"$OUT/deployed/$(basename \"$f\")\"\n" +
                "    ls -l \"$OUT/deployed/$(basename \"$f\")\"\n" +
                "  else\n" +
                "    echo MISSING \"$f\" >> \"$OUT/deployed/MISSING.txt\"\n" +
                "  fi\n" +
                "done\n" +
                "PID=$(pidof vcplax 2>/dev/null || true)\n" +
                "echo vcplax_pid=$PID > \"$OUT/vcplax_pid.txt\"\n" +
                "if [ -n \"$PID\" ]; then\n" +
                "  tr '\\0' ' ' < /proc/$PID/cmdline > \"$OUT/vcplax_cmdline.txt\"\n" +
                "  cat /proc/$PID/maps > \"$OUT/vcplax_maps.txt\" 2>/dev/null || true\n" +
                "fi\n" +
                "echo PULL_OK dir=$OUT\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("pull", r.all());
        return r.all();
    }

    /** Manual attach — if vcplax already running. */
    public String startAttachCapture() {
        truncatePublicLog();
        String script =
                "set -e\n" +
                "setenforce 0 2>/dev/null || true\n" +
                "pkill -f frida-inject 2>/dev/null || true\n" +
                "nohup " + REMOTE_DIR + "/frida-server -D >" + REMOTE_DIR + "/frida-server.log 2>&1 &\n" +
                "sleep 2\n" +
                "PID=$(pidof vcplax 2>/dev/null || true)\n" +
                "if [ -z \"$PID\" ]; then echo ERR_NO_VCPLAX; exit 2; fi\n" +
                "echo CAPTURE_ATTACH_START >> " + LOG_PUBLIC + "\n" +
                "nohup " + REMOTE_DIR + "/frida-inject -p $PID --runtime=qjs \\\n" +
                "  -s " + REMOTE_DIR + "/frida_hook_libvc.js >> " + LOG_PUBLIC + " 2>&1 &\n" +
                "echo INJECT_PID=$! VCPLAX_PID=$PID\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("attach", r.all());
        return r.all();
    }

    public String stopCapture() {
        String script =
                "rm -f " + SESSION_FLAG + "\n" +
                "pkill -f re_tool_watchdog.sh 2>/dev/null || true\n" +
                "pkill -f frida-inject 2>/dev/null || true\n" +
                "pkill frida-server 2>/dev/null || true\n" +
                "echo CAPTURE_STOPPED >> " + LOG_PUBLIC + " 2>/dev/null || true\n" +
                "wc -c " + LOG_PUBLIC + " 2>/dev/null || true\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("stop", r.all());
        return r.all();
    }

    public String tailPublicLog(int lines) {
        return Shell.su("tail -n " + lines + " " + LOG_PUBLIC + " 2>/dev/null || echo '(log empty)'").out;
    }

    public String status() {
        return Shell.su(
                "echo ---root---\n id\n" +
                "echo ---session---\n" +
                "if [ -f " + SESSION_FLAG + " ]; then echo ACTIVE; else echo idle; fi\n" +
                "pgrep -af re_tool_watchdog 2>/dev/null || echo watchdog=off\n" +
                "echo ---target---\n" + detectTargetAppShell() + "\n" +
                "echo ---vcplax---\n pidof vcplax 2>/dev/null || echo none\n" +
                "if pidof vcplax >/dev/null 2>&1; then tr '\\0' ' ' < /proc/$(pidof vcplax)/cmdline; echo; fi\n" +
                "echo ---frida---\n ps -A 2>/dev/null | grep -E 'frida|inject' || true\n" +
                "echo ---deploy---\n ls -l " + REMOTE_DIR + "/frida-server " + REMOTE_DIR + "/frida-inject " +
                WATCHDOG_SCRIPT + " " + SESSION_SCRIPT + " 2>&1\n" +
                "echo ---log---\n wc -c " + LOG_PUBLIC + " 2>/dev/null || echo 0\n"
        ).all();
    }

    private String detectTargetAppShell() {
        return "for PKG in " + String.join(" ", TARGET_PACKAGES) + "; do\n" +
                "  pm path \"$PKG\" >/dev/null 2>&1 && echo target=$PKG && break\n" +
                "done";
    }

    private String deployAllAssets() {
        StringBuilder sb = new StringBuilder();
        for (String asset : ASSET_SCRIPTS) {
            String baseName = asset.substring(asset.lastIndexOf('/') + 1);
            File cached = new File(ctx.getFilesDir(), baseName);
            try {
                copyAsset(asset, cached);
            } catch (Throwable t) {
                sb.append("FAIL ").append(asset).append(": ").append(t).append('\n');
                continue;
            }
            String remote = REMOTE_DIR + "/" + baseName;
            String chmod = baseName.endsWith(".sh") ? "chmod 755 " + remote + "\n" : "chmod 644 " + remote + "\n";
            String deploy =
                    "cp -f " + Shell.q(cached.getAbsolutePath()) + " " + remote + "\n" +
                    chmod +
                    "wc -c " + remote + "\n";
            Shell.Result r = Shell.su(deploy);
            sb.append(baseName).append('\n').append(r.all()).append('\n');
        }
        log.logBlock("deploy-assets", sb.toString());
        return sb.toString();
    }

    private String ensureFridaBinaries() {
        String abi = preferredAbi();
        File localServer = new File(ctx.getFilesDir(), "frida-server-" + FRIDA_VERSION + "-" + abi);
        File localInject = new File(ctx.getFilesDir(), "frida-inject-" + FRIDA_VERSION + "-" + abi);

        String check = Shell.su(
                "test -x " + REMOTE_DIR + "/frida-server && test -x " + REMOTE_DIR + "/frida-inject && echo HAVE_BOTH || echo MISSING"
        ).out.trim();

        if ("HAVE_BOTH".equals(check)) {
            log.log("frida", "using existing " + REMOTE_DIR);
            return "already deployed in " + REMOTE_DIR;
        }

        if (!localServer.exists() || localServer.length() < 1_000_000L) {
            tryDownloadOrAsset("frida-server", FRIDA_VERSION, abi, localServer);
        }
        if (!localInject.exists() || localInject.length() < 1_000_000L) {
            tryDownloadOrAsset("frida-inject", FRIDA_VERSION, abi, localInject);
        }

        if (!localServer.exists() || localServer.length() < 1_000_000L) {
            return "frida-server missing — need Internet on Setup";
        }
        if (!localInject.exists() || localInject.length() < 1_000_000L) {
            return "frida-inject missing — need Internet on Setup";
        }

        String deploy =
                "set -e\n" +
                "cp -f " + Shell.q(localServer.getAbsolutePath()) + " " + REMOTE_DIR + "/frida-server\n" +
                "cp -f " + Shell.q(localInject.getAbsolutePath()) + " " + REMOTE_DIR + "/frida-inject\n" +
                "chmod 755 " + REMOTE_DIR + "/frida-server " + REMOTE_DIR + "/frida-inject\n" +
                "ls -l " + REMOTE_DIR + "/frida-server " + REMOTE_DIR + "/frida-inject\n";
        Shell.Result r = Shell.su(deploy);
        log.logBlock("deploy-frida", r.all());
        return r.all();
    }

    private void truncatePublicLog() {
        Shell.su("mkdir -p /sdcard/Download\n: > " + LOG_PUBLIC + "\n");
    }

    private void tryDownloadOrAsset(String tool, String version, String abi, File dest) {
        String assetName = "re/" + tool + "-" + version + "-android-" + abi;
        if (assetExists(assetName)) {
            try {
                copyAsset(assetName, dest);
                return;
            } catch (Throwable t) {
                log.log("frida", "asset " + assetName + " failed: " + t);
            }
        }
        try {
            downloadFrida(tool, version, abi, dest);
        } catch (Throwable t) {
            log.log("frida", "download " + tool + " failed: " + t);
        }
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
        log.log("asset", assetPath + " -> " + dest.length() + " bytes");
    }

    private void downloadFrida(String tool, String version, String abi, File dest) throws Exception {
        String name = tool + "-" + version + "-android-" + abi;
        String urlStr = "https://github.com/frida/frida/releases/download/" + version + "/" + name;
        log.log("download", "GET " + urlStr);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.connect();
        if (conn.getResponseCode() != 200) {
            throw new IllegalStateException("HTTP " + conn.getResponseCode());
        }
        dest.getParentFile().mkdirs();
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
        } finally {
            conn.disconnect();
        }
        log.log("download", name + " ok");
    }
}
