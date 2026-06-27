package dev.icecam.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class MediaResolver {
    public static String resolveToReadableFile(Context ctx, Uri uri, AppLogger log) {
        try {
            String name = displayName(ctx, uri);
            if (name == null || name.trim().length() == 0) name = "selected_media.bin";
            name = name.replaceAll("[^A-Za-z0-9._-]", "_");
            File out = new File(ctx.getExternalFilesDir(null), "selected_" + System.currentTimeMillis() + "_" + name);
            try (InputStream in = ctx.getContentResolver().openInputStream(uri); FileOutputStream fos = new FileOutputStream(out)) {
                if (in == null) throw new IllegalStateException("openInputStream returned null");
                byte[] buf = new byte[1024 * 128];
                int n;
                while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
            }
            log.log("media", "copied gallery uri to " + out.getAbsolutePath() + " size=" + out.length());
            return out.getAbsolutePath();
        } catch (Throwable t) {
            log.log("media", "resolve failed: " + t);
            return uri.toString();
        }
    }

    private static String displayName(Context ctx, Uri uri) {
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(uri, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Throwable ignored) { } finally { if (c != null) c.close(); }
        String p = uri.getLastPathSegment();
        return p == null ? "media.bin" : p;
    }
}
