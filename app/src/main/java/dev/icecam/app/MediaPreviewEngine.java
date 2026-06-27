package dev.icecam.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import java.io.File;
import java.util.Locale;

/** Modern media decode: PNG/JPEG/HEIF/HEIC + video frame extract. */
public final class MediaPreviewEngine {
    private MediaPreviewEngine() {}

    public static final String[] PICK_MIME_TYPES = new String[]{
            "image/*", "video/*",
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/heic", "image/heif",
            "video/mp4", "video/quicktime", "video/x-matroska", "video/webm"
    };

    public static boolean isImagePath(String path) {
        if (path == null) return false;
        String p = path.toLowerCase(Locale.US);
        return p.endsWith(".jpg") || p.endsWith(".jpeg") || p.endsWith(".png")
                || p.endsWith(".webp") || p.endsWith(".bmp")
                || p.endsWith(".heic") || p.endsWith(".heif");
    }

    public static boolean isVideoPath(String path) {
        if (path == null) return false;
        String p = path.toLowerCase(Locale.US);
        return p.endsWith(".mp4") || p.endsWith(".mkv") || p.endsWith(".webm")
                || p.endsWith(".mov") || p.endsWith(".avi") || p.endsWith(".3gp")
                || p.endsWith(".m4v");
    }

    public static String mediaKind(String path) {
        if (isVideoPath(path)) return "video";
        if (isImagePath(path)) return "image";
        return "unknown";
    }

    public static Bitmap decodeImage(Context ctx, String path, int maxDim, boolean colorCorrect) {
        if (path == null || path.isEmpty() || !isImagePath(path)) return null;
        try {
            Bitmap bm = decodeImageInternal(ctx, path, maxDim);
            if (bm == null) return null;
            if (colorCorrect) {
                float strength = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE)
                        .getFloat("ColorCorrectStrength", 0.35f);
                if (strength > 0f) {
                    Bitmap corrected = ColorCorrector.apply(bm, strength);
                    if (corrected != bm) { bm.recycle(); bm = corrected; }
                }
            }
            return bm;
        } catch (Throwable t) {
            SmartLogger.get(ctx).w("media", "decodeImage failed " + path + ": " + t.getMessage());
            return null;
        }
    }

    private static Bitmap decodeImageInternal(Context ctx, String path, int maxDim) throws Exception {
        File f = new File(path);
        if (Build.VERSION.SDK_INT >= 28 && f.exists()) {
            ImageDecoder.Source src = ImageDecoder.createSource(f);
            Bitmap bm = ImageDecoder.decodeBitmap(src, (decoder, info, s) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                int w = info.getSize().getWidth();
                int h = info.getSize().getHeight();
                int max = Math.max(w, h);
                if (max > maxDim) {
                    float scale = maxDim / (float) max;
                    decoder.setTargetSize(Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)));
                }
            });
            if (bm != null) return bm;
        }
        BitmapFactory.Options probe = new BitmapFactory.Options();
        probe.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, probe);
        if (probe.outWidth <= 0 || probe.outHeight <= 0) return null;
        int sample = 1;
        while (Math.max(probe.outWidth / sample, probe.outHeight / sample) > maxDim) sample *= 2;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inSampleSize = sample;
        return BitmapFactory.decodeFile(path, opt);
    }

    public static Bitmap extractVideoFrame(Context ctx, String path, int maxDim) {
        if (!isVideoPath(path)) return null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            Bitmap frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (frame == null) return null;
            return scaleBitmap(frame, maxDim);
        } catch (Throwable t) {
            SmartLogger.get(ctx).w("media", "video frame failed " + path + ": " + t.getMessage());
            return null;
        } finally {
            try { retriever.release(); } catch (Throwable ignored) {}
        }
    }

    public static Bitmap renderWithTransform(Context ctx, String path, TransformState tx, int outW, int outH) {
        String kind = mediaKind(path);
        Bitmap src = "video".equals(kind)
                ? extractVideoFrame(ctx, path, 1800)
                : decodeImage(ctx, path, 1800, true);
        if (src == null) return null;
        Bitmap out = Bitmap.createBitmap(Math.max(64, outW), Math.max(64, outH), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        c.drawColor(Color.BLACK);
        TransformState s = tx == null ? new TransformState() : tx;
        float sw = src.getWidth();
        float sh = src.getHeight();
        float base = s.mode == TransformState.MODE_FILL ? Math.max(outW / sw, outH / sh)
                : (s.mode == TransformState.MODE_STRETCH ? 1f : Math.min(outW / sw, outH / sh));
        Matrix m = new Matrix();
        m.postTranslate(-sw / 2f, -sh / 2f);
        if (s.mirrorH()) m.postScale(-1f, 1f);
        if (s.mirrorV()) m.postScale(1f, -1f);
        m.postRotate(s.rotationQuadrant() * 90f);
        if (s.mode == TransformState.MODE_STRETCH) m.postScale((outW / sw) * s.zoomX, (outH / sh) * s.zoomY);
        else m.postScale(base * s.zoomX, base * s.zoomY);
        m.postTranslate(outW / 2f + s.panX * (outW / 2f), outH / 2f - s.panY * (outH / 2f));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        c.drawBitmap(src, m, paint);
        src.recycle();
        return out;
    }

    private static Bitmap scaleBitmap(Bitmap src, int maxDim) {
        int w = src.getWidth();
        int h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxDim) return src;
        float scale = maxDim / (float) max;
        int nw = Math.max(1, Math.round(w * scale));
        int nh = Math.max(1, Math.round(h * scale));
        Bitmap out = Bitmap.createScaledBitmap(src, nw, nh, true);
        if (out != src) src.recycle();
        return out;
    }
}
