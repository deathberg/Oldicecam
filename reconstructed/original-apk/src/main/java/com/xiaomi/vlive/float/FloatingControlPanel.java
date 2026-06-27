package com.xiaomi.vlive.float;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.xiaomi.vlive.App;
import com.xiaomi.vlive.R;
import com.xiaomi.vlive.config.ActionRanges;
import com.xiaomi.vlive.config.AppConfigKeys;
import com.xiaomi.vlive.util.VliveBridge;

import java.io.File;

/**
 * Reconstructed from {@code p059d1.C1390e} + {@code App.m1774h}.
 * TYPE_APPLICATION_OVERLAY (2038) floating grid.
 */
public final class FloatingControlPanel {
    private final App app;
    private WindowManager windowManager;
    private View root;
    private WindowManager.LayoutParams params;
    private GridLayout buttonGroup;
    private boolean expanded;

    public FloatingControlPanel(App app) {
        this.app = app;
    }

    public void showIfNeeded() {
        if (root != null) return;
        Context ctx = app.getApplicationContext();
        windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        root = LayoutInflater.from(ctx).inflate(R.layout.float_layout, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 200;
        params.y = 500;

        ImageView mainButton = root.findViewById(R.id.main_button);
        buttonGroup = root.findViewById(R.id.button_group);
        mainButton.setImageDrawable(ctx.getApplicationInfo().loadIcon(ctx.getPackageManager()));
        mainButton.setOnClickListener(v -> {
            expanded = !expanded;
            buttonGroup.setVisibility(expanded ? View.VISIBLE : View.GONE);
        });

        wireActionButton(R.id.butonf1, ActionRanges.ACTION_EYE);
        wireActionButton(R.id.butonf2, ActionRanges.ACTION_HEAD_UP);
        wireActionButton(R.id.butonf3, ActionRanges.ACTION_MOUTH);
        wireActionButton(R.id.butonf4, ActionRanges.ACTION_TURN_LEFT);
        wireActionButton(R.id.butonf5, ActionRanges.ACTION_CENTER);
        wireActionButton(R.id.butonf6, ActionRanges.ACTION_TURN_RIGHT);
        wireActionButton(R.id.butonf8, ActionRanges.ACTION_NOD);

        root.findViewById(R.id.butonf7).setOnClickListener(v -> {
            if (!VliveBridge.isPlaying()) {
                // toast 播放失败
            }
        });

        root.findViewById(R.id.butonf9).setOnClickListener(v -> {
            app.setLoopEnabled(!app.isLoopEnabled());
            VliveBridge.setLoop(app.isLoopEnabled());
        });

        root.findViewById(R.id.butonf10).setOnClickListener(v -> {
            int angle = app.getPrefs().getInt(AppConfigKeys.PLAY_ANGLE, 0) + 90;
            if (angle > 360) angle = 0;
            app.getPrefs().edit().putInt(AppConfigKeys.PLAY_ANGLE, angle).apply();
            VliveBridge.setAngle(angle);
        });

        root.findViewById(R.id.butonf11).setOnClickListener(v -> {
            boolean mirror = !app.getPrefs().getBoolean(AppConfigKeys.PLAY_MIRROR, false);
            app.getPrefs().edit().putBoolean(AppConfigKeys.PLAY_MIRROR, mirror).apply();
            VliveBridge.setMirror(mirror);
        });

        root.findViewById(R.id.butonf12).setOnClickListener(v -> dismiss());

        root.findViewById(R.id.butonf31).setOnClickListener(v -> playSlot("/sdcard/Movies/1.mp4"));
        root.findViewById(R.id.butonf32).setOnClickListener(v -> playSlot("/sdcard/Movies/2.mp4"));
        root.findViewById(R.id.butonf33).setOnClickListener(v -> playSlot("/sdcard/Movies/3.mp4"));
        root.findViewById(R.id.butonf34).setOnClickListener(v -> VliveBridge.hardRecovery());

        mainButton.setOnTouchListener(new DragTouchListener());
        windowManager.addView(root, params);
    }

    private void wireActionButton(int id, int actionId) {
        root.findViewById(id).setOnClickListener(v -> app.triggerActionSeek(actionId));
    }

    private void playSlot(String path) {
        if (!new File(path).exists()) {
            // toast path + " 不存在"
            return;
        }
        VliveBridge.setSource(path, 1);
    }

    public void dismiss() {
        if (root != null && windowManager != null) {
            windowManager.removeViewImmediate(root);
            root = null;
        }
    }

    private final class DragTouchListener implements View.OnTouchListener {
        private int lastX, lastY;
        private float downX, downY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = e.getRawX();
                    downY = e.getRawY();
                    lastX = params.x;
                    lastY = params.y;
                    return false;
                case MotionEvent.ACTION_MOVE:
                    params.x = lastX + (int) (e.getRawX() - downX);
                    params.y = lastY + (int) (e.getRawY() - downY);
                    windowManager.updateViewLayout(root, params);
                    return true;
                default:
                    return false;
            }
        }
    }
}
