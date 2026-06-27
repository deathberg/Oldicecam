package com.xiaomi.vlive;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.lifecycle.C0817A;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.potplayer.music.R;
import com.xiaomi.vlive.App;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import okhttp3.HttpUrl;
import p007D0.RunnableC0063i;
import p036T0.C0293e;
import p037U.AbstractC0330t;
import p048Z0.AbstractC0458i;
import p048Z0.C0450a;
import p048Z0.C0457h;
import p059d1.C1390e;
import p059d1.ViewOnClickListenerC1387b;
import p059d1.ViewOnTouchListenerC1388c;

/* loaded from: classes.dex */
public class App extends Application {

    /* renamed from: k */
    public static App f2584k;

    /* renamed from: l */
    public static C1390e f2585l;

    /* renamed from: a */
    public SharedPreferences f2586a;

    /* renamed from: b */
    public RunnableC0063i f2587b = null;

    /* renamed from: c */
    public HandlerThread f2588c = null;

    /* renamed from: d */
    public Handler f2589d = null;

    /* renamed from: e */
    public final Handler f2590e = new Handler(Looper.getMainLooper());

    /* renamed from: f */
    public final C0817A f2591f = new C0817A();

    /* renamed from: g */
    public final C0817A f2592g = new C0817A();

    /* renamed from: h */
    public final C0817A f2593h = new C0817A();

    /* renamed from: i */
    public final C0817A f2594i = new C0817A();

    /* renamed from: j */
    public final HashSet f2595j = new HashSet();

