package com.xiaomi.vlive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.xiaomi.vlive.config.ActionRanges;
import com.xiaomi.vlive.config.AppConfigKeys;
import com.xiaomi.vlive.util.VliveBridge;

/**
 * Reconstructed from {@code com.xiaomi.vlive.FloatService}.
 * Duplicate of in-app float panel but started as Service when user toggles overlay.
 */
public class FloatService extends Service {
    private WindowManager windowManager;
    private View overlay;
    private WindowManager.LayoutParams params;
    private GridLayout buttonGroup;
    private App app;
    private boolean expanded;

    @Override
    public void onCreate() {
        super.onCreate();
        app = (App) getApplication();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlay = LayoutInflater.from(this).inflate(R.layout.float_layout, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        params.x = 200;
        params.y = 500;

        ImageView main = overlay.findViewById(R.id.main_button);
        buttonGroup = overlay.findViewById(R.id.button_group);
        main.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));
        main.setOnClickListener(v -> {
            expanded = !expanded;
            buttonGroup.setVisibility(expanded ? View.VISIBLE : View.GONE);
        });

        overlay.findViewById(R.id.butonf5).setOnClickListener(v ->
                VliveBridge.seekRange(
                        app.getActionRangeBegin(ActionRanges.ACTION_CENTER, ActionRanges.defaultBegin(5)),
                        app.getActionRangeEnd(ActionRanges.ACTION_CENTER, ActionRanges.defaultEnd(5))));

        overlay.findViewById(R.id.butonf9).setOnClickListener(v -> {
            app.setLoopEnabled(!app.isLoopEnabled());
            VliveBridge.setLoop(app.isLoopEnabled());
        });

        overlay.findViewById(R.id.butonf10).setOnClickListener(v -> {
            int angle = app.getPrefs().getInt(AppConfigKeys.PLAY_ANGLE, 0) + 90;
            if (angle > 360) angle = 0;
            app.getPrefs().edit().putInt(AppConfigKeys.PLAY_ANGLE, angle).apply();
            VliveBridge.setAngle(angle);
        });

        overlay.findViewById(R.id.butonf11).setOnClickListener(v -> {
            boolean mirror = !app.getPrefs().getBoolean(AppConfigKeys.PLAY_MIRROR, false);
            app.getPrefs().edit().putBoolean(AppConfigKeys.PLAY_MIRROR, mirror).apply();
            VliveBridge.setMirror(mirror);
        });

        windowManager.addView(overlay, params);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (overlay != null && windowManager != null) {
            windowManager.removeViewImmediate(overlay);
        }
        super.onDestroy();
    }
}
