package dev.icecam.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
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

/** Floating stream remote — RE protocol (no TX24 geometry). */
public class FloatService extends Service {
    private WindowManager wm;
    private View panel;
    private WindowManager.LayoutParams lp;
    private SharedPreferences prefs;
    private SmartLogger slog;
    private StreamController stream;
    private TextView state;
    private Button startStopBtn;
    private int lastX, lastY;
    private float touchX, touchY;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);
        slog = SmartLogger.get(this);
        stream = StreamController.get(this);
    }

    @Override public int onStartCommand(Intent i, int flags, int startId) {
        if (i != null && "stop".equals(i.getAction())) { stopSelf(); return START_NOT_STICKY; }
        show();
        return START_STICKY;
    }

    @Override public void onDestroy() {
        try { if (wm != null && panel != null) wm.removeView(panel); } catch (Throwable ignored) {}
        panel = null;
        super.onDestroy();
    }

    private void show() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            toast("Разрешите overlay");
            Intent it = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
            return;
        }
        if (panel != null) return;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        lp = new WindowManager.LayoutParams(dp(280), WindowManager.LayoutParams.WRAP_CONTENT, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = prefs.getInt("FloatX", dp(16));
        lp.y = prefs.getInt("FloatY", dp(100));
        panel = buildPanel();
        wm.addView(panel, lp);
        refresh();
    }

    private View buildPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(8), dp(10), dp(10));
        box.setBackground(UiKit.fill(0xee101722, dp(22), 0x55ffffff));
        box.setOnTouchListener((v, e) -> drag(e));

        TextView title = tv("Stream Remote", 14, true);
        title.setGravity(Gravity.CENTER);
        title.setOnTouchListener((v, e) -> drag(e));
        box.addView(title, fullW(dp(26)));

        state = tv("", 9, false);
        state.setGravity(Gravity.CENTER);
        box.addView(state);

        startStopBtn = btn("● START", v -> startOrRestore(), UiKit.GREEN);
        box.addView(startStopBtn, fullW(dp(40)));

        LinearLayout rz = row();
        rz.addView(btn("＋", v -> mutate("zoom+")), weight());
        rz.addView(btn("ZOOM", v -> mutate("center")), weight());
        rz.addView(btn("－", v -> mutate("zoom-")), weight());
        box.addView(rz);

        LinearLayout rUp = row();
        rUp.addView(spacer(), weight());
        rUp.addView(btn("▲", v -> mutate("up")), weight());
        rUp.addView(spacer(), weight());
        box.addView(rUp);

        LinearLayout rMid = row();
        rMid.addView(btn("◀", v -> mutate("left")), weight());
        rMid.addView(btn("●", v -> mutate("center")), weight());
        rMid.addView(btn("▶", v -> mutate("right")), weight());
        box.addView(rMid);

        LinearLayout rDn = row();
        rDn.addView(spacer(), weight());
        rDn.addView(btn("▼", v -> mutate("down")), weight());
        rDn.addView(spacer(), weight());
        box.addView(rDn);

        LinearLayout rXform = row();
        rXform.addView(btn("↻90°", v -> mutate("rot+90")), weight());
        rXform.addView(btn("⇄", v -> mutate("mirror-x")), weight());
        rXform.addView(btn("⇅", v -> mutate("mirror-y")), weight());
        box.addView(rXform);

        LinearLayout rFit = row();
        rFit.addView(btn("FIT/FILL", v -> mutate("fit-fill")), weight());
        rFit.addView(btn("APPLY", v -> { stream.commit(StreamController.Source.FLOAT); refreshDelayed(); }), weight());
        box.addView(rFit);

        LinearLayout rSlots = row();
        for (int i = 1; i <= 4; i++) {
            final int slot = i;
            rSlots.addView(btn("M" + i, v -> selectSlot(slot)), weight());
        }
        box.addView(rSlots);

        LinearLayout rNav = row();
        rNav.addView(btn("App", v -> openApp()), weight());
        rNav.addView(btn("✕", v -> stopSelf()), weight());
        box.addView(rNav);
        return box;
    }

    private void selectSlot(int slot) {
        String path = prefs.getString("Slot" + slot + "Path", "");
        if (path.isEmpty()) { toast("M" + slot + " пуст"); return; }
        stream.selectSlot(StreamController.Source.FLOAT, slot, path);
        refreshDelayed();
    }

    private View spacer() { View v = new View(this); v.setBackgroundColor(Color.TRANSPARENT); return v; }

    private boolean drag(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = lp.x; lastY = lp.y; touchX = e.getRawX(); touchY = e.getRawY(); return true;
            case MotionEvent.ACTION_MOVE:
                lp.x = lastX + (int) (e.getRawX() - touchX);
                lp.y = lastY + (int) (e.getRawY() - touchY);
                wm.updateViewLayout(panel, lp);
                return true;
            case MotionEvent.ACTION_UP:
                prefs.edit().putInt("FloatX", lp.x).putInt("FloatY", lp.y).apply();
                return true;
        }
        return false;
    }

    private void startOrRestore() {
        if (stream.isBusy()) { toast("Занято"); return; }
        if (prefs.getBoolean("ReplacementActive", false)) stream.restoreCamera(StreamController.Source.FLOAT);
        else stream.startReplacement(StreamController.Source.FLOAT);
        refreshDelayed();
    }

    private void mutate(String op) {
        stream.mutate(StreamController.Source.FLOAT, op);
        refresh();
    }

    private void openApp() {
        Intent it = new Intent(this, MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(it);
    }

    private void refreshDelayed() {
        refresh();
        if (state != null) {
            state.postDelayed(this::refresh, 800);
            state.postDelayed(this::refresh, 2000);
        }
    }

    private void refresh() {
        if (state == null) return;
        boolean active = prefs.getBoolean("ReplacementActive", false);
        boolean busy = stream.isBusy();
        String phase = prefs.getString("IceCamState", "IDLE");
        StreamGeometry g = StreamGeometry.load(prefs);
        int slot = activeSlot();

        if (startStopBtn != null) {
            if (busy) {
                startStopBtn.setText("● …");
                startStopBtn.setBackground(UiKit.neonButton(UiKit.PANEL_3, UiKit.WARN, dp(14)));
                startStopBtn.setEnabled(false);
            } else if (active) {
                startStopBtn.setText("■ STOP");
                startStopBtn.setBackground(UiKit.neonButton(UiKit.RED, 0xffff8090, dp(14)));
                startStopBtn.setEnabled(true);
            } else {
                startStopBtn.setText("● START");
                startStopBtn.setBackground(UiKit.neonButton(UiKit.GREEN, 0xff5cffaa, dp(14)));
                startStopBtn.setEnabled(true);
            }
        }

        state.setText(String.format(java.util.Locale.US,
                "%s · %s · M%d\n%s",
                active ? "LIVE" : "IDLE", phase, slot, g.summary()));
        state.setTextColor(active ? 0xff62ff91 : 0xffdbe7f4);
        StreamForegroundService.refresh(this);
    }

    private int activeSlot() { return Math.max(1, Math.min(4, prefs.getInt("ActiveSlot", 1))); }

    private Button btn(String s, View.OnClickListener l) { return btn(s, l, UiKit.PANEL_3); }
    private Button btn(String s, View.OnClickListener l, int color) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false); b.setTextSize(10); b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD); b.setPadding(0, 0, 0, 0);
        b.setMinHeight(0); b.setMinimumHeight(0);
        b.setBackground(UiKit.neonButton(color, UiKit.CYAN, dp(12)));
        b.setOnClickListener(l);
        return b;
    }

    private TextView tv(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(Color.WHITE);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER);
        return l;
    }

    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(34), 1);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        return lp;
    }

    private LinearLayout.LayoutParams fullW(int h) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, h);
        lp.setMargins(dp(2), dp(3), dp(2), dp(3));
        return lp;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); slog.i("float", s); }
}