    /* renamed from: h */
    public static void m1774h() {
        if (f2585l == null) {
            App app = f2584k;
            C1390e c1390e = new C1390e();
            c1390e.f2660d = false;
            c1390e.f2662f = app;
            f2585l = c1390e;
        }
        final C1390e c1390e2 = f2585l;
        if (c1390e2.f2658b != null) {
            return;
        }
        Context applicationContext = c1390e2.f2662f.getApplicationContext();
        c1390e2.f2657a = (WindowManager) applicationContext.getSystemService("window");
        c1390e2.f2658b = LayoutInflater.from(applicationContext).inflate(R.layout.float_layout, (ViewGroup) null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2038, 8, -3);
        c1390e2.f2659c = layoutParams;
        layoutParams.gravity = 8388659;
        layoutParams.x = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        layoutParams.y = 500;
        ImageView imageView = (ImageView) c1390e2.f2658b.findViewById(R.id.main_button);
        c1390e2.f2661e = (GridLayout) c1390e2.f2658b.findViewById(R.id.button_group);
        imageView.setImageDrawable(applicationContext.getApplicationInfo().loadIcon(applicationContext.getPackageManager()));
        final int i = 0;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i2 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i3 = i2 <= 360 ? i2 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i3).apply();
                        AbstractC0330t.m852d0(i3);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf7)).setOnClickListener(new ViewOnClickListenerC1387b(5));
        final int i2 = 11;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf12)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i2) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i3 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i3).apply();
                        AbstractC0330t.m852d0(i3);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i3 = 1;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf1)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i3) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i4 = 2;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf2)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i4) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i5 = 3;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf3)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i5) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i6 = 4;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf4)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i6) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i7 = 5;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf5)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i7) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i8 = 6;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf6)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i8) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i9 = 7;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf8)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i9) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i10 = 8;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf9)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i10) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i11 = 9;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf10)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i11) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        final int i12 = 10;
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf11)).setOnClickListener(new View.OnClickListener() { // from class: d1.d
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                WindowManager windowManager2;
                switch (i12) {
                    case 0:
                        C1390e c1390e3 = c1390e2;
                        boolean z2 = c1390e3.f2660d;
                        c1390e3.f2660d = !z2;
                        c1390e3.f2661e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        App app2 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app2.m1777b(1, 0L), app2.m1776a(1, 1170000L));
                        break;
                    case 2:
                        App app3 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app3.m1777b(2, 5000000L), app3.m1776a(2, 5900000L));
                        break;
                    case 3:
                        App app4 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app4.m1777b(3, 2000000L), app4.m1776a(3, 3200000L));
                        break;
                    case 4:
                        App app5 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app5.m1777b(4, 3200000L), app5.m1776a(4, 4000000L));
                        break;
                    case 5:
                        App app6 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app6.m1777b(5, 4000000L), app6.m1776a(5, 4000000L));
                        break;
                    case 6:
                        App app7 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app7.m1777b(6, 4000000L), app7.m1776a(6, 5000000L));
                        break;
                    case 7:
                        App app8 = c1390e2.f2662f;
                        AbstractC0330t.m843R(app8.m1777b(8, 5600000L), app8.m1776a(8, 6800000L));
                        break;
                    case 8:
                        App app9 = c1390e2.f2662f;
                        app9.m1782g(!app9.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(app9.m1778c()));
                        break;
                    case 9:
                        App app10 = c1390e2.f2662f;
                        int i22 = app10.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        app10.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 10:
                        App app11 = c1390e2.f2662f;
                        app11.f2586a.edit().putBoolean("PlayMirror", !app11.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(app11.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    default:
                        C1390e c1390e4 = c1390e2;
                        View view2 = c1390e4.f2658b;
                        if (view2 != null && (windowManager2 = c1390e4.f2657a) != null) {
                            windowManager2.removeView(view2);
                            c1390e4.f2658b = null;
                        }
                        View view3 = c1390e4.f2658b;
                        if (view3 != null && (windowManager = c1390e4.f2657a) != null) {
                            windowManager.removeViewImmediate(view3);
                            c1390e4.f2658b = null;
                            break;
                        }
                        break;
                }
            }
        });
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf31)).setOnClickListener(new ViewOnClickListenerC1387b(1));
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf32)).setOnClickListener(new ViewOnClickListenerC1387b(2));
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf33)).setOnClickListener(new ViewOnClickListenerC1387b(3));
        ((Button) c1390e2.f2658b.findViewById(R.id.butonf34)).setOnClickListener(new ViewOnClickListenerC1387b(4));
        imageView.setOnTouchListener(new ViewOnTouchListenerC1388c(1, c1390e2));
        c1390e2.f2657a.addView(c1390e2.f2658b, c1390e2.f2659c);
    }

    /* renamed from: j */
    public static String m1775j(int i, int i2) {
        if (i <= 0 || i2 < i) {
            return HttpUrl.FRAGMENT_ENCODE_SET;
        }
        Random random = new Random();
        int nextInt = random.nextInt((i2 - i) + 1) + i;
        StringBuilder sb = new StringBuilder(nextInt);
        for (int i3 = 0; i3 < nextInt; i3++) {
            sb.append("abcdefghijklmnopqrstuvwxyz".charAt(random.nextInt(26)));
        }
        return sb.toString();
    }

    /* renamed from: a */
    public final long m1776a(int i, long j2) {
        return this.f2586a.getLong("ActionRangeEnd" + i, j2);
    }

    /* renamed from: b */
    public final long m1777b(int i, long j2) {
        return this.f2586a.getLong("ActionRangebgin" + i, j2);
    }

    /* renamed from: c */
    public final boolean m1778c() {
        return this.f2586a.getBoolean("PlayisLoop", false);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v11 */
    /* JADX WARN: Type inference failed for: r0v3 */
    /* JADX WARN: Type inference failed for: r0v4, types: [int] */
    /* JADX WARN: Type inference failed for: r0v5, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r0v6, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r0v8 */
    /* JADX WARN: Type inference failed for: r7v2, types: [android.content.SharedPreferences$Editor] */
    /* renamed from: d */
    public final String m1779d() {
        String string = this.f2586a.getString("ServerName", HttpUrl.FRAGMENT_ENCODE_SET);
        boolean isEmpty = string.isEmpty();
        String str = string;
        if (isEmpty) {
            ?? r0 = 12;
            try {
                Method declaredMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("listServices", new Class[0]);
                declaredMethod.setAccessible(true);
                String[] strArr = (String[]) declaredMethod.invoke(null, new Object[0]);
                int length = strArr.length;
                if (length > 0) {
                    r0 = strArr[new Random().nextInt(length)] + m1775j(1, 3);
                } else {
                    r0 = m1775j(5, 12);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                r0 = m1775j(5, r0);
            }
            this.f2586a.edit().putString("ServerName", r0).apply();
            str = r0;
        }
        return str;
    }

    /* renamed from: e */
    public final void m1780e(int i, long j2) {
        this.f2586a.edit().putLong("ActionRangeEnd" + i, j2).apply();
    }

    /* renamed from: f */
    public final void m1781f(int i, long j2) {
        this.f2586a.edit().putLong("ActionRangebgin" + i, j2).apply();
    }

    /* renamed from: g */
    public final void m1782g(boolean z2) {
        this.f2586a.edit().putBoolean("PlayisLoop", z2).apply();
    }

    /* renamed from: i */
    public final void m1783i(String str) {
        C0817A c0817a = this.f2592g;
        Object obj = c0817a.f1855e;
        if (obj == C0817A.f1850j) {
            obj = null;
        }
        String str2 = (String) obj;
        if (str2 == null) {
            str2 = HttpUrl.FRAGMENT_ENCODE_SET;
        }
        c0817a.m1365e(str2 + str);
    }

    @Override // android.app.Application
    public final void onCreate() {
        super.onCreate();
        f2584k = this;
        C0293e c0293e = AbstractC0458i.f1428c;
        if (AbstractC0458i.f1426a == null || AbstractC0458i.f1427b == null || c0293e == null) {
            AbstractC0458i.f1426a = this;
            C0450a m1142a = C0450a.m1142a();
            m1142a.getClass();
            registerActivityLifecycleCallbacks(m1142a);
            C0457h c0457h = new C0457h();
            AbstractC0458i.f1427b = c0457h;
            c0457h.f1422a = AbstractC0458i.f1426a;
            if (c0293e == null) {
                c0293e = new C0293e(12);
            }
            AbstractC0458i.f1428c = c0293e;
        }
        HandlerThread handlerThread = new HandlerThread("TimerThread");
        this.f2588c = handlerThread;
        handlerThread.start();
        this.f2589d = new Handler(this.f2588c.getLooper());
        this.f2586a = getSharedPreferences("app_config", 0);
        this.f2591f.m1365e(0);
        if (AbstractC0330t.m837E() != null) {
            AbstractC0330t.m863q("killall vcplax");
        }
        Context applicationContext = getApplicationContext();
        String absolutePath = applicationContext.getFilesDir().getAbsolutePath();
        String str = AbstractC0330t.m863q("file /system/bin/cameraserver").contains("32-bit") ? "lib/armeabi-v7a" : "lib/arm64-v8a";
        try {
            ApplicationInfo applicationInfo = applicationContext.getApplicationInfo();
            File file = new File(absolutePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            ZipFile zipFile = new ZipFile(applicationInfo.sourceDir);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry nextElement = entries.nextElement();
                String name = nextElement.getName();
                if (name.startsWith(str) && name.endsWith(".so")) {
                    File file2 = new File(absolutePath, name);
                    File parentFile = file2.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    InputStream inputStream = zipFile.getInputStream(nextElement);
                    FileOutputStream fileOutputStream = new FileOutputStream(file2);
                    byte[] bArr = new byte[4096];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read == -1) {
                            break;
                        } else {
                            fileOutputStream.write(bArr, 0, read);
                        }
                    }
                    fileOutputStream.close();
                    inputStream.close();
                    file2.getAbsolutePath();
                }
            }
            zipFile.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        String m1779d = m1779d();
        AbstractC0330t.m863q("cp " + absolutePath + "/" + str + "/libvc.so /data/libvc.so");
        AbstractC0330t.m863q("cp " + absolutePath + "/" + str + "/libshadowhook.so /data/libvc++.so");
        AbstractC0330t.m863q("cp " + absolutePath + "/" + str + "/vcplax.so /data/vcplax");
        AbstractC0330t.m863q("chmod 700 /data/vcplax");
        StringBuilder sb = new StringBuilder("/data/vcplax ");
        sb.append(m1779d);
        sb.append("&");
        AbstractC0330t.m863q(sb.toString());
        RunnableC0063i runnableC0063i = new RunnableC0063i(9, this);
        this.f2587b = runnableC0063i;
        this.f2589d.post(runnableC0063i);
    }

    @Override // android.app.Application
    public final void onTerminate() {
        super.onTerminate();
        RunnableC0063i runnableC0063i = this.f2587b;
        if (runnableC0063i != null) {
            this.f2589d.removeCallbacks(runnableC0063i);
            this.f2587b = null;
        }
    }
}
