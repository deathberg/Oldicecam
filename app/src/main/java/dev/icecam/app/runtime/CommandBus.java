package dev.icecam.app.runtime;

import android.content.Context;
import dev.icecam.app.AppLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CommandBus {
    private static volatile CommandBus instance;
    public static CommandBus get(Context context) {
        Context app = context.getApplicationContext();
        CommandBus local = instance;
        if (local == null) synchronized (CommandBus.class) { local = instance; if (local == null) instance = local = new CommandBus(app); }
        return local;
    }

    private final ExecutorService reducerThread = Executors.newSingleThreadExecutor(r -> new Thread(r, "icecam-runtime-reducer"));
    private final StateStore store;
    private final Persistence persistence;
    private final FlightRecorder recorder;
    private final SideEffectRunner effects;
    private final AppLogger log;

    private CommandBus(Context context) {
        log = new AppLogger(context);
        persistence = new Persistence(context);
        store = new StateStore(persistence.load());
        recorder = new FlightRecorder(context, log);
        effects = new SideEffectRunner(context, log);
        log.log("runtime", "CommandBus initialized");
    }
    public StateStore store() { return store; }
    public AppState state() { return store.get(); }
    public FlightRecorder recorder() { return recorder; }

    public void dispatch(RuntimeCommand c) {
        AppState enqState = store.get();
        recorder.enqueue(c, enqState);
        reducerThread.execute(() -> {
            long start = android.os.SystemClock.elapsedRealtime();
            AppState before = store.get();
            recorder.start(c, before);
            AppState after;
            RuntimeTypes.Result result = RuntimeTypes.Result.OK;
            try {
                after = TransformReducer.reduce(before, c);
                persistence.save(after);
                store.set(after);
                effects.run(c, after, this);
            } catch (Throwable t) {
                after = before.withDiagnostics(before.diagnostics.command(c.id, c.type.name(), "FAILED: " + t));
                result = RuntimeTypes.Result.FAILED;
                if (log != null) log.log("runtime", "command failed " + c.label() + ": " + t);
            }
            long finish = android.os.SystemClock.elapsedRealtime();
            recorder.finish(c, before, after, result);
            recorder.performance(c, start, finish);
            if (log != null) log.log("runtime", "reduced " + c.label() + " result=" + result + " marker=#" + c.id);
        });
    }
}
