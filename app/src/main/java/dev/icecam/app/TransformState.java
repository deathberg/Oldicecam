package dev.icecam.app;

import android.content.SharedPreferences;
import java.util.Locale;

/**
 * v9 clean-room transform model for recovered Binder TX24(int,float,float,float,float,int).
 *
 * Wire format:
 *   TX24(mode, panX, panY, zoomX, zoomY, flags)
 *
 * Important: report.html also uses the term TX24 as RGB24/YUV conversion. That is unrelated.
 * Here TX24 means Binder transaction code 24.
 */
public final class TransformState {
    public static final int MODE_FREE = 0;
    public static final int MODE_FIT = 1;
    public static final int MODE_FILL = 2;
    public static final int MODE_STRETCH = 3;
    public static final int MODE_NATIVE = 4;

    public static final int FLAG_MIRROR_H = 1;
    public static final int FLAG_MIRROR_V = 1 << 1;
    public static final int FLAG_ROT_SHIFT = 2;
    public static final int FLAG_ROT_MASK = 0x3 << FLAG_ROT_SHIFT;
    public static final int FLAG_AUTO_ROTATE = 1 << 5;
    public static final int FLAG_LOCK_ASPECT = 1 << 6;
    public static final int FLAG_CROP_VALID = 1 << 7;
    public static final int FLAG_CROP_SHIFT = 8;
    public static final int FLAG_CROP_MASK = 0xff << FLAG_CROP_SHIFT;

    public static final float ZOOM_MIN = 0.05f;
    public static final float ZOOM_MAX = 32f;
    public static final float PAN_MIN = -8f;
    public static final float PAN_MAX = 8f;

    public int mode = MODE_FIT;
    public float panX = 0f;
    public float panY = 0f;
    public float zoomX = 1f;
    public float zoomY = 1f;
    public int flags = FLAG_LOCK_ASPECT;

    public static TransformState load(SharedPreferences p) {
        TransformState s = new TransformState();
        s.mode = clampInt(p.getInt("TransformMode", MODE_FIT), MODE_FREE, MODE_NATIVE);
        s.panX = clampFloat(p.getFloat("PanX", p.getInt("AutoColor_X", 0) / 100f), PAN_MIN, PAN_MAX);
        s.panY = clampFloat(p.getFloat("PanY", p.getInt("AutoColor_Y", 0) / 100f), PAN_MIN, PAN_MAX);
        float z = clampFloat(p.getFloat("ZoomX", p.getInt("Scale", 100) / 100f), ZOOM_MIN, ZOOM_MAX);
        s.zoomX = z;
        s.zoomY = p.getBoolean("LockAspect", true) ? z : clampFloat(p.getFloat("ZoomY", p.getInt("ScaleY", Math.round(z * 100)) / 100f), ZOOM_MIN, ZOOM_MAX);
        int angle = ((p.getInt("PlayAngle", 0) / 90) % 4 + 4) % 4;
        int f = (angle << FLAG_ROT_SHIFT);
        if (p.getBoolean("PlayMirror", false)) f |= FLAG_MIRROR_H;
        if (p.getBoolean("PlayMirrorV", false)) f |= FLAG_MIRROR_V;
        if (p.getBoolean("PlayAutoRotate", false)) f |= FLAG_AUTO_ROTATE;
        if (p.getBoolean("LockAspect", true)) f |= FLAG_LOCK_ASPECT;
        int crop = clampInt(p.getInt("CropPreset", 0), 0, 255);
        if (crop != 0) f |= FLAG_CROP_VALID | (crop << FLAG_CROP_SHIFT);
        s.flags = f;
        return s;
    }

    public void save(SharedPreferences p) {
        p.edit()
                .putInt("TransformMode", mode)
                .putFloat("PanX", panX)
                .putFloat("PanY", panY)
                .putFloat("ZoomX", zoomX)
                .putFloat("ZoomY", zoomY)
                .putInt("AutoColor_X", Math.round(panX * 100))
                .putInt("AutoColor_Y", Math.round(panY * 100))
                .putInt("Scale", Math.round(zoomX * 100))
                .putInt("ScaleY", Math.round(zoomY * 100))
                .putInt("PlayAngle", rotationQuadrant() * 90)
                .putBoolean("PlayMirror", mirrorH())
                .putBoolean("PlayMirrorV", mirrorV())
                .putBoolean("PlayAutoRotate", autoRotate())
                .putBoolean("LockAspect", lockAspect())
                .putInt("CropPreset", cropPreset())
                .apply();
    }

