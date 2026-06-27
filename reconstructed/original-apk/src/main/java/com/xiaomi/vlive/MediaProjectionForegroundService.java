package com.xiaomi.vlive;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/** Reconstructed from {@code com.xiaomi.vlive.MediaProjectionForegroundService}. */
public class MediaProjectionForegroundService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(
                "media_projection_channel",
                "Media Projection Service Channel",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "media_projection_channel")
                .setContentTitle("Screen Capture Service")
                .setContentText("Running MediaProjection foreground service")
                .setSmallIcon(android.R.drawable.ic_media_play);
        startForeground(1, builder.build());
    }
}
