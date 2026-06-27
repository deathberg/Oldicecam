package dev.icecam.app.runtime;

import java.util.concurrent.atomic.AtomicLong;

public final class RuntimeCommand {
    private static final AtomicLong IDS = new AtomicLong(1L);
    public enum Type { MUTATE_TRANSFORM, COMMIT, START_REPLACEMENT, RESTORE_CAMERA, SELECT_MEDIA, SET_LOOP, OP_FINISHED }
    public final long id;
    public final RuntimeTypes.Source source;
    public final Type type;
    public final String op;
    public final String path;
    public final int slot;
    public final boolean boolValue;
    public final String opId;
    public final long enqueueRealtimeMs;
    private RuntimeCommand(Type type, RuntimeTypes.Source source, String op, String path, int slot, boolean boolValue, String opId) {
        this.id = IDS.getAndIncrement();
        this.type = type;
        this.source = source == null ? RuntimeTypes.Source.SYSTEM : source;
        this.op = op == null ? "" : op;
        this.path = path == null ? "" : path;
        this.slot = slot;
        this.boolValue = boolValue;
        this.opId = opId == null ? "" : opId;
        this.enqueueRealtimeMs = android.os.SystemClock.elapsedRealtime();
    }
    public static RuntimeCommand mutate(RuntimeTypes.Source src, String op) { return new RuntimeCommand(Type.MUTATE_TRANSFORM, src, op, "", 0, false, ""); }
    public static RuntimeCommand commit(RuntimeTypes.Source src, String reason) { return new RuntimeCommand(Type.COMMIT, src, reason, "", 0, false, ""); }
    public static RuntimeCommand start(RuntimeTypes.Source src) { return new RuntimeCommand(Type.START_REPLACEMENT, src, "start", "", 0, false, ""); }
    public static RuntimeCommand restore(RuntimeTypes.Source src) { return new RuntimeCommand(Type.RESTORE_CAMERA, src, "restore", "", 0, false, ""); }
    public static RuntimeCommand select(RuntimeTypes.Source src, int slot, String path) { return new RuntimeCommand(Type.SELECT_MEDIA, src, "select", path, slot, false, ""); }
    public static RuntimeCommand loop(RuntimeTypes.Source src, boolean loop) { return new RuntimeCommand(Type.SET_LOOP, src, "loop", "", 0, loop, ""); }
    public static RuntimeCommand opFinished(String opId, boolean ok) { return new RuntimeCommand(Type.OP_FINISHED, RuntimeTypes.Source.SYSTEM, ok ? "ok" : "failed", "", 0, ok, opId); }
    public String label() { return "#" + id + " " + source + " " + type + (op.length() == 0 ? "" : " " + op); }
}
