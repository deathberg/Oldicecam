package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;

/** Single-row compact slot previews (M1–M4) with framed tiles and media labels. */
public final class SlotPreviewGrid extends LinearLayout {
    public interface SlotListener { void onSlotClick(int slot); void onSlotLongPress(int slot); }

    private final GpuMediaPreviewView[] tiles = new GpuMediaPreviewView[4];
    private SlotListener listener;

    public SlotPreviewGrid(Context ctx) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setPadding(0, dp(2), 0, dp(2));
        for (int i = 0; i < 4; i++) {
            final int slot = i + 1;
            GpuMediaPreviewView tile = new GpuMediaPreviewView(ctx);
            tile.setSlotLabel("M" + slot);
            tile.setFramed(true, false);
            tile.setOnClickListener(v -> { if (listener != null) listener.onSlotClick(slot); });
            tile.setOnLongClickListener(v -> { if (listener != null) listener.onSlotLongPress(slot); return true; });
            tiles[i] = tile;
            LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
            lp.setMargins(dp(3), dp(2), dp(3), dp(2));
            addView(tile, lp);
        }
    }

    public void setListener(SlotListener l) { listener = l; }

    public void bind(SharedPreferences prefs, StreamGeometry geometry, int activeSlot) {
        for (int i = 0; i < 4; i++) {
            int slot = i + 1;
            String path = prefs.getString("Slot" + slot + "Path", "");
            boolean active = slot == activeSlot;
            GpuMediaPreviewView tile = tiles[i];
            tile.setFramed(true, active);
            tile.setMediaPath(path);
            tile.setHighlighted(active);
            tile.setMediaLabel(path.isEmpty() ? "пусто" : shortName(path));
            tile.setTransformState(active ? geometry.toPreviewTransform() : new TransformState());
        }
    }

    public void releaseAll() {
        for (GpuMediaPreviewView t : tiles) if (t != null) t.release();
    }

    private static String shortName(String p) {
        if (p == null || p.isEmpty()) return "";
        int i = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        String n = i >= 0 ? p.substring(i + 1) : p;
        return n.length() > 18 ? n.substring(0, 15) + "…" : n;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
}
