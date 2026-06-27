package dev.icecam.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.util.concurrent.atomic.AtomicLong;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class AppLogger {
    public interface Listener { void onLogChanged(String text); }
    private static final int MAX = 96000;
    private static final long PROCESS_START_MS = SystemClock.elapsedRealtime();
    private static final AtomicLong SEQ = new AtomicLong(1L);
    private final StringBuilder buffer = new StringBuilder();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final File file;
    private Listener listener;

    public AppLogger(Context ctx) {
        file = new File(ctx.getExternalFilesDir(null), "icecam-runtime.log");
        log("logger", "file=" + file.getAbsolutePath());
    }

    public void setListener(Listener l) { listener = l; if (l != null) l.onLogChanged(buffer.toString()); }
    public File file() { return file; }
    public String text() { return buffer.toString(); }

    public void log(String tag, String msg) {
        long id = SEQ.getAndIncrement();
        long up = SystemClock.elapsedRealtime() - PROCESS_START_MS;
        Runtime rt = Runtime.getRuntime();
        long usedKb = (rt.totalMemory() - rt.freeMemory()) / 1024L;
        long maxKb = rt.maxMemory() / 1024L;
        String line = String.format(Locale.US,
                "%s #%05d +%07dms [%s] {%s mem=%d/%dKB} %s\n",
                new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date()),
                id, up, tag, Thread.currentThread().getName(), usedKb, maxKb,
                String.valueOf(msg).replace('\r', ' '));
        synchronized (buffer) {
            buffer.insert(0, line);
            if (buffer.length() > MAX) buffer.setLength(MAX);
            try (FileOutputStream out = new FileOutputStream(file, true)) { out.write(line.getBytes("UTF-8")); } catch (Throwable ignored) {}
        }
        main.post(() -> { if (listener != null) listener.onLogChanged(buffer.toString()); });
    }

    public void logDivider(String tag, String title) {
        log(tag, "---------------- " + title + " ----------------");
    }

    public void logBlock(String tag, String block) {
        if (block == null || block.length() == 0) { log(tag, "<empty>"); return; }
        String[] lines = block.split("\\n");
        for (String l : lines) if (l.trim().length() > 0) log(tag, l);
    }
}
