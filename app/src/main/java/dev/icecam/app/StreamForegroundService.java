package dev.icecam.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.util.Locale;

/** Keeps stream control alive in background with status notification. */
public final class StreamForegroundService extends Service {
    public static final String ACTION_START = "dev.icecam.app.action.STREAM_START";
    public static final String ACTION_STOP = "dev.icecam.app.action.STREAM_STOP";
    public static final String ACTION_REFRESH = "dev.icecam.app.action.STREAM_REFRESH";
    private static final int NOTIF_ID = 26001;
    private static final String CHANNEL = "icecam_stream";

    private final Handler main = new Handler(Looper.getMainLooper());
    private SmartLogger slog;
    private Runnable ticker;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        slog = SmartLogger.get(this);
        ensureChannel();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_REFRESH : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopTicker();
            stopForeground(true);
            stopSelf();
            slog.i("fgs", "stream foreground stopped");
            return START_NOT_STICKY;
        }
        startForeground(NOTIF_ID, buildNotification());
        startTicker();
        slog.i("fgs", "stream foreground active action=" + action);
        return START_STICKY;
    }

    @Override public void onDestroy() {
        stopTicker();
        super.onDestroy();
    }

    public static void start(android.content.Context ctx) {
        Intent i = new Intent(ctx, StreamForegroundService.class);
        i.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i);
        else ctx.startService(i);
    }

    public static void refresh(android.content.Context ctx) {
        Intent i = new Intent(ctx, StreamForegroundService.class);
        i.setAction(ACTION_REFRESH);
        ctx.startService(i);
    }

    public static void stop(android.content.Context ctx) {
        Intent i = new Intent(ctx, StreamForegroundService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    private void startTicker() {
        if (ticker != null) return;
        ticker = new Runnable() {
            @Override public void run() {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (nm != null) nm.notify(NOTIF_ID, buildNotification());
                main.postDelayed(this, 1500);
            }
        };
        main.post(ticker);
    }

    private void stopTicker() {
        if (ticker != null) main.removeCallbacks(ticker);
        ticker = null;
    }

    private Notification buildNotification() {
        SharedPreferences p = getSharedPreferences("app_config", MODE_PRIVATE);
        boolean active = p.getBoolean("ReplacementActive", false);
        String phase = p.getString("IceCamState", "IDLE");
        int slot = Math.max(1, Math.min(4, p.getInt("ActiveSlot", 1)));
        String media = p.getString("PlayFileMp4", "");
        int slash = Math.max(media.lastIndexOf('/'), media.lastIndexOf('\\'));
        if (slash >= 0) media = media.substring(slash + 1);
        if (media.length() > 32) media = media.substring(0, 29) + "…";

        Intent open = new Intent(this, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));

        Intent stopFloat = new Intent(this, FloatService.class);
        stopFloat.setAction("stop");
        PendingIntent stopPi = PendingIntent.getService(this, 1, stopFloat,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));

        String title = active ? "● Stream ACTIVE" : "○ Stream idle";
        String text = String.format(Locale.US, "M%d · %s · %s", slot, phase, media.isEmpty() ? "no media" : media);

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);
        b.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(active)
                .setContentIntent(pi)
                .addAction(new Notification.Action.Builder(null, "Open", pi).build())
                .addAction(new Notification.Action.Builder(null, "Hide float", stopPi).build());
        if (Build.VERSION.SDK_INT >= 26) b.setColor(active ? 0xff24d487 : 0xff607080);
        return b.build();
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL, "IceCam Stream", NotificationManager.IMPORTANCE_LOW);
        ch.setDescription("Background virtual camera stream status");
        nm.createNotificationChannel(ch);
    }
}
