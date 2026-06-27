package dev.icecam.app.runtime;

public final class MediaState {
    public final int activeSlot;
    public final String originalPath;
    public final String playPath;
    public final boolean loop;
    public MediaState(int activeSlot, String originalPath, String playPath, boolean loop) {
        this.activeSlot = Math.max(1, Math.min(4, activeSlot));
        this.originalPath = originalPath == null ? "" : originalPath;
        this.playPath = playPath == null ? "" : playPath;
        this.loop = loop;
    }
    public MediaState withSlot(int slot, String path) { return new MediaState(slot, path, path, loop); }
    public MediaState withLoop(boolean loop) { return new MediaState(activeSlot, originalPath, playPath, loop); }
}
