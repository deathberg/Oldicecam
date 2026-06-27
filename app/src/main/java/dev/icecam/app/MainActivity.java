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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_PICK = 7101;
    private static final boolean MAIN_AUTO_COMMIT = false;

    private AppLogger logger;
    private RootBootstrap root;
    private VliveBinderClient binder;
    private TransformController controller;
    private SharedPreferences prefs;
    private TransformState tx;

    private LinearLayout body;
    private RealtimePreviewView preview;
    private TextView statusScreen, rotationLabel, mediaLabel, versionLabel;
    private Button startRestoreButton, loopButton, fillButton;
    private LinearLayout slotsRow;
    private LinearLayout advancedPanel;
    private int pendingPickSlot = 1;
    private final HashMap<String, Bitmap> thumbCache = new HashMap<>();
    private String lastPreviewKey = "";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        logger = new AppLogger(this);
        root = new RootBootstrap(this, logger);
        binder = new VliveBinderClient(logger);
        controller = TransformController.get(this);
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);
        prefs.edit()
                .putString("ServerName", RootBootstrap.FIXED_SERVICE_NAME)
                .putBoolean("EnableTx24Color", false)
                .putInt("ActiveSlot", Math.max(1, Math.min(4, prefs.getInt("ActiveSlot", 1))))
                .apply();
        tx = TransformState.load(prefs);
        binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME);
        requestBasicPermissions();
        buildUi();
        logger.log("app", BuildInfo.BUILD_LABEL + " " + BuildInfo.VERSION_NAME + " started mainAutoCommit=" + MAIN_AUTO_COMMIT);
        controller.bus().store().addListener(state -> refreshAll());
        runBg(() -> { root.bootstrap(); binder.clearCache(); binder.setPreferredService(RootBootstrap.FIXED_SERVICE_NAME); refreshAll(); });
    }

    private void requestBasicPermissions() {
        if (Build.VERSION.SDK_INT < 23) return;
        ArrayList<String> ps = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.CAMERA);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.READ_MEDIA_VIDEO);
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ps.add(Manifest.permission.READ_EXTERNAL_STORAGE);
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

        versionLabel = text(BuildInfo.BUILD_LABEL + " · " + BuildInfo.BUILD_FLAVOR, 12, true, UiKit.MUTED);
        versionLabel.setGravity(Gravity.CENTER);
        body.addView(versionLabel, new LinearLayout.LayoutParams(-1, dp(22)));

        FrameLayout previewShell = new FrameLayout(this);
        previewShell.setBackground(UiKit.fill(0xff05070b, dp(10), 0x66303a4f));
        preview = new RealtimePreviewView(this);
        previewShell.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        statusScreen = text("", 11, true, 0xffdffaff);
        statusScreen.setPadding(dp(10), dp(7), dp(10), dp(7));
        statusScreen.setBackgroundColor(0xaa000000);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        previewShell.addView(statusScreen, sp);
        body.addView(previewShell, new LinearLayout.LayoutParams(-1, dp(245)));

        rotationLabel = text("", 18, false, UiKit.MUTED);
        rotationLabel.setPadding(0, dp(8), 0, dp(4));
        body.addView(rotationLabel);

        slotsRow = new LinearLayout(this);
        slotsRow.setOrientation(LinearLayout.HORIZONTAL);
        body.addView(slotsRow, new LinearLayout.LayoutParams(-1, dp(92)));

        LinearLayout row1 = row();
        fillButton = bigButton("FILL", v -> mutate("fit-fill"), UiKit.CYAN_DARK);
        row1.addView(fillButton, weight());
        row1.addView(bigButton("RESET\nVIEW", v -> mutate("reset"), UiKit.PANEL_3), weight());
        loopButton = bigButton("LOOP ON", v -> toggleLoop(), UiKit.CYAN_DARK);
        row1.addView(loopButton, weight());
        row1.addView(bigButton("•••", v -> showAdvanced(), UiKit.PANEL_3), weight());
        body.addView(row1);

        LinearLayout row2 = row();
        row2.addView(bigButton("ZOOM -", v -> mutate("zoom-"), UiKit.PANEL_3), weight());
        row2.addView(bigButton("ZOOM +", v -> mutate("zoom+"), UiKit.PANEL_3), weight());
        row2.addView(bigButton("ROT -90", v -> mutate("rot-90"), UiKit.PANEL_3), weight());
        row2.addView(bigButton("ROT +90", v -> mutate("rot+90"), UiKit.PANEL_3), weight());
        body.addView(row2);

        LinearLayout row3 = row();
        row3.addView(bigButton("↑", v -> mutate("up"), UiKit.PANEL_3), weight());
        row3.addView(bigButton("↓", v -> mutate("down"), UiKit.PANEL_3), weight());
        row3.addView(bigButton("←", v -> mutate("left"), UiKit.PANEL_3), weight());
        row3.addView(bigButton("→", v -> mutate("right"), UiKit.PANEL_3), weight());
        body.addView(row3);

        LinearLayout row4 = row();
        row4.addView(bigButton("MIRROR X", v -> mutate("mirror-x"), UiKit.PANEL_3), weight(1.15f));
        row4.addView(bigButton("MIRROR Y", v -> mutate("mirror-y"), UiKit.PANEL_3), weight(1.15f));
        row4.addView(bigButton("CENTER", v -> mutate("center"), UiKit.PANEL_3), weight(1.15f));
        body.addView(row4);

        body.addView(bigButton("▶  PLAY / COMMIT", v -> { controller.commit(TransformController.Source.MAIN, "play-commit"); refreshAll(); }, UiKit.PANEL_3), fullBtn());

        mediaLabel = text("", 12, false, UiKit.MUTED);
        mediaLabel.setGravity(Gravity.CENTER);
        mediaLabel.setPadding(0, dp(5), 0, dp(5));
        body.addView(mediaLabel);

        startRestoreButton = bigButton("● START", v -> startOrRestore(), UiKit.PANEL_3);
        body.addView(startRestoreButton, fullBtn());
        refreshAll();
    }

    private void renderSlots() {
        if (slotsRow == null) return;
        slotsRow.removeAllViews();
        int active = activeSlot();
        for (int i = 1; i <= 4; i++) {
            final int slot = i;
            FrameLayout frame = new FrameLayout(this);
            frame.setBackground(UiKit.neonButton(i == active ? UiKit.CYAN_DARK : UiKit.PANEL_2, UiKit.CYAN, dp(18)));
            frame.setSelected(i == active);
            frame.setOnClickListener(v -> selectSlot(slot));
            frame.setOnLongClickListener(v -> { pickIntoSlot(slot); return true; });

            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(3), dp(3), dp(3), dp(3));
            String p = prefs.getString(slotKey(i), "");

            ImageView thumb = new ImageView(this);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bm = makeThumbnail(p, dp(72), dp(44));
            if (bm != null) thumb.setImageBitmap(bm);
            else thumb.setBackgroundColor(0xff090d15);
            cell.addView(thumb, new LinearLayout.LayoutParams(-1, 0, 1));

            TextView label = text("M" + i + (p == null || p.length() == 0 ? " +" : ""), 12, true, UiKit.TEXT);
            label.setGravity(Gravity.CENTER);
            cell.addView(label, new LinearLayout.LayoutParams(-1, dp(28)));
            frame.addView(cell, new FrameLayout.LayoutParams(-1, -1));

            TextView plus = text("+", 22, true, Color.WHITE);
            plus.setGravity(Gravity.CENTER);
            plus.setBackground(UiKit.neonButton(UiKit.CYAN_DARK, UiKit.CYAN, dp(23)));
            plus.setOnClickListener(v -> pickIntoSlot(slot));
            FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(36), dp(36), Gravity.TOP | Gravity.RIGHT);
            pp.setMargins(0, dp(5), dp(5), 0);
            frame.addView(plus, pp);

            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -1, 1);
            cp.setMargins(dp(4), dp(4), dp(4), dp(4));
            slotsRow.addView(frame, cp);
        }
    }

    private Bitmap makeThumbnail(String p, int w, int h) {
        try {
            if (p == null || p.length() == 0) return null;
            String key = p + "@" + w + "x" + h;
            Bitmap cached = thumbCache.get(key);
            if (cached != null && !cached.isRecycled()) return cached;
            Bitmap bm = null;
            if (MediaTransformer.isImagePath(p)) bm = MediaTransformer.renderPreview(this, p, new TransformState(), w, h);
            if (bm != null) thumbCache.put(key, bm);
            return bm;
        } catch (Throwable ignored) { return null; }
    }

    private int activeSlot() { return Math.max(1, Math.min(4, prefs.getInt("ActiveSlot", 1))); }
    private String slotKey(int slot) { return "Slot" + slot + "Path"; }

    private void pickIntoSlot(int slot) {
        pendingPickSlot = Math.max(1, Math.min(4, slot));
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "image/*"});
        startActivityForResult(i, REQ_PICK);
    }

    private void selectSlot(int slot) {
        String path = prefs.getString(slotKey(slot), "");
        if (path == null || path.length() == 0) { pickIntoSlot(slot); return; }
        controller.selectMedia(TransformController.Source.MAIN, slot, path);
        logger.log("media", "active slot M" + slot + " path=" + path);
        lastPreviewKey = "";
        refreshAll();
        if (prefs.getBoolean("ReplacementActive", false)) controller.startReplacement(TransformController.Source.MAIN);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Throwable ignored) {}
            String path = MediaResolver.resolveToReadableFile(this, uri, logger);
            int slot = Math.max(1, Math.min(4, pendingPickSlot));
            prefs.edit().putString(slotKey(slot), path).putInt("PlayFileType", MediaTransformer.isVideoPath(path) ? 2 : 1).apply();
            controller.selectMedia(TransformController.Source.MAIN, slot, path);
            logger.log("media", "selected slot M" + slot + " path=" + path);
            lastPreviewKey = "";
            thumbCache.clear();
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
        logger.log("ui", "loop=" + n);
        refreshAll();
    }

    private void startOrRestore() {
        if (controller.isBusy()) { toast("Busy"); return; }
        if (prefs.getBoolean("ReplacementActive", false)) controller.restoreCamera(TransformController.Source.MAIN);
        else controller.startReplacement(TransformController.Source.MAIN);
        refreshAll();
    }

    private void showAdvanced() {
        if (advancedPanel != null) {
            advancedPanel.setVisibility(advancedPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return;
        }
        advancedPanel = new LinearLayout(this);
        advancedPanel.setOrientation(LinearLayout.VERTICAL);
        advancedPanel.setPadding(dp(8), dp(7), dp(8), dp(7));
        advancedPanel.setBackground(UiKit.fill(0xff111827, dp(18), 0x557987a0));
        advancedPanel.addView(text("Advanced", 12, true, UiKit.TEXT));
        LinearLayout r = row();
        r.addView(bigButton("FLOAT", v -> startFloatPanel(), UiKit.PANEL_3), weight());
        r.addView(bigButton("LOG", v -> shareLog(), UiKit.PANEL_3), weight());
        r.addView(bigButton("RE", v -> startActivity(new android.content.Intent(this, dev.icecam.app.recapture.ReCaptureActivity.class)), UiKit.CYAN_DARK), weight());
        advancedPanel.addView(r);
        LinearLayout r2 = row();
        r2.addView(bigButton("OVERLAY", v -> openOverlaySettings(), UiKit.PANEL_3), weight());
        advancedPanel.addView(r2);
        int index = body.indexOfChild(mediaLabel);
        if (index < 0) index = body.getChildCount();
        body.addView(advancedPanel, index, new LinearLayout.LayoutParams(-1, -2));
    }

    private void startFloatPanel() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            openOverlaySettings();
            toast("Allow Display over other apps, then open floating controls again");
            return;
        }
        try { startService(new Intent(this, FloatService.class)); logger.log("ui", "floating controls requested"); }
        catch (Throwable t) { logger.log("ui", "start float failed: " + t); }
    }

    private void openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= 23) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
    }

    private void refreshAll() {
        runOnUiThread(() -> {
            tx = TransformState.load(prefs);
            boolean active = prefs.getBoolean("ReplacementActive", false);
            String phase = prefs.getString("IceCamState", "IDLE");
            boolean connected = active || phase.contains("READY") || phase.contains("ACTIVE");
            boolean busy = controller.isBusy();

            String p = prefs.getString("OriginalPlayFileMp4", prefs.getString("PlayFileMp4", ""));
            String previewKey = p;
            if (!previewKey.equals(lastPreviewKey)) {
                lastPreviewKey = previewKey;
                preview.setMediaPath(p);
            }
            preview.setTransformState(tx);

            String backend = connected ? "READY" : (active ? "RUNNING / IPC?" : "OFF");
            String transform = phase;
            statusScreen.setText(String.format(Locale.US,
                    "Backend: %s  ·  Replacement: %s  ·  Transform: %s\nSource: M%d  ·  %s  ·  %s",
                    backend, active ? "ACTIVE" : "OFF", transform,
                    activeSlot(), tx.modeName(), tx.summary()));
            statusScreen.setTextColor(active ? 0xffa7ffd2 : (connected ? UiKit.WARN : 0xffffb0b0));
            rotationLabel.setText("Rotation: " + rotationLabelValue() + "°");
            fillButton.setText(tx.mode == TransformState.MODE_FILL ? "FILL" : "FIT");
            fillButton.setSelected(tx.mode == TransformState.MODE_FILL || tx.mode == TransformState.MODE_FIT);
            loopButton.setText(prefs.getBoolean("PlayisLoop", true) ? "LOOP ON" : "LOOP OFF");
            startRestoreButton.setText(busy ? "● BUSY…" : (active ? "● RESTORE CAMERA" : "● START STREAM"));
            startRestoreButton.setEnabled(!busy);
            startRestoreButton.setSelected(active);
            String play = prefs.getString("PlayFileMp4", "");
            mediaLabel.setText((play == null || play.length() == 0 ? "No active media" : shortName(play)) + "   ·   realtime preview, press PLAY/COMMIT to apply   ·   " + BuildInfo.VERSION_NAME + " · marker #" + prefs.getLong("LastMarkerId", 0L));
            renderSlots();
        });
    }

    private int rotationLabelValue() {
        int deg = tx.rotationQuadrant() * 90;
        if (deg == 270) return -90;
        return deg;
    }

    private void shareLog() {
        try {
            logger.logDivider("diag", "export requested " + BuildInfo.VERSION_NAME);
            String diag = DiagnosticDumper.build(this, logger, binder);
            logger.log("diag", "snapshot built chars=" + diag.length());
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, diag + "\n\n--- runtime-log-ring ---\n" + logger.text());
            startActivity(Intent.createChooser(i, "Export IceCam diagnostics"));
        } catch (Throwable t) { toast("Export failed: " + t.getMessage()); }
    }

    private String shortName(String p) {
        if (p == null) return "Empty";
        int slash = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        String n = slash >= 0 ? p.substring(slash + 1) : p;
        return n.length() > 44 ? n.substring(0, 41) + "…" : n;
    }

    private void runBg(Runnable r) { new Thread(() -> { try { r.run(); } catch (Throwable t) { logger.log("thread", String.valueOf(t)); } }, "icecam-bg").start(); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); logger.log("toast", s); }

    private TextView text(String s, int sp, boolean bold, int color) { TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT_BOLD); return t; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER); return l; }
    private LinearLayout.LayoutParams weight() { return weight(1f); }
    private LinearLayout.LayoutParams weight(float w) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(50), w); p.setMargins(dp(4), dp(4), dp(4), dp(4)); return p; }
    private LinearLayout.LayoutParams fullBtn() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56)); p.setMargins(dp(4), dp(7), dp(4), dp(4)); return p; }
    private Button bigButton(String s, View.OnClickListener l, int color) { Button b = new Button(this); b.setText(s); b.setAllCaps(false); b.setTextSize(12); b.setTextColor(Color.WHITE); b.setTypeface(Typeface.DEFAULT); b.setPadding(0, 0, 0, 0); b.setMinHeight(0); b.setMinimumHeight(0); b.setBackground(UiKit.neonButton(color, UiKit.CYAN, dp(14))); b.setOnClickListener(l); return b; }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
}
