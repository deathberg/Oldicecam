package com.xiaomi.vlive;

import android.R;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import p000A.C0006g;

/* loaded from: classes.dex */
public class MediaProjectionForegroundService extends Service {
    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public final void onCreate() {
        super.onCreate();
        NotificationChannel notificationChannel = new NotificationChannel("media_projection_channel", "Media Projection Service Channel", 2);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
        C0006g c0006g = new C0006g(this, "media_projection_channel");
        c0006g.f6e = C0006g.m16b("Screen Capture Service");
        c0006g.f7f = C0006g.m16b("Running MediaProjection foreground service");
        c0006g.f12k.icon = R.drawable.ic_media_play;
        startForeground(1, c0006g.m17a());
    }
}
