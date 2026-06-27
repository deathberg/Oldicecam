package dev.icecam.app;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class NativeExtractor {
    public static final class Result {
        public final String abi;
        public final File dir;
        public final boolean ok;
        public final String log;
        Result(String abi, File dir, boolean ok, String log) { this.abi = abi; this.dir = dir; this.ok = ok; this.log = log; }
    }

    public static Result extract(Context ctx, AppLogger logger) {
        StringBuilder sb = new StringBuilder();
        String abi = selectCameraServerAbi();
        File outDir = new File(ctx.getFilesDir(), "native-" + abi);
        boolean ok = true;
        try {
            if (!outDir.exists() && !outDir.mkdirs()) sb.append("mkdir failed: ").append(outDir).append('\n');
            ZipFile zip = new ZipFile(ctx.getApplicationInfo().sourceDir);
            String[] names = {"libvc.so", "libshadowhook.so", "vcplax.so"};
            // Package ShadowHook 2.x as libshadowhook.so + libvc.so + vcplax.so per ABI in APK lib/<abi>/
            for (String n : names) {
                String entryName = "lib/" + abi + "/" + n;
                ZipEntry e = zip.getEntry(entryName);
                if (e == null) { ok = false; sb.append("missing ").append(entryName).append('\n'); continue; }
                File dst = new File(outDir, n);
                copy(zip.getInputStream(e), dst);
                sb.append("extracted ").append(entryName).append(" -> ").append(dst.getAbsolutePath()).append(" size=").append(dst.length()).append('\n');
            }
            zip.close();
        } catch (Throwable t) { ok = false; sb.append("extract error: ").append(t).append('\n'); }
        Result r = new Result(abi, outDir, ok, sb.toString());
        if (logger != null) logger.logBlock("extract", r.log);
        return r;
    }

    private static void copy(InputStream in, File dst) throws Exception {
        try (InputStream input = in; FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[128 * 1024];
            int n;
            while ((n = input.read(buf)) > 0) out.write(buf, 0, n);
        }
    }

    private static String selectCameraServerAbi() {
        Shell.Result r = Shell.sh("file /system/bin/cameraserver 2>/dev/null || true");
        String s = (r.out + "\n" + r.err).toLowerCase();
        return s.contains("32-bit") ? "armeabi-v7a" : "arm64-v8a";
    }
}
