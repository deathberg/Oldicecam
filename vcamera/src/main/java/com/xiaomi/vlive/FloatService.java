package com.xiaomi.vlive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.xiaomi.vlive.util.VliveBridge;

/**
 * Floating control overlay (clean-room rebuild of the original FloatService).
 *
 * Shows the app icon as a draggable handle; tapping it toggles a 4x4 grid of
 * action buttons. Each button maps to a daemon command via {@link VliveBridge}.
 * Button labels and preset action-ranges match the recovered float_layout.xml.
 */
public class FloatService extends Service {

    private static final String CHANNEL = "com.xiaomi.vlive";

    // Default action ranges (begin,end in µs) per preset, from the reconstruction.
    private static final long[][] PRESET = {
            /* idx 0 unused */ {0, 0},
            /* 1 眼 */ {0L, 1_170_000L},
            /* 2 ↑ */ {5_000_000L, 5_900_000L},
            /* 3 嘴 */ {2_000_000L, 3_200_000L},
            /* 4 ← */ {3_200_000L, 4_000_000L},
            /* 5 正 */ {4_000_000L, 4_000_000L},
            /* 6 → */ {4_000_000L, 5_000_000L},
            /* 7 unused */ {0, 0},
            /* 8 ↓ */ {5_600_000L, 6_800_000L},
    };

    private WindowManager windowManager;
    private View root;
    private WindowManager.LayoutParams params;
    private GridLayout buttonGroup;
    private boolean expanded = false;
    private App app;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        app = (App) getApplication();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        root = LayoutInflater.from(this).inflate(R.layout.float_layout, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 200;
        params.y = 500;

        ImageView handle = root.findViewById(R.id.main_button);
        buttonGroup = root.findViewById(R.id.button_group);
        handle.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));
        handle.setOnClickListener(v -> {
            expanded = !expanded;
            buttonGroup.setVisibility(expanded ? View.VISIBLE : View.GONE);
        });
        handle.setOnTouchListener(new DragListener());

        wirePresetButton(R.id.butonf1, 1);
        wirePresetButton(R.id.butonf2, 2);
        wirePresetButton(R.id.butonf3, 3);
        wirePresetButton(R.id.butonf4, 4);
        wirePresetButton(R.id.butonf5, 5);
        wirePresetButton(R.id.butonf6, 6);
        wirePresetButton(R.id.butonf8, 8);

        ((Button) root.findViewById(R.id.butonf7)).setOnClickListener(v -> { // 播 play
            if (!VliveBridge.isPlaying()) VliveBridge.toast("\u64ad\u653e\u5931\u8d25"); // play failed
        });
        ((Button) root.findViewById(R.id.butonf9)).setOnClickListener(v -> { // 循 loop
            boolean loop = !app.isLoop();
            app.setLoop(loop);
            VliveBridge.setLoop(loop);
        });
        ((Button) root.findViewById(R.id.butonf10)).setOnClickListener(v -> { // 转 rotate
            int angle = app.getAngle() + 90;
            if (angle > 360) angle = 0;
            app.setAngle(angle);
            VliveBridge.setAngle(angle);
        });
        ((Button) root.findViewById(R.id.butonf11)).setOnClickListener(v -> { // 翻 mirror
            boolean mirror = !app.isMirror();
            app.setMirror(mirror);
            VliveBridge.setMirror(mirror);
        });
        ((Button) root.findViewById(R.id.butonf12)).setOnClickListener(v -> stopOverlay()); // 关 close

        // 1/2/3 -> select /sdcard/Movies/{1,2,3}.mp4 ; 替 -> replace
        wireVideoButton(R.id.butonf31, "/sdcard/Movies/1.mp4");
        wireVideoButton(R.id.butonf32, "/sdcard/Movies/2.mp4");
        wireVideoButton(R.id.butonf33, "/sdcard/Movies/3.mp4");
        ((Button) root.findViewById(R.id.butonf34)).setOnClickListener(v -> VliveBridge.replace());

        windowManager.addView(root, params);
    }

    private void wirePresetButton(int id, int preset) {
        ((Button) root.findViewById(id)).setOnClickListener(v ->
                VliveBridge.setPlayRange(
                        app.rangeBegin(preset, PRESET[preset][0]),
                        app.rangeEnd(preset, PRESET[preset][1])));
    }

    private void wireVideoButton(int id, String path) {
        ((Button) root.findViewById(id)).setOnClickListener(v -> {
            if (!new java.io.File(path).exists()) {
                VliveBridge.toast(path + " \u4e0d\u5b58\u5728"); // not found
            } else {
                VliveBridge.selectVideo(path, 1);
            }
        });
    }

    private void stopOverlay() {
        if (root != null && windowManager != null) {
            windowManager.removeView(root);
            root = null;
        }
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL, "\u524d\u53f0\u670d\u52a1\u901a\u77e5", NotificationManager.IMPORTANCE_LOW));
        }
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("\u5df2\u5f00\u542f\u60ac\u6d6e\u7a97\u53e3")     // overlay enabled
                .setContentText("\u9632\u6b62\u7a0b\u5e8f\u540e\u53f0\u8fd0\u884c\u88ab\u5173\u95ed") // keep alive
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build();
        startForeground(1, n);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
        if (root != null && windowManager != null) windowManager.removeView(root);
    }

    /** Drag handler so the overlay can be repositioned. */
    private class DragListener implements View.OnTouchListener {
        private int initialX, initialY;
        private float touchX, touchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    params.x = initialX + (int) (event.getRawX() - touchX);
                    params.y = initialY + (int) (event.getRawY() - touchY);
                    if (root != null) windowManager.updateViewLayout(root, params);
                    return false;
                default:
                    return false;
            }
        }
    }
}
