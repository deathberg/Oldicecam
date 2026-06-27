package dev.icecam.retool;

import android.graphics.drawable.GradientDrawable;

public final class UiKit {
    private UiKit() {}

    public static final int BG = 0xff070a10;
    public static final int PANEL = 0xff151b28;
    public static final int CYAN = 0xff17c7dd;
    public static final int CYAN_DARK = 0xff0b8fa8;
    public static final int GREEN = 0xff24d487;
    public static final int TEXT = 0xffeef4ff;
    public static final int MUTED = 0xffa9b5c8;

    public static GradientDrawable fill(int color, int radiusPx, int stroke) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{lighten(color, 24), color});
        g.setCornerRadius(radiusPx);
        g.setStroke(Math.max(1, radiusPx / 18), stroke);
        return g;
    }

    private static int lighten(int color, int delta) {
        int a = (color >> 24) & 0xff;
        int r = Math.min(255, ((color >> 16) & 0xff) + delta);
        int g = Math.min(255, ((color >> 8) & 0xff) + delta);
        int b = Math.min(255, (color & 0xff) + delta);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
