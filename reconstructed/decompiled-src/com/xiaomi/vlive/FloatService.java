package com.xiaomi.vlive;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.potplayer.music.R;
import com.xiaomi.vlive.FloatService;
import p000A.C0006g;
import p037U.AbstractC0330t;
import p059d1.ViewOnClickListenerC1387b;
import p059d1.ViewOnTouchListenerC1388c;

/* loaded from: classes.dex */
public class FloatService extends Service {

    /* renamed from: g */
    public static final /* synthetic */ int f2596g = 0;

    /* renamed from: a */
    public WindowManager f2597a;

    /* renamed from: b */
    public View f2598b;

    /* renamed from: c */
    public WindowManager.LayoutParams f2599c;

    /* renamed from: d */
    public boolean f2600d = false;

    /* renamed from: e */
    public GridLayout f2601e;

    /* renamed from: f */
    public App f2602f;

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public final void onCreate() {
        super.onCreate();
        this.f2602f = (App) getApplication();
        this.f2597a = (WindowManager) getSystemService("window");
        this.f2598b = LayoutInflater.from(this).inflate(R.layout.float_layout, (ViewGroup) null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2038, 8, -3);
        this.f2599c = layoutParams;
        layoutParams.gravity = 8388659;
        layoutParams.x = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        layoutParams.y = 500;
        ImageView imageView = (ImageView) this.f2598b.findViewById(R.id.main_button);
        this.f2601e = (GridLayout) this.f2598b.findViewById(R.id.button_group);
        imageView.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));
        final int i = 0;
        imageView.setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i2 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i3 = i2 <= 360 ? i2 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i3).apply();
                        AbstractC0330t.m852d0(i3);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        ((Button) this.f2598b.findViewById(R.id.butonf7)).setOnClickListener(new ViewOnClickListenerC1387b(0));
        final int i2 = 7;
        ((Button) this.f2598b.findViewById(R.id.butonf12)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i2) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i3 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i3).apply();
                        AbstractC0330t.m852d0(i3);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i3 = 8;
        ((Button) this.f2598b.findViewById(R.id.butonf1)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i3) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i4 = 9;
        ((Button) this.f2598b.findViewById(R.id.butonf2)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i4) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i5 = 10;
        ((Button) this.f2598b.findViewById(R.id.butonf3)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i5) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i6 = 11;
        ((Button) this.f2598b.findViewById(R.id.butonf4)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i6) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i7 = 1;
        ((Button) this.f2598b.findViewById(R.id.butonf5)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i7) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i8 = 2;
        ((Button) this.f2598b.findViewById(R.id.butonf6)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i8) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i9 = 3;
        ((Button) this.f2598b.findViewById(R.id.butonf8)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i9) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i10 = 4;
        ((Button) this.f2598b.findViewById(R.id.butonf9)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i10) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i11 = 5;
        ((Button) this.f2598b.findViewById(R.id.butonf10)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i11) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        final int i12 = 6;
        ((Button) this.f2598b.findViewById(R.id.butonf11)).setOnClickListener(new View.OnClickListener(this) { // from class: d1.a

            /* renamed from: b */
            public final /* synthetic */ FloatService f2646b;

            {
                this.f2646b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WindowManager windowManager;
                switch (i12) {
                    case 0:
                        FloatService floatService = this.f2646b;
                        boolean z2 = floatService.f2600d;
                        floatService.f2600d = !z2;
                        floatService.f2601e.setVisibility(!z2 ? 0 : 8);
                        break;
                    case 1:
                        FloatService floatService2 = this.f2646b;
                        AbstractC0330t.m843R(floatService2.f2602f.m1777b(5, 4000000L), floatService2.f2602f.m1776a(5, 4000000L));
                        break;
                    case 2:
                        FloatService floatService3 = this.f2646b;
                        AbstractC0330t.m843R(floatService3.f2602f.m1777b(6, 4000000L), floatService3.f2602f.m1776a(6, 5000000L));
                        break;
                    case 3:
                        FloatService floatService4 = this.f2646b;
                        AbstractC0330t.m843R(floatService4.f2602f.m1777b(8, 5600000L), floatService4.f2602f.m1776a(8, 6800000L));
                        break;
                    case 4:
                        FloatService floatService5 = this.f2646b;
                        floatService5.f2602f.m1782g(!r6.m1778c());
                        AbstractC0330t.m853e(Boolean.valueOf(floatService5.f2602f.m1778c()));
                        break;
                    case 5:
                        FloatService floatService6 = this.f2646b;
                        int i22 = floatService6.f2602f.f2586a.getInt("PlayAngle", 0) + 90;
                        int i32 = i22 <= 360 ? i22 : 0;
                        floatService6.f2602f.f2586a.edit().putInt("PlayAngle", i32).apply();
                        AbstractC0330t.m852d0(i32);
                        break;
                    case 6:
                        FloatService floatService7 = this.f2646b;
                        floatService7.f2602f.f2586a.edit().putBoolean("PlayMirror", !r6.f2586a.getBoolean("PlayMirror", false)).apply();
                        AbstractC0330t.m851c0(Boolean.valueOf(floatService7.f2602f.f2586a.getBoolean("PlayMirror", false)));
                        break;
                    case 7:
                        FloatService floatService8 = this.f2646b;
                        View view2 = floatService8.f2598b;
                        if (view2 != null && (windowManager = floatService8.f2597a) != null) {
                            windowManager.removeView(view2);
                            floatService8.f2598b = null;
                        }
                        floatService8.stopSelf();
                        break;
                    case 8:
                        FloatService floatService9 = this.f2646b;
                        AbstractC0330t.m843R(floatService9.f2602f.m1777b(1, 0L), floatService9.f2602f.m1776a(1, 1170000L));
                        break;
                    case 9:
                        FloatService floatService10 = this.f2646b;
                        AbstractC0330t.m843R(floatService10.f2602f.m1777b(2, 5000000L), floatService10.f2602f.m1776a(2, 5900000L));
                        break;
                    case 10:
                        FloatService floatService11 = this.f2646b;
                        AbstractC0330t.m843R(floatService11.f2602f.m1777b(3, 2000000L), floatService11.f2602f.m1776a(3, 3200000L));
                        break;
                    default:
                        FloatService floatService12 = this.f2646b;
                        AbstractC0330t.m843R(floatService12.f2602f.m1777b(4, 3200000L), floatService12.f2602f.m1776a(4, 4000000L));
                        break;
                }
            }
        });
        imageView.setOnTouchListener(new ViewOnTouchListenerC1388c(0, this));
        this.f2597a.addView(this.f2598b, this.f2599c);
    }

    @Override // android.app.Service
    public final void onDestroy() {
        stopForeground(true);
        super.onDestroy();
        View view = this.f2598b;
        if (view != null) {
            this.f2597a.removeView(view);
        }
    }

    @Override // android.app.Service
    public final int onStartCommand(Intent intent, int i, int i2) {
        ((NotificationManager) getSystemService("notification")).createNotificationChannel(new NotificationChannel("com.xiaomi.vlive", "\u524d\u53f0\u670d\u52a1\u901a\u77e5", 2));
        C0006g c0006g = new C0006g(this, "com.xiaomi.vlive");
        c0006g.f6e = C0006g.m16b("\u5df2\u5f00\u542f\u60ac\u6d6e\u7a97\u53e3");
        c0006g.f7f = C0006g.m16b("\u9632\u6b62\u7a0b\u5e8f\u540e\u53f0\u8fd0\u884c\u88ab\u5173\u95ed");
        c0006g.f12k.icon = R.drawable.ic_launcher_foreground;
        startForeground(1, c0006g.m17a());
        return 1;
    }
}
