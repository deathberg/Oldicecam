package com.xiaomi.vlive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Foreground service used while a MediaProjection screen-capture session is active. */
public class MediaProjectionForegroundService extends Service {

    private static final String CHANNEL = "media_projection_channel";

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL, "Media Projection Service Channel", NotificationManager.IMPORTANCE_LOW));
        }
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Screen Capture Service")
                .setContentText("Running MediaProjection foreground service")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
        startForeground(1, n);
    }
}
