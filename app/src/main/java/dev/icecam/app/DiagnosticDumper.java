package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public final class DiagnosticDumper {
    private DiagnosticDumper() {}

    public static String build(Context ctx, AppLogger log, VliveBinderClient binder) {
        StringBuilder sb = new StringBuilder(16384);
        SharedPreferences prefs = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        sb.append("===== ICECAM DIAGNOSTIC SNAPSHOT =====\n");
        sb.append("time=").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date())).append('\n');
        sb.append("package=").append(ctx.getPackageName()).append('\n');
        sb.append("icecamBuild=").append(BuildInfo.BUILD_LABEL).append(' ').append(BuildInfo.VERSION_NAME).append(" code=").append(BuildInfo.VERSION_CODE).append('\n');
        sb.append("build=SDK ").append(Build.VERSION.SDK_INT).append(" device=").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append(" abi=");
        if (Build.SUPPORTED_ABIS != null) for (String a : Build.SUPPORTED_ABIS) sb.append(a).append(' ');
        sb.append("\n");
        Runtime rt = Runtime.getRuntime();
        sb.append("javaHeapKB used=").append((rt.totalMemory() - rt.freeMemory()) / 1024L)
                .append(" total=").append(rt.totalMemory() / 1024L)
                .append(" max=").append(rt.maxMemory() / 1024L).append('\n');
        sb.append("logFile=").append(log != null && log.file() != null ? log.file().getAbsolutePath() : "<none>").append('\n');
        sb.append("\n--- prefs ---\n");
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        sb.append("\n--- files ---\n");
        appendDir(sb, ctx.getFilesDir(), "files", 2);
        appendDir(sb, ctx.getExternalFilesDir(null), "externalFiles", 2);
        File baked = new File(ctx.getExternalFilesDir(null), "baked");
        appendDir(sb, baked, "baked", 64);
        sb.append("\n--- binder-java ---\n");
        try { sb.append(binder != null ? binder.diagnostics() : "binder=null\n"); } catch (Throwable t) { sb.append("binder diagnostics failed: ").append(t).append('\n'); }
        sb.append("\n--- root-native ---\n");
        try {
            Shell.Result r = Shell.su("" +
                    "echo ---id---; id; " +
                    "echo ---getenforce---; getenforce 2>/dev/null; " +
                    "echo ---service-check---; service check privsam_service 2>&1; " +
                    "echo ---process---; ps -A | grep -iE 'vcplax|privsam' 2>/dev/null; " +
                    "echo ---data-camera---; ls -l /data/camera 2>&1; " +
                    "echo ---data-vcplax---; ls -l /data/vcplax /data/libvc.so /data/libvc++.so 2>&1; " +
                    "echo ---vcplax.log---; tail -120 /data/camera/vcplax.log 2>&1; " +
                    "echo ---vcplax.err---; tail -120 /data/camera/vcplax.err 2>&1; " +
                    "echo ---logcat-icecam---; logcat -d -v time -t 300 | grep -iE 'IceCam|icecam|vcplax|privsam|vlive|MediaCodec|BufferQueue|GraphicBuffer' 2>/dev/null");
            sb.append(r.all()).append('\n');
        } catch (Throwable t) { sb.append("root dump failed: ").append(t).append('\n'); }
        sb.append("===== END ICECAM DIAGNOSTIC SNAPSHOT =====\n");
        return sb.toString();
    }

    private static void appendDir(StringBuilder sb, File dir, String label, int max) {
        sb.append(label).append('=').append(dir == null ? "<null>" : dir.getAbsolutePath()).append('\n');
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        int n = Math.min(files.length, max);
        for (int i = 0; i < n; i++) {
            File f = files[i];
            sb.append("  ").append(f.isDirectory() ? "d " : "f ").append(f.length()).append(' ')
                    .append(new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date(f.lastModified())))
                    .append(' ').append(f.getName()).append('\n');
        }
        if (files.length > n) sb.append("  ... total=").append(files.length).append('\n');
    }
}
