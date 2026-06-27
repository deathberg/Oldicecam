package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Structured debug logging for development: levels, categories, FPS, timings.
 * Toggle via prefs "DebugLogging" or BuildConfig debug.
 */
public final class SmartLogger {
    public enum Level { DEBUG, INFO, WARN, ERROR }

    private static volatile SmartLogger instance;
    private final AppLogger base;
    private final SharedPreferences prefs;
    private final ConcurrentHashMap<String, FpsWindow> fpsWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> timers = new ConcurrentHashMap<>();

    public static SmartLogger get(Context ctx) {
        SmartLogger local = instance;
        if (local == null) {
            synchronized (SmartLogger.class) {
                local = instance;
                if (local == null) instance = local = new SmartLogger(ctx.getApplicationContext());
            }
        }
        return local;
    }

    private SmartLogger(Context ctx) {
        base = new AppLogger(ctx);
        prefs = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE);
    }

    public AppLogger base() { return base; }

    public boolean debugEnabled() {
        return prefs.getBoolean("DebugLogging", true);
    }

    public void setDebugEnabled(boolean on) {
        prefs.edit().putBoolean("DebugLogging", on).apply();
    }

    public void d(String cat, String msg) { log(Level.DEBUG, cat, msg); }
    public void i(String cat, String msg) { log(Level.INFO, cat, msg); }
    public void w(String cat, String msg) { log(Level.WARN, cat, msg); }
    public void e(String cat, String msg) { log(Level.ERROR, cat, msg); }

    public void log(Level level, String cat, String msg) {
        if (level == Level.DEBUG && !debugEnabled()) return;
        String prefix = level.name().charAt(0) + "/" + cat;
        base.log(prefix, msg);
    }

    public void event(String cat, String event, String detail) {
        i(cat, event + (detail == null || detail.isEmpty() ? "" : " :: " + detail));
    }

    public void begin(String key) {
        timers.put(key, SystemClock.elapsedRealtime());
    }

    public void end(String cat, String key) {
        Long start = timers.remove(key);
        if (start == null) return;
        long ms = SystemClock.elapsedRealtime() - start;
        if (ms > 8 || debugEnabled()) d(cat, key + " took " + ms + "ms");
    }

    /** Record frame interval; logs rolling FPS every ~2s when debug on. */
    public void frame(String scope) {
        if (!debugEnabled()) return;
        FpsWindow w = fpsWindows.computeIfAbsent(scope, k -> new FpsWindow());
        w.tick(base, scope);
    }

    private static final class FpsWindow {
        long lastNs;
        int frames;
        long windowStartMs;

        void tick(AppLogger log, String scope) {
            long nowMs = SystemClock.elapsedRealtime();
            if (windowStartMs == 0) windowStartMs = nowMs;
            frames++;
            if (nowMs - windowStartMs >= 2000) {
                float fps = frames * 1000f / Math.max(1, nowMs - windowStartMs);
                log.log("fps/" + scope, String.format(Locale.US, "%.1f fps (%d frames)", fps, frames));
                frames = 0;
                windowStartMs = nowMs;
            }
            lastNs = System.nanoTime();
        }
    }
}
