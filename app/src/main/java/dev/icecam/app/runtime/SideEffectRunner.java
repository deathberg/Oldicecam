package dev.icecam.app.runtime;

import android.content.Context;
import dev.icecam.app.AppLogger;

/** Side effects disabled — {@link dev.icecam.app.StreamController} owns Binder I/O. */
public final class SideEffectRunner {
    public SideEffectRunner(Context context, AppLogger log) {}
    public void run(RuntimeCommand c, AppState state, CommandBus bus) {
        if (c.type == RuntimeCommand.Type.OP_FINISHED) {
            bus.dispatch(RuntimeCommand.opFinished(c.opId, c.boolValue));
        }
    }
}
