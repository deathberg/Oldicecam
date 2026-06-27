package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

/**
 * Unified stream control aligned with RE (docs/RE_FINAL_REPORT.md, MyBinderClient):
 *
 * TX11/14 — media path
 * TX16/17/18/19 — autoRotate, loop, angle, mirror (native MediaContext)
 * TX24 — color injection ONLY (never pan/zoom)
 *
 * Images: geometry baked to JPEG → TX14→TX11 (debounced).
 * Video: TX18/TX19/TX17 sent live; pan/zoom preview-only until clone pipeline.
 */
public final class StreamController {
    public enum Source { MAIN, FLOAT }

    private static volatile StreamController instance;
    private static final long BAKE_DEBOUNCE_MS = 450L;

    private final Context app;
    private final SharedPreferences prefs;
    private final SmartLogger slog;
    private final RootBootstrap root;
    private final VliveBinderClient binder;
    private final Handler main = new Handler(Looper.getMainLooper());
    private Runnable pendingBake;
    private volatile boolean busy;

    public static StreamController get(Context ctx) {
        StreamController local = instance;
        if (local == null) {
            synchronized (StreamController.class) {
                local = instance;
                if (local == null) instance = local = new StreamController(ctx.getApplicationContext());
            }
        }
        return local;
    }

    private StreamController(Context context) {
        app = context;
        prefs = app.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        slog = SmartLogger.get(app);
        root = new RootBootstrap(app, slog.base());
        binder = new VliveBinderClient(slog.base());
        binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
    }

    public boolean isBusy() { return busy || BackendApplyQueue.get(app).isRunning(); }
    public VliveBinderClient binder() { return binder; }

    public StreamGeometry geometry() { return StreamGeometry.load(prefs); }

    public StreamGeometry mutate(Source source, String op) {
        StreamGeometry g = StreamGeometry.load(prefs);
        g.applyOp(op);
        g.save(prefs);
        slog.d("stream", source + " op=" + op + " " + g.summary());

        if (prefs.getBoolean("ReplacementActive", false)) {
            pushNativeGeometry(g);
        }
        return g;
    }

    public void selectSlot(Source source, int slot, String path) {
        slot = Math.max(1, Math.min(4, slot));
        prefs.edit()
                .putInt("ActiveSlot", slot)
                .putString("Slot" + slot + "Path", path)
                .putString("OriginalPlayFileMp4", path)
                .putString("PlayFileMp4", path)
                .putInt("PlayFileType", MediaPreviewEngine.isVideoPath(path) ? 2 : 1)
                .apply();
        StreamGeometry.resetGeometry(prefs);
        slog.event("stream", "select", "M" + slot + " " + path);
        if (prefs.getBoolean("ReplacementActive", false)) {
            applyMedia(path, source.name().toLowerCase() + "-slot", true);
        }
    }

    public void setLoop(Source source, boolean loop) {
        prefs.edit().putBoolean("PlayisLoop", loop).apply();
        if (prefs.getBoolean("ReplacementActive", false)) {
            binder.setLoop(loop);
        }
        slog.d("stream", "loop=" + loop);
    }

    public void commit(Source source) {
        String path = activePlayPath();
        if (path.isEmpty()) {
            slog.w("stream", "commit skipped — no media");
            return;
        }
        applyMedia(path, source.name().toLowerCase() + "-commit", true);
    }

    public void startReplacement(Source source) {
        if (busy) return;
        busy = true;
        prefs.edit().putString("IceCamState", "STARTING").apply();
        new Thread(() -> {
            try {
                root.bootstrap();
                binder.clearCache();
                sleep(400);
                String path = activePlayPath();
                if (path.isEmpty()) {
                    prefs.edit().putString("IceCamState", "NO_MEDIA").apply();
                    slog.w("stream", "start without media");
                    return;
                }
                StreamGeometry g = StreamGeometry.load(prefs);
                pushNativeGeometry(g);
                applyMedia(path, source.name().toLowerCase() + "-start", true);
                StreamForegroundService.start(app);
            } catch (Throwable t) {
                slog.e("stream", "start failed: " + t.getMessage());
                prefs.edit().putString("IceCamState", "START_ERROR").apply();
            } finally {
                busy = false;
            }
        }, "icecam-start").start();
    }

    public void restoreCamera(Source source) {
        if (busy) return;
        busy = true;
        main.removeCallbacks(pendingBake);
        new Thread(() -> {
            try {
                root.restoreCamera();
                binder.clearCache();
                prefs.edit()
                        .putBoolean("ReplacementActive", false)
                        .putString("IceCamState", "IDLE")
                        .apply();
                StreamForegroundService.stop(app);
                slog.i("stream", "restored camera");
            } finally {
                busy = false;
            }
        }, "icecam-restore").start();
    }

    private void pushNativeGeometry(StreamGeometry g) {
        String path = activePlayPath();
        if (MediaPreviewEngine.isVideoPath(path)) {
            binder.setAngle(g.angleDegrees());
            binder.setMirror(g.mirrorH);
            binder.setLoop(prefs.getBoolean("PlayisLoop", true));
            binder.setAutoRotate(g.autoRotate);
            slog.d("stream", "video native TX18=" + g.angleDegrees() + " TX19=" + g.mirrorH);
        } else if (MediaPreviewEngine.isImagePath(path)) {
            scheduleImageBake(g);
        }
    }

    private void scheduleImageBake(StreamGeometry g) {
        if (pendingBake != null) main.removeCallbacks(pendingBake);
        final StreamGeometry snap = StreamGeometry.copy(g);
        pendingBake = () -> {
            String original = prefs.getString("OriginalPlayFileMp4", "");
            if (original.isEmpty() || !MediaPreviewEngine.isImagePath(original)) return;
            String baked = MediaTransformer.bakeImage(app, original, snap, slog.base());
            prefs.edit().putString("PlayFileMp4", baked).putString("BakedPlayFileMp4", baked).apply();
            BackendApplyQueue.get(app).enqueue(baked, "geometry-bake", false);
            slog.i("stream", "baked replay " + baked);
        };
        main.postDelayed(pendingBake, BAKE_DEBOUNCE_MS);
    }

    private void applyMedia(String path, String reason, boolean force) {
        if (MediaPreviewEngine.isImagePath(path)) {
            StreamGeometry g = StreamGeometry.load(prefs);
            String baked = MediaTransformer.bakeImage(app, path, g, slog.base());
            prefs.edit().putString("PlayFileMp4", baked).apply();
            BackendApplyQueue.get(app).enqueue(baked, reason, force);
        } else {
            BackendApplyQueue.get(app).enqueue(path, reason, force);
        }
    }

    private String activePlayPath() {
        String p = prefs.getString("OriginalPlayFileMp4", "");
        if (p == null || p.isEmpty()) p = prefs.getString("PlayFileMp4", "");
        return p == null ? "" : p;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