    public int rotationQuadrant() { return (flags & FLAG_ROT_MASK) >>> FLAG_ROT_SHIFT; }
    public int cropPreset() { return (flags & FLAG_CROP_VALID) == 0 ? 0 : ((flags & FLAG_CROP_MASK) >>> FLAG_CROP_SHIFT); }
    public boolean mirrorH() { return (flags & FLAG_MIRROR_H) != 0; }
    public boolean mirrorV() { return (flags & FLAG_MIRROR_V) != 0; }
    public boolean autoRotate() { return (flags & FLAG_AUTO_ROTATE) != 0; }
    public boolean lockAspect() { return (flags & FLAG_LOCK_ASPECT) != 0; }

    public void zoom(float factor) {
        zoomX = clampFloat(zoomX * factor, ZOOM_MIN, ZOOM_MAX);
        zoomY = lockAspect() ? zoomX : clampFloat(zoomY * factor, ZOOM_MIN, ZOOM_MAX);
    }

    public void move(float dx, float dy) {
        panX = clampFloat(panX + dx, PAN_MIN, PAN_MAX);
        panY = clampFloat(panY + dy, PAN_MIN, PAN_MAX);
    }

    public void center() { panX = 0f; panY = 0f; }
    public void reset() { mode = MODE_FIT; panX = 0f; panY = 0f; zoomX = 1f; zoomY = 1f; flags = FLAG_LOCK_ASPECT; }
    public void rotate90() { int q = (rotationQuadrant() + 1) & 3; flags = (flags & ~FLAG_ROT_MASK) | (q << FLAG_ROT_SHIFT); }
    public void rotateMinus90() { int q = (rotationQuadrant() + 3) & 3; flags = (flags & ~FLAG_ROT_MASK) | (q << FLAG_ROT_SHIFT); }
    public void toggleMirrorH() { flags ^= FLAG_MIRROR_H; }
    public void toggleMirrorV() { flags ^= FLAG_MIRROR_V; }
    public void toggleFitFill() { mode = (mode == MODE_FIT) ? MODE_FILL : MODE_FIT; }
    public void cycleCrop() { int c = (cropPreset() + 1) % 5; setCropPreset(c); }
    public void setCropPreset(int c) { c = clampInt(c, 0, 255); flags &= ~(FLAG_CROP_VALID | FLAG_CROP_MASK); if (c != 0) flags |= FLAG_CROP_VALID | (c << FLAG_CROP_SHIFT); }
    public void toggleAutoRotate() { flags ^= FLAG_AUTO_ROTATE; }
    public void toggleLockAspect() { flags ^= FLAG_LOCK_ASPECT; if (lockAspect()) zoomY = zoomX; }

    public String modeName() {
        switch (mode) {
            case MODE_FIT: return "FIT";
            case MODE_FILL: return "FILL";
            case MODE_STRETCH: return "STRETCH";
            case MODE_NATIVE: return "NATIVE";
            default: return "FREE";
        }
    }

    public String cropName() {
        switch (cropPreset()) {
            case 1: return "CENTER";
            case 2: return "16:9";
            case 3: return "4:3";
            case 4: return "SQUARE";
            default: return "OFF";
        }
    }

    public String summary() {
        return String.format(Locale.US,
                "%s crop=%s pan=(%.2f,%.2f) zoom=(%.2f,%.2f) rot=%d mirH=%s mirV=%s flags=0x%08X",
                modeName(), cropName(), panX, panY, zoomX, zoomY, rotationQuadrant() * 90, mirrorH(), mirrorV(), flags);
    }

    public static float clampFloat(float v, float mn, float mx) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return mn;
        return Math.max(mn, Math.min(mx, v));
    }

    private static int clampInt(int v, int mn, int mx) { return Math.max(mn, Math.min(mx, v)); }
}
