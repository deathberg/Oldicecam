package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import dev.icecam.app.runtime.AppState;

/** 2×2 live preview grid for media slots M1–M4. */
public final class SlotPreviewGrid extends LinearLayout {
    public interface SlotListener { void onSlotClick(int slot); void onSlotLongPress(int slot); }

    private final GpuMediaPreviewView[] tiles = new GpuMediaPreviewView[4];
    private SlotListener listener;

    public SlotPreviewGrid(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        LinearLayout row1 = row(ctx);
        LinearLayout row2 = row(ctx);
        for (int i = 0; i < 4; i++) {
            final int slot = i + 1;
            GpuMediaPreviewView tile = new GpuMediaPreviewView(ctx);
            tile.setSlotLabel("M" + slot);
            tile.setOnClickListener(v -> { if (listener != null) listener.onSlotClick(slot); });
            tile.setOnLongClickListener(v -> { if (listener != null) listener.onSlotLongPress(slot); return true; });
            tiles[i] = tile;
            (i < 2 ? row1 : row2).addView(tile, cellLp());
        }
        addView(row1, fullLp());
        addView(row2, fullLp());
    }

    public void setListener(SlotListener l) { listener = l; }

    public void bind(SharedPreferences prefs, AppState state) {
        int active = Math.max(1, Math.min(4, state.media.activeSlot));
        TransformState tx = state.transform;
        for (int i = 0; i < 4; i++) {
            int slot = i + 1;
            String path = prefs.getString("Slot" + slot + "Path", "");
            GpuMediaPreviewView tile = tiles[i];
            tile.setMediaPath(path);
            tile.setHighlighted(slot == active);
            tile.setTransformState(slot == active ? tx : new TransformState());
        }
    }

    public void releaseAll() {
        for (GpuMediaPreviewView t : tiles) if (t != null) t.release();
    }

    private static LinearLayout row(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(HORIZONTAL);
        return l;
    }

    private static LayoutParams cellLp() {
        LayoutParams p = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
        p.setMargins(3, 3, 3, 3);
        return p;
    }

    private static LayoutParams fullLp() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
    }
}
