package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Process-wide serialized backend apply queue.
 *
 * v19 replaces the old pendingApplyPath/pendingApplySource/pendingApplyForce triplet with
 * one immutable request snapshot. New requests coalesce by replacing the pending request;
 * the worker drains the latest request via getAndSet(null), so rapid transform taps cannot
 * produce mixed path/source/force state.
 */
public final class BackendApplyQueue {
    private static final long POST_REPLAY_COOLDOWN_MS = 650L;
    private static volatile BackendApplyQueue instance;

    public static BackendApplyQueue get(Context context) {
        Context app = context.getApplicationContext();
        BackendApplyQueue local = instance;
        if (local == null) {
            synchronized (BackendApplyQueue.class) {
                local = instance;
                if (local == null) {
                    local = new BackendApplyQueue(app);
                    instance = local;
                }
            }
        }
        return local;
    }

    public static final class ApplyRequest {
        public final String path;
        public final String source;
        public final boolean force;
        public final long sequence;
        public final long createdAtMs;

        ApplyRequest(String path, String source, boolean force, long sequence) {
            this.path = path;
            this.source = source == null ? "queued" : source;
            this.force = force;
            this.sequence = sequence;
            this.createdAtMs = System.currentTimeMillis();
        }
    }

    private final Context context;
    private final SharedPreferences prefs;
    private final AppLogger log;
    private final RootBootstrap root;
    private final VliveBinderClient binder;
    private final Object backendLock = new Object();
    private final AtomicReference<ApplyRequest> pending = new AtomicReference<>();
    private final AtomicBoolean worker = new AtomicBoolean(false);
    private long nextSeq = 1L;

    private BackendApplyQueue(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        this.log = new AppLogger(context);
        this.root = new RootBootstrap(context, log);
        this.binder = new VliveBinderClient(log);
        this.binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
    }

    public boolean isRunning() { return worker.get(); }

    public void enqueue(String path, String source, boolean force) {
        if (path == null || path.trim().isEmpty()) return;
        ApplyRequest req;
        synchronized (this) {
            req = new ApplyRequest(path, source, force, nextSeq++);
        }
        pending.set(req);
        prefs.edit().putString("IceCamState", "APPLY_QUEUED").apply();
        log.log("applyq", "queued #" + req.sequence + " source=" + req.source + " force=" + req.force + " running=" + worker.get() + " path=" + req.path);
        startWorkerIfNeeded();
    }

    private void startWorkerIfNeeded() {
        if (!worker.compareAndSet(false, true)) return;
        new Thread(this::drain, "icecam-backend-applyq").start();
    }

    private void drain() {
        try {
            while (true) {
                ApplyRequest req = pending.getAndSet(null);
                if (req == null) break;
                if (!req.force && !prefs.getBoolean("ReplacementActive", false)) {
                    log.log("applyq", "skip inactive #" + req.sequence + " path=" + req.path);
                    continue;
                }

                boolean ok = legacyApplyMediaOnce(req, false);
                if (!ok) {
                    log.log("applyq", "first apply failed for #" + req.sequence + "; restarting daemon");
                    binder.clearCache();
                    prefs.edit().putString("IceCamState", "RECOVERING_BACKEND").apply();
                    root.bootstrap();
                    sleepMs(700);
                    binder.clearCache();

                    ApplyRequest latest = pending.getAndSet(null);
                    ApplyRequest retry = latest != null ? latest : req;
                    ok = legacyApplyMediaOnce(retry, true);
                    log.log("applyq", "retry result ok=" + ok + " request=#" + retry.sequence + " path=" + retry.path);
                }

                sleepMs(POST_REPLAY_COOLDOWN_MS);
            }
        } finally {
            worker.set(false);
            if (pending.get() != null) startWorkerIfNeeded();
        }
    }

    private boolean legacyApplyMediaOnce(ApplyRequest req, boolean retry) {
        synchronized (backendLock) {
            try {
                prefs.edit().putString("IceCamState", "APPLYING_MEDIA").apply();
                File f = new File(req.path);
                long t0 = android.os.SystemClock.elapsedRealtime();
                log.log("applyq", "legacy apply " + (retry ? "retry" : "start") + " #" + req.sequence + " source=" + req.source + " exists=" + f.exists() + " size=" + (f.exists() ? f.length() : -1L) + " path=" + req.path);
                binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
                Shell.su("setenforce 0 2>/dev/null || true\nservice check " + RootBootstrap.FIXED_SERVICE_NAME + " 2>&1 || true\n");
                if (!binder.connected()) {
                    binder.clearCache();
                    sleepMs(250);
                }
                long tx14Start = android.os.SystemClock.elapsedRealtime();
                int mode = binder.setModeString(1, req.path); // TX14 mode 1 -> path
                long tx14Ms = android.os.SystemClock.elapsedRealtime() - tx14Start;
                sleepMs(520);
                StreamGeometry geom = StreamGeometry.load(prefs);
                long tx11Start = android.os.SystemClock.elapsedRealtime();
                boolean loop = prefs.getBoolean("PlayisLoop", true);
                int play = binder.playSource(req.path, loop);
                long tx11Ms = android.os.SystemClock.elapsedRealtime() - tx11Start;
                sleepMs(150);
                binder.setAngle(geom.angleDegrees());
                binder.setMirror(geom.mirrorH);
                binder.setLoop(loop);
                binder.setAutoRotate(geom.autoRotate);
                boolean active = mode >= 0 && play >= 0;
                prefs.edit()
                        .putBoolean("ReplacementActive", active)
                        .putString("IceCamState", active ? "REPLACEMENT_ACTIVE" : "PLAY_ERROR")
                        .apply();
                log.log("applyq", "legacy apply done #" + req.sequence + " TX14=" + mode + "(" + tx14Ms + "ms) TX11=" + play + "(" + tx11Ms + "ms) total=" + (android.os.SystemClock.elapsedRealtime() - t0) + "ms active=" + active);
                if (!active) binder.clearCache();
                return active;
            } catch (Throwable t) {
                prefs.edit().putBoolean("ReplacementActive", false).putString("IceCamState", "PLAY_ERROR").apply();
                binder.clearCache();
                log.log("applyq", "legacy apply exception #" + req.sequence + ": " + t);
                return false;
            }
        }
    }

    private static void sleepMs(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
