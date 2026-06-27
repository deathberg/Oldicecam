package dev.icecam.app;

import android.content.Context;
import dev.icecam.app.runtime.AppState;
import dev.icecam.app.runtime.CommandBus;
import dev.icecam.app.runtime.RuntimeCommand;
import dev.icecam.app.runtime.RuntimeTypes;

/**
 * v25 compatibility facade over Runtime Architecture.
 * MainActivity and FloatService keep calling this class, but state changes are routed only via CommandBus -> Reducer -> AppState.
 * This class no longer bakes images and no longer owns pending render/apply state.
 */
public final class TransformController {
    public enum Source { MAIN, FLOAT }
    private static volatile TransformController instance;

    public static TransformController get(Context context) {
        Context app = context.getApplicationContext();
        TransformController local = instance;
        if (local == null) {
            synchronized (TransformController.class) {
                local = instance;
                if (local == null) instance = local = new TransformController(app);
            }
        }
        return local;
    }

    private final android.content.Context context;
    private final CommandBus bus;
    private TransformController(Context context) { this.context = context.getApplicationContext(); bus = CommandBus.get(this.context); }

    public CommandBus bus() { return bus; }
    public boolean isRendering() { return false; }
    public boolean isBusy() { return bus.state().backend.operations.busy() || BackendApplyQueue.get(context).isRunning(); }

    public TransformState mutate(Source source, String op, boolean autoCommitIgnored) {
        bus.dispatch(RuntimeCommand.mutate(map(source), op));
        return bus.state().transform;
    }

    public void updateState(Source source, String reason, TransformState state, boolean autoCommitIgnored) {
        // Direct state mutation is intentionally disabled in v25. Use commands only.
        bus.dispatch(RuntimeCommand.mutate(map(source), reason));
    }

    public void commit(Source source, String reason) { bus.dispatch(RuntimeCommand.commit(map(source), reason)); }
    public void startReplacement(Source source) { bus.dispatch(RuntimeCommand.start(map(source))); }
    public void restoreCamera(Source source) { bus.dispatch(RuntimeCommand.restore(map(source))); }
    public void selectMedia(Source source, int slot, String path) { bus.dispatch(RuntimeCommand.select(map(source), slot, path)); }
    public void setLoop(Source source, boolean loop) { bus.dispatch(RuntimeCommand.loop(map(source), loop)); }
    public AppState state() { return bus.state(); }

    private static RuntimeTypes.Source map(Source s) { return s == Source.FLOAT ? RuntimeTypes.Source.FLOAT : RuntimeTypes.Source.MAIN; }
}
