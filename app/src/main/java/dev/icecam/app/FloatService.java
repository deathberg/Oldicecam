package dev.icecam.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.icecam.app.config.ActionRanges;

public class FloatService extends Service {
    private static final boolean FLOAT_AUTO_COMMIT = false;

    private WindowManager wm;
    private View panel;
    private WindowManager.LayoutParams lp;
    private SharedPreferences prefs;
    private AppLogger log;
    private VliveBinderClient binder;
    private ExecutorService txExecutor;
    private TransformController controller;
    private TextView state;
    private int lastX, lastY;
    private float touchX, touchY;

    @Override public IBinder onBind(Intent i){ return null; }

    @Override public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);
        log = new AppLogger(this);
        binder = new VliveBinderClient(log);
        binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
        txExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "float-tx22"));
        controller = TransformController.get(this);
    }

    @Override public int onStartCommand(Intent i, int flags, int startId) {
        if (i != null && "stop".equals(i.getAction())) { stopSelf(); return START_NOT_STICKY; }
        show();
        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (txExecutor != null) {
            txExecutor.shutdownNow();
            try { txExecutor.awaitTermination(500, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            txExecutor = null;
        }
        if (binder != null) {
            binder.release();
            binder = null;
        }
        try { if (wm != null && panel != null) wm.removeView(panel); } catch (Throwable ignored) {}
        panel = null;
        wm = null;
        super.onDestroy();
    }

    private void show() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            toast("Overlay permission is not granted");
            Intent it = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
            return;
        }
        if (panel != null) return;
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        lp = new WindowManager.LayoutParams(dp(264), WindowManager.LayoutParams.WRAP_CONTENT, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = prefs.getInt("FloatX", dp(22));
        lp.y = prefs.getInt("FloatY", dp(120));
        panel = buildPanel();
        wm.addView(panel, lp);
        refresh();
        controller.bus().store().addListener(appState -> refresh());
        log.log("float", BuildInfo.VERSION_NAME + " floating controls started autoCommit=" + FLOAT_AUTO_COMMIT);
    }

    private View buildPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(12));
        box.setBackground(bg(0xee151b28, dp(26), 0x66ffffff));
        box.setOnTouchListener((v, e) -> drag(e));

        TextView title = tv(BuildInfo.BUILD_LABEL + " Remote", 15, true);
        title.setGravity(Gravity.CENTER);
        title.setOnTouchListener((v, e) -> drag(e));
        box.addView(title, new LinearLayout.LayoutParams(-1, dp(30)));

        state = tv("", 10, false);
        state.setGravity(Gravity.CENTER);
        state.setTextColor(0xffdbe7f4);
        box.addView(state);

        LinearLayout r0 = row();
        r0.addView(btn("START / RESTORE", v -> { startOrRestore(); refreshDelayed(); }), wideWeight());
        box.addView(r0);

        LinearLayout r1 = row();
        r1.addView(btn("Zoom +", v -> mutate("zoom+")), weight());
        r1.addView(btn("Up", v -> mutate("up")), weight());
        r1.addView(btn("Zoom -", v -> mutate("zoom-")), weight());
        box.addView(r1);

        LinearLayout r2 = row();
        r2.addView(btn("Left", v -> mutate("left")), weight());
        r2.addView(btn("Center", v -> mutate("center")), weight());
        r2.addView(btn("Right", v -> mutate("right")), weight());
        box.addView(r2);

        LinearLayout r3 = row();
        r3.addView(btn("Fit/Fill", v -> mutate("fit-fill")), weight());
        r3.addView(btn("Down", v -> mutate("down")), weight());
        r3.addView(btn("Crop", v -> mutate("crop")), weight());
        box.addView(r3);

        LinearLayout r4 = row();
        r4.addView(btn("ROT +90", v -> mutate("rot+90")), weight());
        r4.addView(btn("MIR X", v -> mutate("mirror-x")), weight());
        r4.addView(btn("Commit", v -> commit()), weight());
        box.addView(r4);

        TextView faceLabel = tv("Face actions (TX22)", 9, true);
        faceLabel.setGravity(Gravity.CENTER);
        faceLabel.setTextColor(0xff8ab4d4);
        faceLabel.setPadding(0, dp(4), 0, dp(2));
        box.addView(faceLabel);

        LinearLayout rFace1 = row();
        rFace1.addView(btn("EYE", v -> triggerActionSeek(ActionRanges.ACTION_EYE)), weight());
        rFace1.addView(btn("HEAD", v -> triggerActionSeek(ActionRanges.ACTION_HEAD_UP)), weight());
        rFace1.addView(btn("MOUTH", v -> triggerActionSeek(ActionRanges.ACTION_MOUTH)), weight());
        box.addView(rFace1);

        LinearLayout rFace2 = row();
        rFace2.addView(btn("L", v -> triggerActionSeek(ActionRanges.ACTION_TURN_LEFT)), weight());
        rFace2.addView(btn("CTR", v -> triggerActionSeek(ActionRanges.ACTION_CENTER)), weight());
        rFace2.addView(btn("R", v -> triggerActionSeek(ActionRanges.ACTION_TURN_RIGHT)), weight());
        box.addView(rFace2);

        LinearLayout rFace3 = row();
        rFace3.addView(btn("NOD", v -> triggerActionSeek(ActionRanges.ACTION_NOD)), wideWeight());
        box.addView(rFace3);

        LinearLayout r5 = row();
        r5.addView(btn("Open app", v -> openApp()), weight());
        r5.addView(btn("Close", v -> stopSelf()), weight());
        box.addView(r5);
        return box;
    }

    private boolean drag(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = lp.x; lastY = lp.y; touchX = e.getRawX(); touchY = e.getRawY(); return true;
            case MotionEvent.ACTION_MOVE:
                lp.x = lastX + (int)(e.getRawX() - touchX);
                lp.y = lastY + (int)(e.getRawY() - touchY);
                wm.updateViewLayout(panel, lp);
                return true;
            case MotionEvent.ACTION_UP:
                prefs.edit().putInt("FloatX", lp.x).putInt("FloatY", lp.y).apply(); return true;
        }
        return false;
    }

    private void startOrRestore() {
        if (controller.isBusy()) { toast("Busy"); return; }
        if (prefs.getBoolean("ReplacementActive", false)) controller.restoreCamera(TransformController.Source.FLOAT);
        else controller.startReplacement(TransformController.Source.FLOAT);
    }

    private void mutate(String op) {
        TransformState s = controller.mutate(TransformController.Source.FLOAT, op, FLOAT_AUTO_COMMIT);
        log.log("float", "command source=FLOAT op=" + op + " routed=TransformController autoCommit=" + FLOAT_AUTO_COMMIT + " " + s.summary());
        refresh();
    }

    private void commit() {
        log.log("float", "commit routed to TransformController; no direct Binder/TX in FloatService");
        controller.commit(TransformController.Source.FLOAT, "float-commit");
        refreshDelayed();
    }

    private void triggerActionSeek(int actionId) {
        if (txExecutor == null || binder == null) return;
        final long beginUs = actionRangeBegin(actionId);
        final long endUs = actionRangeEnd(actionId);
        log.log("float", "TX22 seek action=" + actionId + " beginUs=" + beginUs + " endUs=" + endUs);
        txExecutor.execute(() -> {
            try {
                binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
                int rc = binder.setRange(beginUs, endUs);
                log.log("float", "TX22 result action=" + actionId + " rc=" + rc);
            } catch (Throwable t) {
                log.log("float", "TX22 error action=" + actionId + ": " + t);
            }
        });
    }

    private long actionRangeBegin(int actionId) {
        return prefs.getLong(ActionRanges.PREF_BEGIN_PREFIX + actionId, ActionRanges.defaultBegin(actionId));
    }

    private long actionRangeEnd(int actionId) {
        return prefs.getLong(ActionRanges.PREF_END_PREFIX + actionId, ActionRanges.defaultEnd(actionId));
    }

    private void openApp() {
        Intent it = new Intent(this, MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(it);
    }

    private void refreshDelayed() {
        refresh();
        if (state != null) state.postDelayed(this::refresh, 700);
        if (state != null) state.postDelayed(this::refresh, 1700);
    }

    private void refresh() {
        if (state == null) return;
        boolean active = prefs.getBoolean("ReplacementActive", false);
        String phase = prefs.getString("IceCamState", "IDLE");
        TransformState s = TransformState.load(prefs);
        state.setText((active ? "ACTIVE" : "OFF") + " · " + phase + " · #" + prefs.getLong("LastMarkerId", 0L) + "\n" + s.modeName() + " z=" + String.format(java.util.Locale.US, "%.2f", s.zoomX) + " rot=" + (s.rotationQuadrant() == 3 ? -90 : s.rotationQuadrant() * 90) + "°" + "\nFLOAT: Runtime CommandBus · no preview bake");
        state.setTextColor(active ? 0xff62ff91 : 0xffdbe7f4);
    }

    private Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false); b.setTextSize(9); b.setTextColor(Color.WHITE); b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setPadding(0, 0, 0, 0); b.setMinHeight(0); b.setMinimumHeight(0); b.setBackground(UiKit.neonButton(0xff30384a, UiKit.CYAN, dp(15))); b.setOnClickListener(l); return b;
    }
    private TextView tv(String s, int sp, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(Color.WHITE); if (bold) t.setTypeface(Typeface.DEFAULT_BOLD); return t; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER); return l; }
    private LinearLayout.LayoutParams weight() { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(32), 1); lp.setMargins(dp(3), dp(3), dp(3), dp(3)); return lp; }
    private LinearLayout.LayoutParams wideWeight() { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(38)); lp.setMargins(dp(3), dp(3), dp(3), dp(5)); return lp; }
    private GradientDrawable bg(int color, int radius, int stroke) { GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(radius); g.setStroke(1, stroke); return g; }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); log.log("float", s); }
}
