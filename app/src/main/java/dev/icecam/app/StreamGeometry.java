package dev.icecam.app;

import android.content.SharedPreferences;
import java.util.Locale;

/**
 * Preview + bake geometry. Applied to images via MediaTransformer bake;
 * video uses TX18/TX19 on native side. Never sent as TX24.
 */
public final class StreamGeometry {
    public static final int MODE_FIT = 1;
    public static final int MODE_FILL = 2;

    public int mode = MODE_FIT;
    public float panX = 0f;
    public float panY = 0f;
    public float zoomX = 1f;
    public float zoomY = 1f;
    public int angleQuadrant = 0;
    public boolean mirrorH = false;
    public boolean mirrorV = false;
    public boolean autoRotate = false;
    public boolean lockAspect = true;

    public static StreamGeometry load(SharedPreferences p) {
        StreamGeometry s = new StreamGeometry();
        s.mode = p.getInt("TransformMode", MODE_FIT);
        s.panX = p.getFloat("PanX", 0f);
        s.panY = p.getFloat("PanY", 0f);
        s.zoomX = p.getFloat("ZoomX", 1f);
        s.zoomY = p.getFloat("ZoomY", p.getFloat("ZoomX", 1f));
        s.angleQuadrant = ((p.getInt("PlayAngle", 0) / 90) % 4 + 4) % 4;
        s.mirrorH = p.getBoolean("PlayMirror", false);
        s.mirrorV = p.getBoolean("PlayMirrorV", false);
        s.autoRotate = p.getBoolean("PlayAutoRotate", false);
        s.lockAspect = p.getBoolean("LockAspect", true);
        return s;
    }

    public static StreamGeometry copy(StreamGeometry x) {
        StreamGeometry s = new StreamGeometry();
        s.mode = x.mode; s.panX = x.panX; s.panY = x.panY;
        s.zoomX = x.zoomX; s.zoomY = x.zoomY; s.angleQuadrant = x.angleQuadrant;
        s.mirrorH = x.mirrorH; s.mirrorV = x.mirrorV;
        s.autoRotate = x.autoRotate; s.lockAspect = x.lockAspect;
        return s;
    }

    public void save(SharedPreferences p) {
        p.edit()
                .putInt("TransformMode", mode)
                .putFloat("PanX", panX)
                .putFloat("PanY", panY)
                .putFloat("ZoomX", zoomX)
                .putFloat("ZoomY", zoomY)
                .putInt("PlayAngle", angleDegrees())
                .putBoolean("PlayMirror", mirrorH)
                .putBoolean("PlayMirrorV", mirrorV)
                .putBoolean("PlayAutoRotate", autoRotate)
                .putBoolean("LockAspect", lockAspect)
                .apply();
    }

    public static void resetGeometry(SharedPreferences p) {
        p.edit()
                .putFloat("PanX", 0f).putFloat("PanY", 0f)
                .putFloat("ZoomX", 1f).putFloat("ZoomY", 1f)
                .putInt("PlayAngle", 0)
                .putBoolean("PlayMirror", false)
                .putBoolean("PlayMirrorV", false)
                .apply();
    }

    public int angleDegrees() { return angleQuadrant * 90; }

    public void applyOp(String op) {
        switch (op) {
            case "zoom+": zoom(1.12f); break;
            case "zoom-": zoom(1f / 1.12f); break;
            case "up": panY = clamp(panY + 0.04f); break;
            case "down": panY = clamp(panY - 0.04f); break;
            case "left": panX = clamp(panX - 0.04f); break;
            case "right": panX = clamp(panX + 0.04f); break;
            case "center": panX = 0f; panY = 0f; break;
            case "fit-fill": mode = (mode == MODE_FIT) ? MODE_FILL : MODE_FIT; break;
            case "rot+90": case "rotate": angleQuadrant = (angleQuadrant + 1) & 3; break;
            case "rot-90": angleQuadrant = (angleQuadrant + 3) & 3; break;
            case "mirror-x": case "mirror": mirrorH = !mirrorH; break;
            case "mirror-y": mirrorV = !mirrorV; break;
            case "reset": reset(); break;
        }
    }

    public void zoom(float f) {
        zoomX = clampZoom(zoomX * f);
        zoomY = lockAspect ? zoomX : clampZoom(zoomY * f);
    }

    public void reset() {
        mode = MODE_FIT; panX = 0f; panY = 0f; zoomX = 1f; zoomY = 1f;
        angleQuadrant = 0; mirrorH = false; mirrorV = false;
    }

    /** For GpuMediaPreviewView / MediaTransformer compatibility. */
    public TransformState toPreviewTransform() {
        TransformState t = new TransformState();
        t.mode = mode;
        t.panX = panX; t.panY = panY; t.zoomX = zoomX; t.zoomY = zoomY;
        t.flags = TransformState.FLAG_LOCK_ASPECT;
        if (mirrorH) t.flags |= TransformState.FLAG_MIRROR_H;
        if (mirrorV) t.flags |= TransformState.FLAG_MIRROR_V;
        t.flags |= (angleQuadrant << TransformState.FLAG_ROT_SHIFT);
        if (autoRotate) t.flags |= TransformState.FLAG_AUTO_ROTATE;
        return t;
    }

    public String modeName() { return mode == MODE_FILL ? "FILL" : "FIT"; }

    public String summary() {
        return String.format(Locale.US, "%s pan=(%.2f,%.2f) z=%.2f rot=%d° mir=%s%s",
                modeName(), panX, panY, zoomX, angleDegrees(),
                mirrorH ? "X" : "-", mirrorV ? "Y" : "");
    }

    private static float clamp(float v) { return Math.max(-8f, Math.min(8f, v)); }
    private static float clampZoom(float v) { return Math.max(0.05f, Math.min(32f, v)); }
}
