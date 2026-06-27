package dev.icecam.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;
import java.util.Locale;

public final class RealtimePreviewView extends ImageView {
    private String mediaPath = "";
    private Bitmap source;
    private TransformState transform = new TransformState();

    public RealtimePreviewView(Context context) { super(context); init(); }
    public RealtimePreviewView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    private void init() { setBackgroundColor(Color.BLACK); setScaleType(ScaleType.MATRIX); }

    public void setMediaPath(String path) {
        path = path == null ? "" : path;
        if (path.equals(mediaPath)) return;
        mediaPath = path;
        if (source != null) { source.recycle(); source = null; }
        if (MediaTransformer.isImagePath(path)) source = decodeBounded(path, 1800);
        setImageBitmap(source);
        applyMatrix();
    }

    public void setTransformState(TransformState state) {
        transform = state == null ? new TransformState() : state;
        applyMatrix();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) { super.onSizeChanged(w, h, oldw, oldh); applyMatrix(); }

    private void applyMatrix() {
        if (source == null || getWidth() <= 0 || getHeight() <= 0) { setImageMatrix(new Matrix()); return; }
        float vw = getWidth();
        float vh = getHeight();
        float sw = source.getWidth();
        float sh = source.getHeight();
        float base;
        if (transform.mode == TransformState.MODE_FILL) base = Math.max(vw / sw, vh / sh);
        else if (transform.mode == TransformState.MODE_STRETCH) base = 1f;
        else base = Math.min(vw / sw, vh / sh);
        Matrix m = new Matrix();
        m.postTranslate(-sw / 2f, -sh / 2f);
        if (transform.mirrorH()) m.postScale(-1f, 1f);
        if (transform.mirrorV()) m.postScale(1f, -1f);
        m.postRotate(transform.rotationQuadrant() * 90f);
        if (transform.mode == TransformState.MODE_STRETCH) m.postScale((vw / sw) * transform.zoomX, (vh / sh) * transform.zoomY);
        else m.postScale(base * transform.zoomX, base * transform.zoomY);
        m.postTranslate(vw / 2f + transform.panX * (vw / 2f), vh / 2f - transform.panY * (vh / 2f));
        setImageMatrix(m);
    }

    private static Bitmap decodeBounded(String path, int maxDim) {
        try {
            BitmapFactory.Options probe = new BitmapFactory.Options();
            probe.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, probe);
            int sample = 1;
            while (Math.max(probe.outWidth / sample, probe.outHeight / sample) > maxDim) sample *= 2;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opt.inDither = true;
            opt.inSampleSize = sample;
            return BitmapFactory.decodeFile(path, opt);
        } catch (Throwable ignored) { return null; }
    }

    public String debugSummary() { return String.format(Locale.US, "%s bitmap=%s", mediaPath, source == null ? "none" : source.getWidth() + "x" + source.getHeight()); }
}
