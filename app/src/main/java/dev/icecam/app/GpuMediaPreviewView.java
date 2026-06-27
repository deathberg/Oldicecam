package dev.icecam.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * GPU-accelerated preview: TextureView + MediaPlayer for video, hardware ImageView for stills.
 */
public final class GpuMediaPreviewView extends FrameLayout implements TextureView.SurfaceTextureListener {
    private final TextureView texture;
    private final ImageView image;
    private final TextView badge;
    private MediaPlayer player;
    private String mediaPath = "";
    private TransformState transform = new TransformState();
    private boolean highlighted;
    private SmartLogger slog;

    public GpuMediaPreviewView(Context ctx) { this(ctx, null); }
    public GpuMediaPreviewView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        slog = SmartLogger.get(ctx);
        setBackgroundColor(0xff05070b);
        texture = new TextureView(ctx);
        texture.setOpaque(false);
        texture.setSurfaceTextureListener(this);
        addView(texture, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        image = new ImageView(ctx);
        image.setScaleType(ImageView.ScaleType.MATRIX);
        image.setBackgroundColor(Color.BLACK);
        addView(image, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        badge = new TextView(ctx);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(10);
        badge.setPadding(dp(6), dp(3), dp(6), dp(3));
        badge.setBackgroundColor(0xaa000000);
        LayoutParams bp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        bp.setMargins(dp(4), dp(4), 0, 0);
        addView(badge, bp);
        showImageLayer(true);
    }

    public void setSlotLabel(String label) { badge.setText(label == null ? "" : label); }
    public void setHighlighted(boolean on) {
        highlighted = on;
        setBackgroundColor(on ? 0xff0d2838 : 0xff05070b);
        float a = on ? 1f : 0.92f;
        setAlpha(a);
    }

    public void setMediaPath(String path) {
        path = path == null ? "" : path;
        if (path.equals(mediaPath)) return;
        releasePlayer();
        mediaPath = path;
        image.setImageBitmap(null);
        if (MediaPreviewEngine.isVideoPath(path)) {
            showImageLayer(false);
            Bitmap thumb = MediaPreviewEngine.extractVideoFrame(getContext(), path, 720);
            if (thumb != null) {
                image.setImageBitmap(thumb);
                image.setVisibility(VISIBLE);
                image.setAlpha(0.35f);
            }
            startVideo(path);
        } else if (MediaPreviewEngine.isImagePath(path)) {
            showImageLayer(true);
            Bitmap bm = MediaPreviewEngine.decodeImage(getContext(), path, 1400, true);
            image.setImageBitmap(bm);
            applyMatrix();
        } else {
            showImageLayer(true);
            image.setImageBitmap(null);
        }
        slog.d("preview", "path=" + shortName(path) + " kind=" + MediaPreviewEngine.mediaKind(path));
    }

    public void setTransformState(TransformState state) {
        transform = state == null ? new TransformState() : state;
        applyMatrix();
        if (player != null && player.isPlaying()) applyVideoTransform();
    }

    public void release() {
        releasePlayer();
        image.setImageBitmap(null);
    }

    private void showImageLayer(boolean imagePrimary) {
        image.setAlpha(1f);
        image.setVisibility(VISIBLE);
        texture.setVisibility(imagePrimary ? GONE : VISIBLE);
        if (imagePrimary) image.bringToFront();
        else texture.bringToFront();
        badge.bringToFront();
    }

    private void startVideo(String path) {
        if (!texture.isAvailable()) return;
        try {
            releasePlayer();
            player = new MediaPlayer();
            player.setDataSource(path);
            player.setLooping(true);
            player.setSurface(new Surface(texture.getSurfaceTexture()));
            player.setOnPreparedListener(mp -> {
                mp.start();
                applyVideoTransform();
                image.animate().alpha(0f).setDuration(180).withEndAction(() -> image.setVisibility(GONE)).start();
                slog.d("preview", "video playing " + shortName(path));
            });
            player.setOnErrorListener((mp, what, extra) -> {
                slog.w("preview", "MediaPlayer error what=" + what + " extra=" + extra);
                showImageLayer(true);
                return true;
            });
            player.prepareAsync();
        } catch (Throwable t) {
            slog.w("preview", "video start failed: " + t.getMessage());
            showImageLayer(true);
        }
    }

    private void releasePlayer() {
        if (player == null) return;
        try { player.stop(); } catch (Throwable ignored) {}
        try { player.release(); } catch (Throwable ignored) {}
        player = null;
    }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (MediaPreviewEngine.isVideoPath(mediaPath)) startVideo(mediaPath);
        else applyVideoTransform();
    }
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        applyMatrix();
        applyVideoTransform();
    }
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releasePlayer();
        return true;
    }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        SmartLogger.get(getContext()).frame("preview-" + (highlighted ? "main" : "slot"));
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        applyMatrix();
        applyVideoTransform();
    }

    private void applyMatrix() {
        if (image.getDrawable() == null || getWidth() <= 0 || getHeight() <= 0) {
            image.setImageMatrix(new Matrix());
            return;
        }
        int iw = image.getDrawable().getIntrinsicWidth();
        int ih = image.getDrawable().getIntrinsicHeight();
        if (iw <= 0 || ih <= 0) return;
        image.setImageMatrix(buildTransformMatrix(iw, ih, getWidth(), getHeight()));
    }

    private void applyVideoTransform() {
        if (player == null || getWidth() <= 0 || getHeight() <= 0) return;
        int vw = player.getVideoWidth();
        int vh = player.getVideoHeight();
        if (vw <= 0 || vh <= 0) return;
        texture.setTransform(buildTransformMatrix(vw, vh, getWidth(), getHeight()));
    }

    private Matrix buildTransformMatrix(float sw, float sh, float vw, float vh) {
        float base;
        if (transform.mode == TransformState.MODE_FILL) base = Math.max(vw / sw, vh / sh);
        else if (transform.mode == TransformState.MODE_STRETCH) base = 1f;
        else base = Math.min(vw / sw, vh / sh);
        Matrix m = new Matrix();
        m.postTranslate(-sw / 2f, -sh / 2f);
        if (transform.mirrorH()) m.postScale(-1f, 1f);
        if (transform.mirrorV()) m.postScale(1f, -1f);
        m.postRotate(transform.rotationQuadrant() * 90f);
        if (transform.mode == TransformState.MODE_STRETCH) {
            m.postScale((vw / sw) * transform.zoomX, (vh / sh) * transform.zoomY);
        } else {
            m.postScale(base * transform.zoomX, base * transform.zoomY);
        }
        m.postTranslate(vw / 2f + transform.panX * (vw / 2f), vh / 2f - transform.panY * (vh / 2f));
        return m;
    }

    private static String shortName(String p) {
        if (p == null || p.isEmpty()) return "empty";
        int i = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        String n = i >= 0 ? p.substring(i + 1) : p;
        return n.length() > 28 ? n.substring(0, 25) + "…" : n;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
}
