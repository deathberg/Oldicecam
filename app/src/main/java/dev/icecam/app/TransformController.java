package dev.icecam.app;

import android.content.Context;

/** @deprecated Use {@link StreamController} — kept for minimal compat. */
public final class TransformController {
    public enum Source { MAIN, FLOAT }
    private static volatile TransformController instance;
    private final StreamController stream;

    public static TransformController get(Context context) {
        if (instance == null) {
            synchronized (TransformController.class) {
                if (instance == null) instance = new TransformController(context.getApplicationContext());
            }
        }
        return instance;
    }

    private TransformController(Context ctx) { stream = StreamController.get(ctx); }

    public StreamController stream() { return stream; }
    public boolean isBusy() { return stream.isBusy(); }
    public boolean isRendering() { return false; }

    public TransformState mutate(Source s, String op, boolean ignored) {
        return stream.mutate(map(s), op).toPreviewTransform();
    }

    public void commit(Source s, String reason) { stream.commit(map(s)); }
    public void startReplacement(Source s) { stream.startReplacement(map(s)); }
    public void restoreCamera(Source s) { stream.restoreCamera(map(s)); }
    public void selectMedia(Source s, int slot, String path) { stream.selectSlot(map(s), slot, path); }
    public void setLoop(Source s, boolean loop) { stream.setLoop(map(s), loop); }

    private static StreamController.Source map(Source s) {
        return s == Source.FLOAT ? StreamController.Source.FLOAT : StreamController.Source.MAIN;
    }
}
