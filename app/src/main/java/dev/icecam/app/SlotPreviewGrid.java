package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;

/** Single-row compact slot previews (M1–M4). */
public final class SlotPreviewGrid extends LinearLayout {
    public interface SlotListener { void onSlotClick(int slot); void onSlotLongPress(int slot); }

    private final GpuMediaPreviewView[] tiles = new GpuMediaPreviewView[4];
    private SlotListener listener;

    public SlotPreviewGrid(Context ctx) {
        super(ctx);
        setOrientation(HORIZONTAL);
        for (int i = 0; i < 4; i++) {
            final int slot = i + 1;
            GpuMediaPreviewView tile = new GpuMediaPreviewView(ctx);
            tile.setSlotLabel("M" + slot);
            tile.setOnClickListener(v -> { if (listener != null) listener.onSlotClick(slot); });
            tile.setOnLongClickListener(v -> { if (listener != null) listener.onSlotLongPress(slot); return true; });
            tiles[i] = tile;
            LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
            lp.setMargins(2, 2, 2, 2);
            addView(tile, lp);
        }
    }

    public void setListener(SlotListener l) { listener = l; }

    public void bind(SharedPreferences prefs, StreamGeometry geometry, int activeSlot) {
        for (int i = 0; i < 4; i++) {
            int slot = i + 1;
            String path = prefs.getString("Slot" + slot + "Path", "");
            GpuMediaPreviewView tile = tiles[i];
            tile.setMediaPath(path);
            tile.setHighlighted(slot == activeSlot);
            tile.setTransformState(slot == activeSlot ? geometry.toPreviewTransform() : new TransformState());
        }
    }

    public void releaseAll() {
        for (GpuMediaPreviewView t : tiles) if (t != null) t.release();
    }
}
