package dev.icecam.app;

import android.app.Application;

public class IceCamApp extends Application {
    public static IceCamApp instance;
    @Override public void onCreate() { super.onCreate(); instance = this; }
}
