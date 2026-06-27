package com.xiaomi.vlive;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.vlive.util.VliveBridge;

/**
 * Single-activity control panel with three tabs (Home / Controller / Settings).
 *
 * Framework-only (no AndroidX): tabs swap inflated content views in a FrameLayout,
 * matching the original's Home/Controller/Settings fragments behaviorally.
 */
public class MainActivity extends Activity {

    // presets shown in Settings + their default (begin,end) µs.
    private static final int[] PRESETS = {1, 2, 3, 4, 5, 6, 8};
    private static final String[] PRESET_LABELS = {"眼", "↑", "嘴", "←", "正", "→", "↓"};
    private static final long[][] DEFAULTS = {
            {0L, 1_170_000L}, {5_000_000L, 5_900_000L}, {2_000_000L, 3_200_000L},
            {3_200_000L, 4_000_000L}, {4_000_000L, 4_000_000L}, {4_000_000L, 5_000_000L},
            {5_600_000L, 6_800_000L},
    };

    private FrameLayout content;
    private View homeView, controllerView, settingsView;
    private final EditText[] beginEdits = new EditText[PRESETS.length];
    private final EditText[] endEdits = new EditText[PRESETS.length];
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();
        setContentView(R.layout.activity_main);
        content = findViewById(R.id.content);

        findViewById(R.id.tab_home).setOnClickListener(v -> showHome());
        findViewById(R.id.tab_controller).setOnClickListener(v -> showController());
        findViewById(R.id.tab_settings).setOnClickListener(v -> showSettings());

        showHome();
    }

    private void swap(View view) {
        content.removeAllViews();
        content.addView(view);
    }

    // ─── Home ───────────────────────────────────────────────────────────────

    private void showHome() {
        if (homeView == null) {
            homeView = LayoutInflater.from(this).inflate(R.layout.fragment_home, content, false);
        }
        swap(homeView);
    }

    // ─── Controller ──────────────────────────────────────────────────────────

    private void showController() {
        if (controllerView == null) {
            controllerView = LayoutInflater.from(this).inflate(R.layout.fragment_controller, content, false);
            TextView status = controllerView.findViewById(R.id.status_text);

            wireSelect(controllerView, R.id.btn_video1, "/sdcard/Movies/1.mp4", status);
            wireSelect(controllerView, R.id.btn_video2, "/sdcard/Movies/2.mp4", status);
            wireSelect(controllerView, R.id.btn_video3, "/sdcard/Movies/3.mp4", status);

            controllerView.findViewById(R.id.btn_play).setOnClickListener(v -> {
                boolean playing = VliveBridge.isPlaying();
                status.setText(playing ? "状态: 播放中" : "状态: 未连接 / 未播放");
                if (!playing) VliveBridge.toast("播放失败");
            });
            controllerView.findViewById(R.id.btn_loop).setOnClickListener(v -> {
                boolean loop = !app.isLoop();
                app.setLoop(loop);
                VliveBridge.setLoop(loop);
                VliveBridge.toast("循环: " + loop);
            });
            controllerView.findViewById(R.id.btn_mirror).setOnClickListener(v -> {
                boolean mirror = !app.isMirror();
                app.setMirror(mirror);
                VliveBridge.setMirror(mirror);
                VliveBridge.toast("镜像: " + mirror);
            });
            controllerView.findViewById(R.id.btn_rotate).setOnClickListener(v -> {
                int angle = app.getAngle() + 90;
                if (angle > 360) angle = 0;
                app.setAngle(angle);
                VliveBridge.setAngle(angle);
                status.setText("状态: 旋转 " + angle + "°");
            });
            controllerView.findViewById(R.id.btn_overlay).setOnClickListener(v -> startOverlayPanel());
        }
        swap(controllerView);
    }

    private void wireSelect(View root, int id, String path, TextView status) {
        root.findViewById(id).setOnClickListener(v -> {
            if (!new java.io.File(path).exists()) {
                VliveBridge.toast(path + " 不存在");
                status.setText("状态: 文件不存在 " + path);
            } else {
                boolean ok = VliveBridge.selectVideo(path, 1);
                status.setText(ok ? "状态: 已选择 " + path : "状态: 选择失败");
            }
        });
    }

    private void startOverlayPanel() {
        if (!Settings.canDrawOverlays(this)) {
            VliveBridge.toast("需要悬浮窗权限");
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            return;
        }
        startForegroundService(new Intent(this, FloatService.class));
        VliveBridge.toast("悬浮面板已开启");
    }

    // ─── Settings ──────────────────────────────────────────────────────────

    private void showSettings() {
        if (settingsView == null) {
            settingsView = LayoutInflater.from(this).inflate(R.layout.fragment_settings, content, false);
            LinearLayout container = settingsView.findViewById(R.id.ranges_container);
            int saveIndex = container.indexOfChild(settingsView.findViewById(R.id.btn_save));

            for (int i = 0; i < PRESETS.length; i++) {
                int preset = PRESETS[i];
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);

                TextView label = new TextView(this);
                label.setText("[" + preset + "] " + PRESET_LABELS[i]);
                label.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                EditText begin = numberField(app.rangeBegin(preset, DEFAULTS[i][0]));
                EditText end = numberField(app.rangeEnd(preset, DEFAULTS[i][1]));
                beginEdits[i] = begin;
                endEdits[i] = end;

                row.addView(label);
                row.addView(begin);
                row.addView(end);
                container.addView(row, saveIndex + i);
            }

            settingsView.findViewById(R.id.btn_save).setOnClickListener(v -> saveRanges());
        }
        swap(settingsView);
    }

    private EditText numberField(long value) {
        EditText e = new EditText(this);
        e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        e.setText(String.valueOf(value));
        e.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));
        return e;
    }

    private void saveRanges() {
        try {
            long[] begins = new long[PRESETS.length];
            long[] ends = new long[PRESETS.length];
            for (int i = 0; i < PRESETS.length; i++) {
                begins[i] = Long.parseLong(beginEdits[i].getText().toString().trim());
                ends[i] = Long.parseLong(endEdits[i].getText().toString().trim());
                if (ends[i] < begins[i]) {
                    VliveBridge.toast("保存失败\n结束时间不能小于开始时间");
                    return;
                }
            }
            for (int i = 0; i < PRESETS.length; i++) {
                app.setRangeBegin(PRESETS[i], begins[i]);
                app.setRangeEnd(PRESETS[i], ends[i]);
            }
            VliveBridge.toast("保存成功");
        } catch (NumberFormatException e) {
            VliveBridge.toast("保存失败,输入数据有误");
        }
    }
}
