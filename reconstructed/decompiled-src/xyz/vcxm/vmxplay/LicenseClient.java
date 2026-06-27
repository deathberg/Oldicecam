package xyz.vcxm.vmxplay;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.HttpUrl;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.vcxm.vmxplay.tls.PkixAtDateTrust;
import xyz.vcxm.vmxplay.util.Sntp;

/* loaded from: classes5.dex */
final class LicenseClient {
    private static final int CLOCK_SKEW_SEC = 900;
    private static final int CONNECT_TIMEOUT = 12000;
    private static final String ENDPOINT = "https://vcxm.liuzhou.shop/lic/public/api/activate.php";
    private static final String[] MS_NTP = {"time.windows.com", "time1.windows.com", "time2.windows.com", "time3.windows.com", "time4.windows.com", "time5.windows.com", "time6.windows.com", "time7.windows.com"};
    private static final int NTP_TIMEOUT_MS = 1500;
    private static final int READ_TIMEOUT = 12000;

    private LicenseClient() {
    }

    static String[] activate(Context ctx, String activationCode) throws Exception {
        String deviceFp = DeviceFingerprint.getOrCreate(ctx);
        Long offsetSec = Sntp.getOffsetSecondsAny(MS_NTP, NTP_TIMEOUT_MS);
        JSONObject body = new JSONObject();
        body.put("activation_code", activationCode);
        body.put("device_fp", deviceFp);
        URL url = new URL(ENDPOINT);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(12000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        if (offsetSec != null && Math.abs(offsetSec.longValue()) >= 900) {
            long ntpMillis = System.currentTimeMillis() + (offsetSec.longValue() * 1000);
            SSLSocketFactory sf = PkixAtDateTrust.factory(ntpMillis);
            conn.setSSLSocketFactory(sf);
            conn.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        } else {
            conn.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            conn.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        }
        OutputStream os = conn.getOutputStream();
        try {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            if (os != null) {
                os.close();
            }
            int code = conn.getResponseCode();
            String text = readAll((code < 200 || code >= 300) ? conn.getErrorStream() : conn.getInputStream());
            if (code < 200 || code >= 300) {
                String msg = "\u6fc0\u6d3b\u5931\u8d25(\u670d\u52a1\u5668\u8fd4\u56de\u5f02\u5e38)";
                try {
                    msg = new JSONObject(text).optString("error", "\u6fc0\u6d3b\u5931\u8d25(\u670d\u52a1\u5668\u8fd4\u56de\u5f02\u5e38)");
                } catch (Exception e2) {
                }
                throw new IOException(msg);
            }
            JSONObject json = new JSONObject(text);
            JSONArray arr = json.getJSONArray("data");
            String exp = normalizeSecondTs(arr.getString(0));
            String now = normalizeSecondTs(arr.getString(1));
            return new String[]{exp, now, "\u6fc0\u6d3b\u6210\u529f\uff01"};
        } catch (Throwable th) {
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) {
            return HttpUrl.FRAGMENT_ENCODE_SET;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[2048];
            while (true) {
                int n2 = br.read(buf);
                if (n2 <= 0) {
                    String sb2 = sb.toString();
                    br.close();
                    return sb2;
                }
                sb.append(buf, 0, n2);
            }
        } catch (Throwable th) {
            try {
                br.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private static String normalizeSecondTs(String s2) {
        try {
            long v2 = Long.parseLong(s2.trim());
            if (v2 >= 100000000000L) {
                v2 /= 1000;
            }
            return Long.toString(v2);
        } catch (Exception e2) {
            return s2;
        }
    }
}
