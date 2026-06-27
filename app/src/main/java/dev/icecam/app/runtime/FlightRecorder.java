package dev.icecam.app.runtime;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import dev.icecam.app.AppLogger;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class FlightRecorder {
    private final File dir;
    private final AppLogger log;
    public FlightRecorder(Context ctx, AppLogger log) {
        this.dir = new File(ctx.getExternalFilesDir(null), "flight-recorder");
        if (!dir.exists()) dir.mkdirs();
        this.log = log;
    }
    public void enqueue(RuntimeCommand c, AppState before) { append("commands.log", line(c, "enqueue", before, null, "")); }
    public void start(RuntimeCommand c, AppState before) { append("timeline.log", line(c, "start", before, null, "")); }
    public void finish(RuntimeCommand c, AppState before, AppState after, RuntimeTypes.Result result) {
        append("commands.log", line(c, "finish", before, after, result.name()));
        append("state.log", "#" + c.id + " before=" + summarize(before) + " after=" + summarize(after) + "\n");
    }
    public void performance(RuntimeCommand c, long startMs, long finishMs) {
        append("performance.log", String.format(Locale.US, "#%d %s queueDelayMs=%d runMs=%d\n", c.id, c.type, Math.max(0, startMs - c.enqueueRealtimeMs), Math.max(0, finishMs - startMs)));
    }
    public File exportZip(Context ctx) {
        try {
            writeDevice(ctx);
            File zip = new File(ctx.getExternalFilesDir(null), "IceCam_Report.zip");
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
                for (String n : new String[]{"commands.log", "timeline.log", "state.log", "performance.log", "device.txt"}) {
                    File f = new File(dir, n);
                    if (!f.exists()) continue;
                    out.putNextEntry(new ZipEntry(n));
                    java.nio.file.Files.copy(f.toPath(), out);
                    out.closeEntry();
                }
            }
            return zip;
        } catch (Throwable t) { if (log != null) log.log("flight", "export failed: " + t); return null; }
    }
    private void writeDevice(Context ctx) {
        appendOverwrite("device.txt", "time=" + now() + "\npackage=" + ctx.getPackageName() + "\nsdk=" + Build.VERSION.SDK_INT + "\ndevice=" + Build.MANUFACTURER + " " + Build.MODEL + "\nabis=" + java.util.Arrays.toString(Build.SUPPORTED_ABIS) + "\n");
    }
    private String line(RuntimeCommand c, String phase, AppState before, AppState after, String result) {
        return now() + " +" + SystemClock.elapsedRealtime() + "ms #" + c.id + " source=" + c.source + " type=" + c.type + " op=" + c.op + " phase=" + phase + " result=" + result + " state=" + summarize(after == null ? before : after) + "\n";
    }
    private static String summarize(AppState s) {
        if (s == null) return "<null>";
        return "media=" + s.media.activeSlot + ":" + shortPath(s.media.originalPath) + " backend=" + s.backend.phase + " active=" + s.backend.replacementActive + " ops=" + s.backend.operations.summary() + " tx=" + s.transform.summary();
    }
    private static String shortPath(String p) { if (p == null) return ""; int i = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\')); return i >= 0 ? p.substring(i + 1) : p; }
    private static String now() { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()); }
    private synchronized void append(String name, String s) { try (FileOutputStream out = new FileOutputStream(new File(dir, name), true)) { out.write(s.getBytes("UTF-8")); } catch (Throwable ignored) {} }
    private synchronized void appendOverwrite(String name, String s) { try (FileOutputStream out = new FileOutputStream(new File(dir, name), false)) { out.write(s.getBytes("UTF-8")); } catch (Throwable ignored) {} }
}
