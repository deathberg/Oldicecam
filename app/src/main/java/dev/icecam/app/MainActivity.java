package dev.icecam.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import dev.icecam.app.runtime.AppState;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_PICK = 7101;
    private static final boolean MAIN_AUTO_COMMIT = false;

    private SmartLogger slog;
    private RootBootstrap root;
    private VliveBinderClient binder;
    private TransformController controller;
    private SharedPreferences prefs;
    private TransformState tx;

    private LinearLayout body;
    private GpuMediaPreviewView mainPreview;
    private SlotPreviewGrid slotGrid;
    private TextView statusScreen, rotationLabel, mediaLabel, versionLabel, fpsLabel;
    private Button startRestoreButton, loopButton, fillButton;
    private LinearLayout advancedPanel;
    private int pendingPickSlot = 1;
    private String lastPreviewKey = "";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        slog = SmartLogger.get(this);
        root = new RootBootstrap(this, slog.base());
        binder = new VliveBinderClient(slog.base());
        controller = TransformController.get(this);
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);
        prefs.edit()
                .putString("ServerName", RootBootstrap.FIXED_SERVICE_NAME)
                .putBoolean("EnableTx24Color", false)
                .putBoolean("DebugLogging", true)
                .putFloat("ColorCorrectStrength", 0.35f)
                .putInt("ActiveSlot", Math.max(1, Math.min(4, prefs.getInt("ActiveSlot", 1))))
                .apply();
        tx = TransformState.load(prefs);
        binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
        requestBasicPermissions();
        buildUi();
        slog.i("app", BuildInfo.BUILD_LABEL + " started");
        controller.bus().store().addListener(state -> refreshAll());
        runBg(() -> {
            root.bootstrap();
            binder.clearCache();
            binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
            refreshAll();
        });
    }

    @Override protected void onDestroy() {
        if (slotGrid != null) slotGrid.releaseAll();
        if (mainPreview != null) mainPreview.release();
        super.onDestroy();
    }

    private void requestBasicPermissions() {
        if (Build.VERSION.SDK_INT < 23) return;
        ArrayList<String> ps = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.CAMERA);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.READ_MEDIA_VIDEO);
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ps.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!ps.isEmpty()) requestPermissions(ps.toArray(new String[0]), 7);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(10), dp(8), dp(10), dp(16));
        body.setBackgroundColor(UiKit.BG);
        scroll.addView(body);
        setContentView(scroll);

        versionLabel = text(BuildInfo.BUILD_LABEL + " · " + BuildInfo.VERSION_NAME, 12, true, UiKit.MUTED);
        versionLabel.setGravity(Gravity.CENTER);
        body.addView(versionLabel, new LinearLayout.LayoutParams(-1, dp(22)));

        // Main GPU preview (active slot)
        FrameLayout previewShell = new FrameLayout(this);
        previewShell.setBackground(UiKit.fill(0xff05070b, dp(10), 0x66303a4f));
        mainPreview = new GpuMediaPreviewView(this);
        mainPreview.setSlotLabel("LIVE");
        previewShell.addView(mainPreview, new FrameLayout.LayoutParams(-1, -1));
        statusScreen = text("", 11, true, 0xffdffaff);
        statusScreen.setPadding(dp(10), dp(7), dp(10), dp(7));
        statusScreen.setBackgroundColor(0xaa000000);
        previewShell.addView(statusScreen, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));
        body.addView(previewShell, new LinearLayout.LayoutParams(-1, dp(220)));

        fpsLabel = text("preview: — fps", 10, false, UiKit.MUTED);
        fpsLabel.setGravity(Gravity.CENTER);
        body.addView(fpsLabel);

        // 4-slot live grid
        slotGrid = new SlotPreviewGrid(this);
        slotGrid.setListener(new SlotPreviewGrid.SlotListener() {
            @Override public void onSlotClick(int slot) { selectSlot(slot); }
            @Override public void onSlotLongPress(int slot) { pickIntoSlot(slot); }
        });
        body.addView(slotGrid, new LinearLayout.LayoutParams(-1, dp(200)));

        rotationLabel = text("", 16, false, UiKit.MUTED);
        rotationLabel.setPadding(0, dp(6), 0, dp(4));
        body.addView(rotationLabel);

        LinearLayout row1 = row();
        fillButton = bigButton("FILL", v -> mutate("fit-fill"), UiKit.CYAN_DARK);
        row1.addView(fillButton, weight());
        row1.addView(bigButton("RESET", v -> mutate("reset"), UiKit.PANEL_3), weight());
        loopButton = bigButton("LOOP ON", v -> toggleLoop(), UiKit.CYAN_DARK);
        row1.addView(loopButton, weight());
        row1.addView(bigButton("FLOAT", v -> startFloatPanel(), UiKit.PANEL_3), weight());
        body.addView(row1);

        LinearLayout row2 = row();
        row2.addView(bigButton("Z−", v -> mutate("zoom-"), UiKit.PANEL_3), weight());
        row2.addView(bigButton("Z+", v -> mutate("zoom+"), UiKit.PANEL_3), weight());
        row2.addView(bigButton("↻90", v -> mutate("rot+90"), UiKit.PANEL_3), weight());
        row2.addView(bigButton("⇄", v -> mutate("mirror-x"), UiKit.PANEL_3), weight());
        body.addView(row2);

        LinearLayout row3 = row();
        row3.addView(bigButton("▲", v -> mutate("up"), UiKit.PANEL_3), weight());
        row3.addView(bigButton("●", v -> mutate("center"), UiKit.PANEL_3), weight());
        row3.addView(bigButton("▼", v -> mutate("down"), UiKit.PANEL_3), weight());
        row3.addView(bigButton("⇅", v -> mutate("mirror-y"), UiKit.PANEL_3), weight());
        body.addView(row3);

        LinearLayout row4 = row();
        row4.addView(bigButton("◀", v -> mutate("left"), UiKit.PANEL_3), weight());
        row4.addView(bigButton("▶", v -> mutate("right"), UiKit.PANEL_3), weight());
        row4.addView(bigButton("APPLY", v -> {
            controller.commit(TransformController.Source.MAIN, "main-apply");
            refreshAll();
        }, UiKit.CYAN_DARK), weight(1.2f));
        body.addView(row4);

        mediaLabel = text("", 11, false, UiKit.MUTED);
        mediaLabel.setGravity(Gravity.CENTER);
        mediaLabel.setPadding(0, dp(4), 0, dp(4));
        body.addView(mediaLabel);

        startRestoreButton = bigButton("● START STREAM", v -> startOrRestore(), UiKit.GREEN);
        body.addView(startRestoreButton, fullBtn());

        LinearLayout rowAdv = row();
        rowAdv.addView(bigButton("LOG", v -> shareLog(), UiKit.PANEL_3), weight());
        rowAdv.addView(bigButton("DEBUG", v -> toggleDebug(), UiKit.PANEL_3), weight());
        body.addView(rowAdv);

        refreshAll();
    }

    private void toggleDebug() {
        boolean on = !slog.debugEnabled();
        slog.setDebugEnabled(on);
        toast("Debug log " + (on ? "ON" : "OFF"));
    }

    private int activeSlot() { return Math.max(1, Math.min(4, prefs.getInt("ActiveSlot", 1))); }
    private String slotKey(int slot) { return "Slot" + slot + "Path"; }

    private void pickIntoSlot(int slot) {
        pendingPickSlot = Math.max(1, Math.min(4, slot));
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, MediaPreviewEngine.PICK_MIME_TYPES);
        startActivityForResult(i, REQ_PICK);
    }

    private void selectSlot(int slot) {
        String path = prefs.getString(slotKey(slot), "");
        if (path == null || path.isEmpty()) { pickIntoSlot(slot); return; }
        controller.selectMedia(TransformController.Source.MAIN, slot, path);
        slog.event("media", "select", "M" + slot + " " + shortName(path));
        lastPreviewKey = "";
        refreshAll();
        if (prefs.getBoolean("ReplacementActive", false)) controller.startReplacement(TransformController.Source.MAIN);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Throwable ignored) {}
            String path = MediaResolver.resolveToReadableFile(this, uri, slog.base());
            int slot = Math.max(1, Math.min(4, pendingPickSlot));
            prefs.edit()
                    .putString(slotKey(slot), path)
                    .putInt("PlayFileType", MediaPreviewEngine.isVideoPath(path) ? 2 : 1)
                    .apply();
            controller.selectMedia(TransformController.Source.MAIN, slot, path);
            slog.event("media", "picked", "M" + slot + " " + MediaPreviewEngine.mediaKind(path));
            lastPreviewKey = "";
            refreshAll();
            if (prefs.getBoolean("ReplacementActive", false)) controller.startReplacement(TransformController.Source.MAIN);
        }
    }

    private void mutate(String op) {
        controller.mutate(TransformController.Source.MAIN, op, MAIN_AUTO_COMMIT);
        refreshAll();
    }

    private void toggleLoop() {
        boolean n = !prefs.getBoolean("PlayisLoop", true);
        controller.setLoop(TransformController.Source.MAIN, n);
        refreshAll();
    }

    private void startOrRestore() {
        if (controller.isBusy()) { toast("Занято…"); return; }
        if (prefs.getBoolean("ReplacementActive", false)) controller.restoreCamera(TransformController.Source.MAIN);
        else controller.startReplacement(TransformController.Source.MAIN);
        refreshAll();
    }

    private void startFloatPanel() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            toast("Разрешите overlay, затем снова FLOAT");
            return;
        }
        try {
            startService(new Intent(this, FloatService.class));
            slog.i("ui", "float panel started");
        } catch (Throwable t) { slog.w("ui", "float failed: " + t.getMessage()); }
    }

    private void refreshAll() {
        runOnUiThread(() -> {
            AppState app = controller.state();
            tx = app.transform;
            boolean active = prefs.getBoolean("ReplacementActive", false);
            String phase = prefs.getString("IceCamState", "IDLE");
            boolean busy = controller.isBusy();

            String p = prefs.getString("OriginalPlayFileMp4", prefs.getString("PlayFileMp4", ""));
            String previewKey = p + "@" + tx.summary();
            if (!previewKey.equals(lastPreviewKey)) {
                lastPreviewKey = previewKey;
                mainPreview.setMediaPath(p);
            }
            mainPreview.setTransformState(tx);
            mainPreview.setHighlighted(true);
            slotGrid.bind(prefs, app);

            String kind = MediaPreviewEngine.mediaKind(p);
            statusScreen.setText(String.format(Locale.US,
                    "%s · %s · M%d · %s · z=%.2f\n%s · %s",
                    active ? "● LIVE" : "○ IDLE", phase, activeSlot(), kind, tx.zoomX,
                    tx.modeName(), tx.summary()));
            statusScreen.setTextColor(active ? 0xffa7ffd2 : 0xffffd0a0);

            rotationLabel.setText(String.format(Locale.US, "Rotation %d° · Mirror %s%s · %s",
                    rotationLabelValue(), tx.mirrorH() ? "X" : "", tx.mirrorV() ? "Y" : "",
                    BuildInfo.NATIVE_STACK_NOTE));

            fillButton.setText(tx.mode == TransformState.MODE_FILL ? "FILL" : "FIT");
            loopButton.setText(prefs.getBoolean("PlayisLoop", true) ? "LOOP ON" : "LOOP OFF");

            if (busy) {
                startRestoreButton.setText("● BUSY…");
                startRestoreButton.setBackground(UiKit.neonButton(UiKit.PANEL_3, UiKit.WARN, dp(14)));
                startRestoreButton.setEnabled(false);
            } else if (active) {
                startRestoreButton.setText("■ STOP STREAM");
                startRestoreButton.setBackground(UiKit.neonButton(UiKit.RED, 0xffff8090, dp(14)));
                startRestoreButton.setEnabled(true);
            } else {
                startRestoreButton.setText("● START STREAM");
                startRestoreButton.setBackground(UiKit.neonButton(UiKit.GREEN, 0xff5cffaa, dp(14)));
                startRestoreButton.setEnabled(true);
            }

            String play = prefs.getString("PlayFileMp4", "");
            mediaLabel.setText((play == null || play.isEmpty() ? "Нет медиа — нажмите M1–M4" : shortName(play))
                    + " · PNG/JPEG/HEIC/MP4/MOV · GPU preview");
            fpsLabel.setText(slog.debugEnabled() ? "debug log ON · smart logger active" : "debug log OFF");
        });
    }

    private int rotationLabelValue() {
        int deg = tx.rotationQuadrant() * 90;
        return deg == 270 ? -90 : deg;
    }

    private void shareLog() {
        try {
            slog.base().logDivider("diag", "export " + BuildInfo.VERSION_NAME);
            String diag = DiagnosticDumper.build(this, slog.base(), binder);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, diag + "\n\n--- log ---\n" + slog.base().text());
            startActivity(Intent.createChooser(i, "Export diagnostics"));
        } catch (Throwable t) { toast("Export failed"); }
    }

    private String shortName(String p) {
        if (p == null) return "Empty";
        int slash = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        String n = slash >= 0 ? p.substring(slash + 1) : p;
        return n.length() > 40 ? n.substring(0, 37) + "…" : n;
    }

    private void runBg(Runnable r) {
        new Thread(() -> { try { r.run(); } catch (Throwable t) { slog.e("thread", String.valueOf(t)); } }, "icecam-bg").start();
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); slog.i("toast", s); }

    private TextView text(String s, int sp, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER); return l; }
    private LinearLayout.LayoutParams weight() { return weight(1f); }
    private LinearLayout.LayoutParams weight(float w) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(48), w); p.setMargins(dp(3), dp(3), dp(3), dp(3)); return p; }
    private LinearLayout.LayoutParams fullBtn() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54)); p.setMargins(dp(3), dp(6), dp(3), dp(4)); return p; }
    private Button bigButton(String s, View.OnClickListener l, int color) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false); b.setTextSize(11); b.setTextColor(Color.WHITE);
        b.setBackground(UiKit.neonButton(color, UiKit.CYAN, dp(14))); b.setOnClickListener(l); return b;
    }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
}
