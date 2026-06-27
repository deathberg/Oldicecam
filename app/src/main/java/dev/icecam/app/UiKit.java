package dev.icecam.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;

public final class UiKit {
    private UiKit() {}

    public static final int BG = 0xff070a10;
    public static final int PANEL = 0xff151b28;
    public static final int PANEL_2 = 0xff202838;
    public static final int PANEL_3 = 0xff30384a;
    public static final int CYAN = 0xff17c7dd;
    public static final int CYAN_DARK = 0xff0b8fa8;
    public static final int GREEN = 0xff24d487;
    public static final int RED = 0xffff6078;
    public static final int TEXT = 0xffeef4ff;
    public static final int MUTED = 0xffa9b5c8;
    public static final int WARN = 0xffffcc66;

    public static StateListDrawable neonButton(int base, int active, int radiusPx) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, glow(active, 0x99ffffff, radiusPx));
        s.addState(new int[]{android.R.attr.state_selected}, glow(active, 0x99ffffff, radiusPx));
        s.addState(new int[]{-android.R.attr.state_enabled}, fill(0xff303643, radiusPx, 0x33606a7d));
        s.addState(new int[]{}, fill(base, radiusPx, 0x665f6d85));
        return s;
    }

    public static GradientDrawable fill(int color, int radiusPx, int stroke) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{lighten(color, 24), color});
        g.setCornerRadius(radiusPx);
        g.setStroke(Math.max(1, radiusPx / 18), stroke);
        return g;
    }

    public static LayerDrawable glow(int color, int stroke, int radiusPx) {
        GradientDrawable outer = new GradientDrawable();
        outer.setColor(0x4417c7dd);
        outer.setCornerRadius(radiusPx + 6);
        outer.setStroke(2, 0xaa17e8ff);
        GradientDrawable inner = fill(lighten(color, 48), radiusPx, stroke);
        LayerDrawable l = new LayerDrawable(new android.graphics.drawable.Drawable[]{outer, inner});
        int inset = Math.max(2, radiusPx / 8);
        l.setLayerInset(1, inset, inset, inset, inset);
        return l;
    }

    private static int lighten(int c, int delta) {
        int a = Color.alpha(c);
        int r = Math.min(255, Color.red(c) + delta);
        int g = Math.min(255, Color.green(c) + delta);
        int b = Math.min(255, Color.blue(c) + delta);
        return Color.argb(a, r, g, b);
    }
}
