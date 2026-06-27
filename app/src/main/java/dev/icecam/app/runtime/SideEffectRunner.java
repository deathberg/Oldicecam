package dev.icecam.app.runtime;

import android.content.Context;
import dev.icecam.app.AppLogger;
import dev.icecam.app.BackendApplyQueue;
import dev.icecam.app.RootBootstrap;
import dev.icecam.app.TransformState;
import dev.icecam.app.VliveBinderClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SideEffectRunner {
    private final Context context;
    private final AppLogger log;
    private final RootBootstrap root;
    private final VliveBinderClient binder;
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> new Thread(r, "icecam-runtime-effects"));

    public SideEffectRunner(Context context, AppLogger log) {
        this.context = context.getApplicationContext();
        this.log = log;
        this.root = new RootBootstrap(this.context, log);
        this.binder = new VliveBinderClient(log);
        this.binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
    }

    public void run(RuntimeCommand c, AppState state, CommandBus bus) {
        switch (c.type) {
            case MUTATE_TRANSFORM:
                // Realtime preview only. No JPEG bake, no backend replay.
                sendTransformBestEffort(state.transform);
                break;
            case COMMIT:
                io.execute(() -> {
                    String opId = "commit-" + c.id;
                    boolean ok = false;
                    try {
                        ok = sendTransformBestEffort(state.transform);
                        String path = state.media.originalPath.length() > 0 ? state.media.originalPath : state.media.playPath;
                        if (path.length() > 0) BackendApplyQueue.get(context).enqueue(path, "runtime-commit-" + c.source.name().toLowerCase(), true);
                    } catch (Throwable t) { if (log != null) log.log("runtime", "commit side effect failed #" + c.id + ": " + t); }
                    bus.dispatch(RuntimeCommand.opFinished(opId, ok));
                });
                break;
            case START_REPLACEMENT:
                io.execute(() -> {
                    String opId = "start-" + c.id;
                    boolean ok = false;
                    try {
                        binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
                        if (!binder.connected()) { root.bootstrap(); binder.clearCache(); sleep(350); }
                        ok = sendTransformBestEffort(state.transform);
                        String path = state.media.originalPath.length() > 0 ? state.media.originalPath : state.media.playPath;
                        if (path.length() > 0) BackendApplyQueue.get(context).enqueue(path, "runtime-start-" + c.source.name().toLowerCase(), true);
                    } catch (Throwable t) { if (log != null) log.log("runtime", "start side effect failed #" + c.id + ": " + t); }
                    bus.dispatch(RuntimeCommand.opFinished(opId, ok));
                });
                break;
            case RESTORE_CAMERA:
                io.execute(() -> {
                    String opId = "restore-" + c.id;
                    boolean ok = false;
                    try { root.restoreCamera(); binder.clearCache(); ok = true; }
                    catch (Throwable t) { if (log != null) log.log("runtime", "restore side effect failed #" + c.id + ": " + t); }
                    bus.dispatch(RuntimeCommand.opFinished(opId, ok));
                });
                break;
            default: break;
        }
    }

    private boolean sendTransformBestEffort(TransformState s) {
        try {
            binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
            int r = binder.setTransform(s);
            if (log != null) log.log("runtime", "TX24 transform result=" + r + " " + s.summary());
            return r >= 0;
        } catch (Throwable t) { if (log != null) log.log("runtime", "TX24 transform skipped: " + t); return false; }
    }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
