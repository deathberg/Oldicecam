package dev.icecam.app;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Lightweight auto color correction for preview/bake paths.
 * Histogram stretch + gray-world white balance.
 */
public final class ColorCorrector {
    private ColorCorrector() {}

    public static Bitmap apply(Bitmap src, float strength) {
        if (src == null || strength <= 0f) return src;
        strength = Math.min(1f, strength);
        int w = src.getWidth();
        int h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);

        int minR = 255, minG = 255, minB = 255, maxR = 0, maxG = 0, maxB = 0;
        long sumR = 0, sumG = 0, sumB = 0;
        for (int c : px) {
            int r = Color.red(c), g = Color.green(c), b = Color.blue(c);
            minR = Math.min(minR, r); maxR = Math.max(maxR, r);
            minG = Math.min(minG, g); maxG = Math.max(maxG, g);
            minB = Math.min(minB, b); maxB = Math.max(maxB, b);
            sumR += r; sumG += g; sumB += b;
        }
        int n = px.length;
        float avgR = sumR / (float) n;
        float avgG = sumG / (float) n;
        float avgB = sumB / (float) n;
        float gray = (avgR + avgG + avgB) / 3f;
        float gainR = gray / Math.max(1f, avgR);
        float gainG = gray / Math.max(1f, avgG);
        float gainB = gray / Math.max(1f, avgB);

        float scaleR = (255f / Math.max(1, maxR - minR));
        float scaleG = (255f / Math.max(1, maxG - minG));
        float scaleB = (255f / Math.max(1, maxB - minB));

        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int a = Color.alpha(c);
            float r = Color.red(c);
            float g = Color.green(c);
            float b = Color.blue(c);

            r = (r - minR) * scaleR;
            g = (g - minG) * scaleG;
            b = (b - minB) * scaleB;

            r = lerp(r, r * gainR, strength);
            g = lerp(g, g * gainG, strength);
            b = lerp(b, b * gainB, strength);

            px[i] = Color.argb(a, clamp(r), clamp(g), clamp(b));
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(px, 0, w, 0, 0, w, h);
        return out;
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static int clamp(float v) { return Math.max(0, Math.min(255, Math.round(v))); }
}
