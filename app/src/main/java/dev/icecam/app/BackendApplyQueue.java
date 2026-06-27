package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Process-wide serialized backend apply queue.
 *
 * Full applies (start/commit/slot) use the legacy TX14→TX11 path with recovery.
 * Geometry bakes use a soft path: shorter sleeps, no daemon bootstrap on failure.
 */
public final class BackendApplyQueue {
    private static final long POST_REPLAY_COOLDOWN_MS = 650L;
    private static final long POST_GEOMETRY_COOLDOWN_MS = 100L;
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
        public final boolean softGeometry;
        public final long sequence;
        public final long createdAtMs;

        ApplyRequest(String path, String source, boolean force, boolean softGeometry, long sequence) {
            this.path = path;
            this.source = source == null ? "queued" : source;
            this.force = force;
            this.softGeometry = softGeometry;
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
        enqueueInternal(path, source, force, false);
    }

    /** Debounced image geometry — fast path, no bootstrap recovery. */
    public void enqueueGeometry(String path, String source) {
        enqueueInternal(path, source, false, true);
    }

    private void enqueueInternal(String path, String source, boolean force, boolean softGeometry) {
        if (path == null || path.trim().isEmpty()) return;
        ApplyRequest req;
        synchronized (this) {
            req = new ApplyRequest(path, source, force, softGeometry, nextSeq++);
        }
        pending.set(req);
        prefs.edit().putString("IceCamState", softGeometry ? "APPLYING_GEOMETRY" : "APPLY_QUEUED").apply();
        log.log("applyq", "queued #" + req.sequence + " source=" + req.source
                + " force=" + req.force + " soft=" + req.softGeometry
                + " running=" + worker.get() + " path=" + req.path);
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
                if (!req.force && !req.softGeometry && !prefs.getBoolean("ReplacementActive", false)) {
                    log.log("applyq", "skip inactive #" + req.sequence + " path=" + req.path);
                    continue;
                }

                boolean ok = legacyApplyMediaOnce(req, false);
                if (!ok) {
                    if (req.softGeometry) {
                        log.log("applyq", "soft geometry retry #" + req.sequence);
                        sleepMs(60);
                        ok = legacyApplyMediaOnce(req, true);
                        if (!ok) {
                            boolean stillLive = probeNativeLive();
                            prefs.edit().putString("IceCamState",
                                    stillLive ? "GEOMETRY_SKIPPED" : "GEOMETRY_APPLY_FAIL").apply();
                            log.log("applyq", "soft geometry failed #" + req.sequence + " stillLive=" + stillLive);
                            sleepMs(POST_GEOMETRY_COOLDOWN_MS);
                            continue;
                        }
                    } else {
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
                }

                syncReplacementActive(req);
                sleepMs(req.softGeometry ? POST_GEOMETRY_COOLDOWN_MS : POST_REPLAY_COOLDOWN_MS);
            }
        } finally {
            worker.set(false);
            if (pending.get() != null) startWorkerIfNeeded();
            else if (prefs.getBoolean("ReplacementActive", false)) {
                prefs.edit().putString("IceCamState", "REPLACEMENT_ACTIVE").apply();
            }
        }
    }

    private boolean legacyApplyMediaOnce(ApplyRequest req, boolean retry) {
        synchronized (backendLock) {
            try {
                prefs.edit().putString("IceCamState",
                        req.softGeometry ? "APPLYING_GEOMETRY" : "APPLYING_MEDIA").apply();
                File f = new File(req.path);
                long t0 = android.os.SystemClock.elapsedRealtime();
                log.log("applyq", "legacy apply " + (retry ? "retry" : "start") + " #" + req.sequence
                        + " source=" + req.source + " soft=" + req.softGeometry
                        + " exists=" + f.exists() + " size=" + (f.exists() ? f.length() : -1L)
                        + " path=" + req.path);
                binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
                if (!req.softGeometry) {
                    Shell.su("setenforce 0 2>/dev/null || true\nservice check "
                            + RootBootstrap.FIXED_SERVICE_NAME + " 2>&1 || true\n");
                }
                if (!binder.connected()) {
                    binder.clearCache();
                    sleepMs(req.softGeometry ? 80 : 250);
                }
                long tx14Sleep = req.softGeometry ? 70L : 520L;
                long tx11Sleep = req.softGeometry ? 50L : 150L;

                long tx14Start = android.os.SystemClock.elapsedRealtime();
                int mode = binder.setMode(1, req.path);
                long tx14Ms = android.os.SystemClock.elapsedRealtime() - tx14Start;
                sleepMs(tx14Sleep);
                StreamGeometry geom = StreamGeometry.load(prefs);
                long tx11Start = android.os.SystemClock.elapsedRealtime();
                boolean loop = prefs.getBoolean("PlayisLoop", true);
                int play = binder.playSource(req.path, loop);
                long tx11Ms = android.os.SystemClock.elapsedRealtime() - tx11Start;
                sleepMs(tx11Sleep);
                binder.setAngle(geom.angleDegrees());
                binder.setMirror(geom.mirrorH);
                binder.setLoop(loop);
                binder.setAutoRotate(geom.autoRotate);
                boolean active = verifyLive(mode, play);
                prefs.edit()
                        .putBoolean("ReplacementActive", active)
                        .putString("IceCamState", active ? "REPLACEMENT_ACTIVE" : "PLAY_ERROR")
                        .apply();
                log.log("applyq", "legacy apply done #" + req.sequence + " TX14=" + mode + "(" + tx14Ms + "ms)"
                        + " TX11=" + play + "(" + tx11Ms + "ms) total="
                        + (android.os.SystemClock.elapsedRealtime() - t0) + "ms active=" + active);
                if (!active && !req.softGeometry) binder.clearCache();
                return active;
            } catch (Throwable t) {
                if (!req.softGeometry) {
                    prefs.edit().putBoolean("ReplacementActive", false).putString("IceCamState", "PLAY_ERROR").apply();
                    binder.clearCache();
                } else {
                    prefs.edit().putString("IceCamState", probeNativeLive() ? "GEOMETRY_SKIPPED" : "PLAY_ERROR").apply();
                }
                log.log("applyq", "legacy apply exception #" + req.sequence + ": " + t);
                return false;
            }
        }
    }

    private boolean verifyLive(int tx14, int tx11) {
        if (tx14 < 0 || tx11 < 0) return false;
        return probeNativeLive() || (tx14 >= 0 && tx11 >= 0);
    }

    private boolean probeNativeLive() {
        if (!binder.connected()) return false;
        int[] poll = binder.pollState();
        if (poll.length > 0 && poll[0] == 1) return true;
        int st = binder.getStatus();
        return st == StreamStatus.TX15_PLAYING || st == StreamStatus.TX15_OK_SOURCE;
    }

    private void syncReplacementActive(ApplyRequest req) {
        if (!binder.connected()) return;
        boolean live = probeNativeLive();
        if (live != prefs.getBoolean("ReplacementActive", false)) {
            prefs.edit()
                    .putBoolean("ReplacementActive", live)
                    .putString("IceCamState", live ? "REPLACEMENT_ACTIVE" : "PLAY_ERROR")
                    .apply();
            log.log("applyq", "sync ReplacementActive=" + live + " after #" + req.sequence);
        }
    }

    private static void sleepMs(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
