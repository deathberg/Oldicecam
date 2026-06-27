package xyz.vcxm.vmxplay;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import kotlin.UByte;
import okhttp3.HttpUrl;

/* loaded from: classes5.dex */
final class DeviceFingerprint {
    private static final String FP_FILE = "vmx_fp.dat";

    DeviceFingerprint() {
    }

    static String getOrCreate(Context ctx) {
        String cached = read(ctx);
        if (!TextUtils.isEmpty(cached)) {
            return cached;
        }
        String material = buildMaterial(ctx);
        String fp = base32NoPad(sha256(material.getBytes(StandardCharsets.UTF_8)));
        if (fp.length() > 26) {
            fp = fp.substring(0, 26);
        }
        write(ctx, fp);
        return fp;
    }

    private static String buildMaterial(Context ctx) {
        String androidId = HttpUrl.FRAGMENT_ENCODE_SET;
        try {
            androidId = Settings.Secure.getString(ctx.getContentResolver(), "android_id");
        } catch (Throwable th) {
        }
        return Build.BOARD + "|" + Build.BRAND + "|" + Build.DEVICE + "|" + Build.MANUFACTURER + "|" + Build.MODEL + "|" + (androidId == null ? HttpUrl.FRAGMENT_ENCODE_SET : androidId);
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e2) {
            return new byte[32];
        }
    }

    private static String base32NoPad(byte[] data) {
        StringBuilder out = new StringBuilder(((data.length * 8) + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b2 : data) {
            buffer = (buffer << 8) | (b2 & UByte.MAX_VALUE);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                out.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            out.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt((buffer << (5 - bitsLeft)) & 31));
        }
        return out.toString();
    }

    private static String read(Context ctx) {
        File f2 = new File(ctx.getFilesDir(), FP_FILE);
        if (!f2.exists()) {
            return null;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f2));
            try {
                String readLine = br.readLine();
                br.close();
                return readLine;
            } finally {
            }
        } catch (IOException e2) {
            return null;
        }
    }

    private static void write(Context ctx, String s2) {
        try {
            FileWriter fw = new FileWriter(new File(ctx.getFilesDir(), FP_FILE), false);
            try {
                fw.write(s2);
                fw.close();
            } finally {
            }
        } catch (IOException e2) {
        }
    }
}
