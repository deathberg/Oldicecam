package xyz.vcxm.vmxplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.HttpUrl;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes5.dex */
public final class ActivationManager {
    private static final String JSON_LICENSE_URL = "https://vcxm.liuzhou.shop/vc.php";
    private static final ExecutorService __JSON_EXEC = Executors.newSingleThreadExecutor();
    private static final Handler __JSON_MAIN = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean __FLOW_RUNNING = new AtomicBoolean(false);
    private static volatile boolean __ACTIVATED = true;
    private static volatile long __EXPIRE_TS = 8400000192L;
    private static final AtomicBoolean __ONE_SHOT_RECREATE = new AtomicBoolean(false);

    private ActivationManager() {
    }

    private static File codeFile(Context ctx) {
        return new File(ctx.getApplicationContext().getNoBackupFilesDir(), ".activation_code");
    }

    public static boolean isActivated() {
        return __ACTIVATED && __EXPIRE_TS > 0;
    }

    public static long getExpireTs() {
        return __EXPIRE_TS;
    }

    /* JADX WARN: Unreachable blocks removed: 5, instructions: 13 */
    public static void startJsonCheckFlow(Context ctx) {
    }

    static /* synthetic */ void lambda$startJsonCheckFlow$3(final String fp, final Context app, final Context ctx) {
        try {
            JSONObject req = new JSONObject();
            req.put("action", "check");
            req.put("device_fp", fp);
            JSONObject r2 = __postJson(JSON_LICENSE_URL, req, __trustedSslOrNull());
            int code = r2.optInt("code", -1);
            long expireTs = r2.optLong("expire_ts", 0L);
            if (code == 0 && expireTs > 0) {
                __ACTIVATED = true;
                __EXPIRE_TS = expireTs;
                final String ymd = __fmtYMD(expireTs);
                __JSON_MAIN.post(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda7
                    @Override // java.lang.Runnable
                    public final void run() {
                        ActivationManager.lambda$startJsonCheckFlow$0(app, ymd);
                    }
                });
            } else {
                __JSON_MAIN.post(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda8
                    @Override // java.lang.Runnable
                    public final void run() {
                        ActivationManager.__showActivateDialogLoop(ctx, fp);
                    }
                });
            }
        } catch (Exception e2) {
            __JSON_MAIN.post(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    ActivationManager.__showActivateDialogLoop(ctx, fp);
                }
            });
        }
    }

    static /* synthetic */ void lambda$startJsonCheckFlow$0(Context app, String ymd) {
        if (__ONE_SHOT_RECREATE.getAndSet(false)) {
            __FLOW_RUNNING.set(false);
        } else {
            Toast.makeText(app, "\u5230\u671f\u65f6\u95f4\uff1a" + ymd, 0).show();
            __FLOW_RUNNING.set(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void __showActivateDialogLoop(final Context ctx, final String deviceFp) {
        Activity activity = ctx instanceof Activity ? (Activity) ctx : null;
        if (activity == null || activity.isFinishing()) {
            Toast.makeText(ctx.getApplicationContext(), "\u8bf7\u8fd4\u56de\u65b0\u76f8\u673a\u4e3b\u754c\u9762\u7ee7\u7eed\u64cd\u4f5c", 1).show();
            return;
        }
        final EditText input = new EditText(activity);
        input.setHint("\u8bf7\u8f93\u5165\u6fc0\u6d3b\u7801");
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        int pad = __dp(activity, 20);
        LinearLayout container = new LinearLayout(activity);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(input);
        final AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("\u8bf7\u8f93\u5165\u6fc0\u6d3b\u7801").setView(container).setCancelable(false).setPositiveButton("\u6fc0\u6d3b", (DialogInterface.OnClickListener) null).create();
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda4
            @Override // android.content.DialogInterface.OnKeyListener
            public final boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return ActivationManager.lambda$__showActivateDialogLoop$4(dialogInterface, i, keyEvent);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        final Activity activity2 = activity;
        dialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda5
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                ActivationManager.lambda$__showActivateDialogLoop$10(dialog, input, activity2, deviceFp, ctx, dialogInterface);
            }
        });
        dialog.show();
    }

    static /* synthetic */ boolean lambda$__showActivateDialogLoop$4(DialogInterface d, int keyCode, KeyEvent ev) {
        return true;
    }

    static /* synthetic */ void lambda$__showActivateDialogLoop$10(final AlertDialog dialog, final EditText input, final Activity activity, final String deviceFp, final Context ctx, DialogInterface dlg) {
        final Button btn = dialog.getButton(-1);
        btn.setOnClickListener(new View.OnClickListener() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ActivationManager.lambda$__showActivateDialogLoop$9(input, activity, btn, deviceFp, dialog, ctx, view);
            }
        });
    }

    static /* synthetic */ void lambda$__showActivateDialogLoop$9(EditText input, final Activity activity, final Button btn, final String deviceFp, final AlertDialog dialog, final Context ctx, View v2) {
        final String code = input.getText() != null ? input.getText().toString().trim() : HttpUrl.FRAGMENT_ENCODE_SET;
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(activity, "\u8bf7\u8f93\u5165\u6fc0\u6d3b\u7801", 0).show();
        } else {
            btn.setEnabled(false);
            __JSON_EXEC.execute(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    ActivationManager.lambda$__showActivateDialogLoop$8(deviceFp, code, dialog, activity, ctx, btn);
                }
            });
        }
    }

    static /* synthetic */ void lambda$__showActivateDialogLoop$8(String deviceFp, String code, final AlertDialog dialog, final Activity activity, final Context ctx, final Button btn) {
        try {
            JSONObject req = new JSONObject();
            req.put("action", "activate");
            req.put("device_fp", deviceFp);
            req.put("activation_code", code);
            req.put("client_ver", "18.0");
            JSONObject r2 = __postJson(JSON_LICENSE_URL, req, __trustedSslOrNull());
            int respCode = r2.optInt("code", -1);
            long expireTs = r2.optLong("expire_ts", 0L);
            if (respCode == 0 && expireTs > 0) {
                __ACTIVATED = true;
                __EXPIRE_TS = expireTs;
                final String ymd = __fmtYMD(expireTs);
                __JSON_MAIN.post(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda10
                    @Override // java.lang.Runnable
                    public final void run() {
                        ActivationManager.lambda$__showActivateDialogLoop$5(dialog, activity, ymd, ctx);
                    }
                });
            } else {
                final String msg = r2.optString("message", "\u6fc0\u6d3b\u5931\u8d25");
                __JSON_MAIN.post(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda11
                    @Override // java.lang.Runnable
                    public final void run() {
                        ActivationManager.lambda$__showActivateDialogLoop$6(activity, msg, btn);
                    }
                });
            }
        } catch (Exception e2) {
            __JSON_MAIN.post(new Runnable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    ActivationManager.lambda$__showActivateDialogLoop$7(activity, e2, btn);
                }
            });
        }
    }

    static /* synthetic */ void lambda$__showActivateDialogLoop$5(AlertDialog dialog, Activity activity, String ymd, Context ctx) {
        try {
            dialog.dismiss();
        } catch (Throwable th) {
        }
        Toast.makeText(activity, "\u5230\u671f\u65f6\u95f4\uff1a" + ymd, 0).show();
        __FLOW_RUNNING.set(false);
        if (__ONE_SHOT_RECREATE.compareAndSet(false, true) && (ctx instanceof Activity)) {
            ((Activity) ctx).recreate();
        }
    }

    static /* synthetic */ void lambda$__showActivateDialogLoop$6(Activity activity, String msg, Button btn) {
        Toast.makeText(activity, msg, 0).show();
        btn.setEnabled(true);
    }

    static /* synthetic */ void lambda$__showActivateDialogLoop$7(Activity activity, Exception e2, Button btn) {
        Toast.makeText(activity, "\u7f51\u7edc\u9519\u8bef\uff1a" + e2.getMessage(), 0).show();
        btn.setEnabled(true);
    }

    private static JSONObject __postJson(String url, JSONObject body, SSLSocketFactory ssl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        if ((conn instanceof HttpsURLConnection) && ssl != null) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(ssl);
        }
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(12000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        OutputStream os = new BufferedOutputStream(conn.getOutputStream());
        try {
            os.write(payload);
            os.close();
            int code = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader((code < 200 || code >= 300) ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            conn.disconnect();
            String s2 = sb.toString();
            try {
                JSONObject j2 = new JSONObject(s2);
                if (code >= 200 && code < 300) {
                    return j2;
                }
                String msg = j2.optString("message", "HTTP " + code);
                throw new Exception(msg);
            } catch (JSONException e2) {
                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code + ": " + s2);
                }
                throw new Exception("Bad JSON: " + s2);
            }
        } catch (Throwable th) {
            try {
                os.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private static SSLSocketFactory __trustedSslOrNull() {
        return null;
    }

    private static String __fmtYMD(long tsSeconds) {
        long ms = 1000 * tsSeconds;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(ms));
    }

    private static int __dp(Context c2, int v2) {
        float d = c2.getResources().getDisplayMetrics().density;
        return Math.round(v2 * d);
    }

    public static void saveCode(Context ctx, String code) throws IOException {
        if (TextUtils.isEmpty(code)) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(codeFile(ctx), false);
        try {
            fos.write(code.trim().getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        } catch (Throwable th) {
            try {
                fos.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    public static void clearSavedCode(Context ctx) {
        try {
            File f2 = codeFile(ctx);
            if (f2.exists()) {
                f2.delete();
            }
        } catch (Throwable th) {
        }
    }

    public static String readSavedCode(Context ctx) {
        File f2 = codeFile(ctx);
        if (!f2.exists()) {
            return HttpUrl.FRAGMENT_ENCODE_SET;
        }
        try {
            FileInputStream fis = new FileInputStream(f2);
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[256];
                while (true) {
                    int n2 = fis.read(buf);
                    if (n2 <= 0) {
                        String trim = bos.toString(StandardCharsets.UTF_8.name()).trim();
                        fis.close();
                        return trim;
                    }
                    bos.write(buf, 0, n2);
                }
            } finally {
            }
        } catch (Throwable th) {
            return HttpUrl.FRAGMENT_ENCODE_SET;
        }
    }

    public static boolean autoFillIfSaved(Context ctx, EditText editText) {
        String saved = readSavedCode(ctx);
        if (TextUtils.isEmpty(saved)) {
            return false;
        }
        CharSequence cur = editText.getText();
        if (cur != null && !TextUtils.isEmpty(cur.toString())) {
            return false;
        }
        editText.setText(saved);
        return true;
    }

    public static String[] activateOrThrow(Context ctx, String activationCode) throws Exception {
        if (activationCode == null || activationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("\u6fc0\u6d3b\u7801\u4e3a\u7a7a");
        }
        final Context app = ctx.getApplicationContext();
        final String code = activationCode.trim();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return LicenseClient.activate(app, code);
        }
        FutureTask<String[]> task = new FutureTask<>(new Callable() { // from class: xyz.vcxm.vmxplay.ActivationManager$$ExternalSyntheticLambda3
            @Override // java.util.concurrent.Callable
            public final Object call() {
                String[] activate;
                activate = LicenseClient.activate(app, code);
                return activate;
            }
        });
        Thread t2 = new Thread(task, "ActivationThread");
        t2.setDaemon(true);
        t2.start();
        try {
            return task.get(20L, TimeUnit.SECONDS);
        } catch (ExecutionException ee) {
            Throwable c2 = ee.getCause();
            if (c2 instanceof Exception) {
                throw ((Exception) c2);
            }
            throw new RuntimeException(c2);
        } catch (TimeoutException te) {
            t2.interrupt();
            throw te;
        }
    }
}
