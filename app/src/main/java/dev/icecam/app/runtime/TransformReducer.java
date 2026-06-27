package dev.icecam.app.runtime;

import dev.icecam.app.TransformState;

public final class TransformReducer {
    private TransformReducer() {}
    public static AppState reduce(AppState state, RuntimeCommand c) {
        AppState next = state;
        switch (c.type) {
            case MUTATE_TRANSFORM: {
                TransformState s = cloneTransform(state.transform);
                mutate(s, c.op);
                next = state.withTransform(s).withBackend(state.backend.withPhase(c.source == RuntimeTypes.Source.FLOAT ? "FLOAT_TRANSFORM_DIRTY" : "TRANSFORM_DIRTY"));
                break;
            }
            case COMMIT: {
                String opId = "commit-" + c.id;
                next = state.withBackend(state.backend.withPhase("COMMIT_QUEUED").withOperations(state.backend.operations.start(opId, c.label())));
                break;
            }
            case START_REPLACEMENT: {
                String opId = "start-" + c.id;
                next = state.withBackend(state.backend.withPhase("STARTING").withOperations(state.backend.operations.start(opId, c.label())));
                break;
            }
            case RESTORE_CAMERA: {
                String opId = "restore-" + c.id;
                next = state.withBackend(state.backend.withPhase("RESTORING_CAMERA").withOperations(state.backend.operations.start(opId, c.label())));
                break;
            }
            case SELECT_MEDIA: {
                TransformState fresh = new TransformState();
                next = state.withTransform(fresh).withMedia(state.media.withSlot(c.slot, c.path)).withBackend(state.backend.withPhase("MEDIA_SELECTED"));
                break;
            }
            case SET_LOOP: next = state.withMedia(state.media.withLoop(c.boolValue)); break;
            case OP_FINISHED: {
                RuntimeTypes.OperationState ops = state.backend.operations.finish(c.opId);
                next = state.withBackend(state.backend.withOperations(ops).withPhase(c.boolValue ? "REPLACEMENT_ACTIVE" : "PLAY_ERROR"));
                break;
            }
        }
        return next.withUi(state.ui.marker(c.id, c.source, c.op)).withDiagnostics(state.diagnostics.command(c.id, c.type.name(), "REDUCED"));
    }
    public static TransformState cloneTransform(TransformState x) {
        TransformState s = new TransformState();
        s.mode = x.mode; s.panX = x.panX; s.panY = x.panY; s.zoomX = x.zoomX; s.zoomY = x.zoomY; s.flags = x.flags;
        return s;
    }
    private static void mutate(TransformState s, String op) {
        switch (op) {
            case "zoom+": s.zoom(1.12f); break;
            case "zoom-": s.zoom(1f / 1.12f); break;
            case "up": s.move(0f, 0.04f); break;
            case "down": s.move(0f, -0.04f); break;
            case "left": s.move(-0.04f, 0f); break;
            case "right": s.move(0.04f, 0f); break;
            case "center": s.center(); break;
            case "fit-fill": s.toggleFitFill(); break;
            case "crop": s.cycleCrop(); break;
            case "rotate": case "rot+90": s.rotate90(); break;
            case "rot-90": s.rotateMinus90(); break;
            case "mirror": case "mirror-x": s.toggleMirrorH(); break;
            case "mirror-y": s.toggleMirrorV(); break;
            case "reset": s.reset(); break;
        }
    }
}
