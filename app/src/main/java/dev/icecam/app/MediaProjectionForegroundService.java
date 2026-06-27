package dev.icecam.app;
import android.app.*;import android.content.*;import android.os.*;
public class MediaProjectionForegroundService extends Service { @Override public IBinder onBind(Intent i){return null;} @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;} }
