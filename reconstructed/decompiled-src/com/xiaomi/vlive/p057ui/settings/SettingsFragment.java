package com.xiaomi.vlive.p057ui.settings;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.C0838V;
import androidx.lifecycle.InterfaceC0836T;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.potplayer.music.R;
import com.xiaomi.vlive.App;
import com.xiaomi.vlive.p057ui.settings.SettingsFragment;
import p037U.AbstractC0330t;
import p037U.C0332v;
import p045Y.AbstractComponentCallbacksC0442x;
import p045Y.C0389B;
import p055c0.C1028e;
import p062e1.C1398b;
import p065f1.C1440b;
import p066g.AbstractActivityC1471i;
import p070h1.C1510d;
import p070h1.RunnableC1509c;
import p081l.AbstractC1807z;
import p099q1.AbstractC1952d;
import p099q1.AbstractC1957i;
import p099q1.C1950b;

/* loaded from: classes.dex */
public class SettingsFragment extends AbstractComponentCallbacksC0442x {

    /* renamed from: W */
    public C1398b f2634W;

    /* renamed from: Z */
    public TextView f2637Z;

    /* renamed from: a0 */
    public App f2638a0;

    /* renamed from: c0 */
    public RunnableC1509c f2640c0;

    /* renamed from: X */
    public FrameLayout f2635X = null;

    /* renamed from: Y */
    public View f2636Y = null;

    /* renamed from: b0 */
    public final Handler f2639b0 = new Handler(Looper.getMainLooper());

    @Override // p045Y.AbstractComponentCallbacksC0442x
    /* renamed from: u */
    public final View mo1136u(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        this.f2638a0 = (App) m1119G().getApplication();
        C0838V mo979c = mo979c();
        InterfaceC0836T m1127i = m1127i();
        C1028e mo1074a = mo1074a();
        AbstractC1952d.m2508e(m1127i, "factory");
        C0332v c0332v = new C0332v(mo979c, m1127i, mo1074a);
        C1950b m2512a = AbstractC1957i.m2512a(C1510d.class);
        String m865s = AbstractC0330t.m865s(m2512a);
        if (m865s == null) {
            throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
        }
        View inflate = layoutInflater.inflate(R.layout.fragment_settings, viewGroup, false);
        int i = R.id.actionRangebgin1;
        EditText editText = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin1);
        if (editText != null) {
            i = R.id.actionRangebgin2;
            EditText editText2 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin2);
            if (editText2 != null) {
                i = R.id.actionRangebgin3;
                EditText editText3 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin3);
                if (editText3 != null) {
                    i = R.id.actionRangebgin4;
                    EditText editText4 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin4);
                    if (editText4 != null) {
                        i = R.id.actionRangebgin5;
                        EditText editText5 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin5);
                        if (editText5 != null) {
                            i = R.id.actionRangebgin6;
                            EditText editText6 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin6);
                            if (editText6 != null) {
                                i = R.id.actionRangebgin8;
                                EditText editText7 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangebgin8);
                                if (editText7 != null) {
                                    i = R.id.actionRangeend1;
                                    EditText editText8 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend1);
                                    if (editText8 != null) {
                                        i = R.id.actionRangeend2;
                                        EditText editText9 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend2);
                                        if (editText9 != null) {
                                            i = R.id.actionRangeend3;
                                            EditText editText10 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend3);
                                            if (editText10 != null) {
                                                i = R.id.actionRangeend4;
                                                EditText editText11 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend4);
                                                if (editText11 != null) {
                                                    i = R.id.actionRangeend5;
                                                    EditText editText12 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend5);
                                                    if (editText12 != null) {
                                                        i = R.id.actionRangeend6;
                                                        EditText editText13 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend6);
                                                        if (editText13 != null) {
                                                            i = R.id.actionRangeend8;
                                                            EditText editText14 = (EditText) AbstractC1807z.m2262i(inflate, R.id.actionRangeend8);
                                                            if (editText14 != null) {
                                                                i = R.id.colorctype_1;
                                                                RadioButton radioButton = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.colorctype_1);
                                                                if (radioButton != null) {
                                                                    i = R.id.colorctype_2;
                                                                    RadioButton radioButton2 = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.colorctype_2);
                                                                    if (radioButton2 != null) {
                                                                        i = R.id.colorctype_3;
                                                                        RadioButton radioButton3 = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.colorctype_3);
                                                                        if (radioButton3 != null) {
                                                                            i = R.id.radioGroup_color_type;
                                                                            RadioGroup radioGroup = (RadioGroup) AbstractC1807z.m2262i(inflate, R.id.radioGroup_color_type);
                                                                            if (radioGroup != null) {
                                                                                i = R.id.rootLayout;
                                                                                FrameLayout frameLayout = (FrameLayout) AbstractC1807z.m2262i(inflate, R.id.rootLayout);
                                                                                if (frameLayout != null) {
                                                                                    i = R.id.rootLinearLayout;
                                                                                    if (((LinearLayout) AbstractC1807z.m2262i(inflate, R.id.rootLinearLayout)) != null) {
                                                                                        i = R.id.savePlayTime;
                                                                                        Button button = (Button) AbstractC1807z.m2262i(inflate, R.id.savePlayTime);
                                                                                        if (button != null) {
                                                                                            i = R.id.selectxy;
                                                                                            Button button2 = (Button) AbstractC1807z.m2262i(inflate, R.id.selectxy);
                                                                                            if (button2 != null) {
                                                                                                i = R.id.textxy;
                                                                                                TextView textView = (TextView) AbstractC1807z.m2262i(inflate, R.id.textxy);
                                                                                                if (textView != null) {
                                                                                                    i = R.id.tips1;
                                                                                                    TextView textView2 = (TextView) AbstractC1807z.m2262i(inflate, R.id.tips1);
                                                                                                    if (textView2 != null) {
                                                                                                        ConstraintLayout constraintLayout = (ConstraintLayout) inflate;
                                                                                                        this.f2634W = new C1398b(constraintLayout, editText, editText2, editText3, editText4, editText5, editText6, editText7, editText8, editText9, editText10, editText11, editText12, editText13, editText14, radioButton, radioButton2, radioButton3, radioGroup, frameLayout, button, button2, textView, textView2);
                                                                                                        final int i2 = 0;
                                                                                                        button2.setOnClickListener(new View.OnClickListener(this) { // from class: h1.a

                                                                                                            /* renamed from: b */
                                                                                                            public final /* synthetic */ SettingsFragment f3141b;

                                                                                                            {
                                                                                                                this.f3141b = this;
                                                                                                            }

                                                                                                            @Override // android.view.View.OnClickListener
                                                                                                            public final void onClick(View view) {
                                                                                                                switch (i2) {
                                                                                                                    case 0:
                                                                                                                        final SettingsFragment settingsFragment = this.f3141b;
                                                                                                                        if (settingsFragment.f2636Y == null) {
                                                                                                                            C0389B c0389b = settingsFragment.f1376t;
                                                                                                                            final AbstractActivityC1471i abstractActivityC1471i = c0389b == null ? null : c0389b.f1094e;
                                                                                                                            settingsFragment.f2634W.f2713s.setVisibility(0);
                                                                                                                            View view2 = new View(abstractActivityC1471i);
                                                                                                                            settingsFragment.f2636Y = view2;
                                                                                                                            view2.setBackgroundColor(Color.parseColor("#88000000"));
                                                                                                                            settingsFragment.f2634W.f2713s.addView(settingsFragment.f2636Y, new FrameLayout.LayoutParams(-1, -1));
                                                                                                                            settingsFragment.f2636Y.bringToFront();
                                                                                                                            TextView textView3 = new TextView(abstractActivityC1471i);
                                                                                                                            settingsFragment.f2637Z = textView3;
                                                                                                                            textView3.setTextColor(ViewCompat.MEASURED_STATE_MASK);
                                                                                                                            settingsFragment.f2637Z.setTextSize(12.0f);
                                                                                                                            settingsFragment.f2637Z.setText("\u9009\u62e9\u5750\u6807\uff0c3\u79d2\u540e\u81ea\u52a8\u786e\u8ba4");
                                                                                                                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
                                                                                                                            layoutParams.gravity = 8388659;
                                                                                                                            layoutParams.setMargins(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION, 500, 0, 0);
                                                                                                                            settingsFragment.f2634W.f2713s.addView(settingsFragment.f2637Z, layoutParams);
                                                                                                                            settingsFragment.f2637Z.bringToFront();
                                                                                                                            settingsFragment.f2636Y.setOnTouchListener(new View.OnTouchListener() { // from class: h1.b
                                                                                                                                /* JADX WARN: Multi-variable type inference failed */
                                                                                                                                /* JADX WARN: Type inference failed for: r12v9, types: [h1.c, java.lang.Runnable] */
                                                                                                                                @Override // android.view.View.OnTouchListener
                                                                                                                                public final boolean onTouch(View view3, MotionEvent motionEvent) {
                                                                                                                                    AbstractActivityC1471i abstractActivityC1471i2 = abstractActivityC1471i;
                                                                                                                                    final SettingsFragment settingsFragment2 = SettingsFragment.this;
                                                                                                                                    settingsFragment2.getClass();
                                                                                                                                    try {
                                                                                                                                        if (motionEvent.getAction() == 0) {
                                                                                                                                            int x2 = (int) motionEvent.getX();
                                                                                                                                            int y2 = (int) motionEvent.getY();
                                                                                                                                            int[] iArr = new int[2];
                                                                                                                                            view3.getLocationOnScreen(iArr);
                                                                                                                                            final int x3 = iArr[0] + ((int) motionEvent.getX());
                                                                                                                                            final int y3 = iArr[1] + ((int) motionEvent.getY());
                                                                                                                                            settingsFragment2.f2634W.f2715u.setText("\u5f53\u524d\u9009\u62e9\u76d1\u6d4b\u5c4f\u5e55\u5750\u6807: (" + x3 + ", " + y3 + ")");
                                                                                                                                            settingsFragment2.f2637Z.setText("\u9009\u62e9\u5750\u6807\uff0c3\u79d2\u540e\u81ea\u52a8\u786e\u8ba4: (" + x3 + ", " + y3 + ")");
                                                                                                                                            FrameLayout frameLayout2 = settingsFragment2.f2635X;
                                                                                                                                            if (frameLayout2 != null) {
                                                                                                                                                settingsFragment2.f2634W.f2713s.removeView(frameLayout2);
                                                                                                                                            }
                                                                                                                                            FrameLayout frameLayout3 = new FrameLayout(abstractActivityC1471i2);
                                                                                                                                            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(40, 40);
                                                                                                                                            layoutParams2.leftMargin = x2 - 20;
                                                                                                                                            layoutParams2.topMargin = y2 - 20;
                                                                                                                                            frameLayout3.setLayoutParams(layoutParams2);
                                                                                                                                            View view4 = new View(abstractActivityC1471i2);
                                                                                                                                            view4.setBackgroundColor(SupportMenu.CATEGORY_MASK);
                                                                                                                                            FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-1, 4);
                                                                                                                                            layoutParams3.gravity = 16;
                                                                                                                                            frameLayout3.addView(view4, layoutParams3);
                                                                                                                                            View view5 = new View(abstractActivityC1471i2);
                                                                                                                                            view5.setBackgroundColor(SupportMenu.CATEGORY_MASK);
                                                                                                                                            FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(4, -1);
                                                                                                                                            layoutParams4.gravity = 1;
                                                                                                                                            frameLayout3.addView(view5, layoutParams4);
                                                                                                                                            settingsFragment2.f2634W.f2713s.addView(frameLayout3);
                                                                                                                                            settingsFragment2.f2635X = frameLayout3;
                                                                                                                                            RunnableC1509c runnableC1509c = settingsFragment2.f2640c0;
                                                                                                                                            Handler handler = settingsFragment2.f2639b0;
                                                                                                                                            if (runnableC1509c != null) {
                                                                                                                                                handler.removeCallbacks(runnableC1509c);
                                                                                                                                            }
                                                                                                                                            ?? r12 = new Runnable() { // from class: h1.c
                                                                                                                                                @Override // java.lang.Runnable
                                                                                                                                                public final void run() {
                                                                                                                                                    SettingsFragment settingsFragment3 = SettingsFragment.this;
                                                                                                                                                    FrameLayout frameLayout4 = settingsFragment3.f2635X;
                                                                                                                                                    if (frameLayout4 != null) {
                                                                                                                                                        settingsFragment3.f2634W.f2713s.removeView(frameLayout4);
                                                                                                                                                        settingsFragment3.f2635X = null;
                                                                                                                                                    }
                                                                                                                                                    View view6 = settingsFragment3.f2636Y;
                                                                                                                                                    if (view6 != null) {
                                                                                                                                                        settingsFragment3.f2634W.f2713s.removeView(view6);
                                                                                                                                                        settingsFragment3.f2636Y = null;
                                                                                                                                                    }
                                                                                                                                                    TextView textView4 = settingsFragment3.f2637Z;
                                                                                                                                                    if (textView4 != null) {
                                                                                                                                                        settingsFragment3.f2634W.f2713s.removeView(textView4);
                                                                                                                                                        settingsFragment3.f2637Z = null;
                                                                                                                                                    }
                                                                                                                                                    settingsFragment3.f2634W.f2713s.setVisibility(8);
                                                                                                                                                    SharedPreferences.Editor edit = settingsFragment3.f2638a0.f2586a.edit();
                                                                                                                                                    int i3 = x3;
                                                                                                                                                    edit.putInt("MonitorTargetX", i3).apply();
                                                                                                                                                    SharedPreferences.Editor edit2 = settingsFragment3.f2638a0.f2586a.edit();
                                                                                                                                                    int i4 = y3;
                                                                                                                                                    edit2.putInt("MonitorTargetY", i4).apply();
                                                                                                                                                    AbstractC0330t.m854f(1, App.f2584k.getApplicationContext(), "\u8bbe\u7f6e\u5750\u6807\u6210\u529f(" + i3 + "," + i4 + ")");
                                                                                                                                                }
                                                                                                                                            };
                                                                                                                                            settingsFragment2.f2640c0 = r12;
                                                                                                                                            handler.postDelayed(r12, 3000L);
                                                                                                                                        }
                                                                                                                                    } catch (Exception unused) {
                                                                                                                                    }
                                                                                                                                    return true;
                                                                                                                                }
                                                                                                                            });
                                                                                                                            break;
                                                                                                                        }
                                                                                                                        break;
                                                                                                                    default:
                                                                                                                        SettingsFragment settingsFragment2 = this.f3141b;
                                                                                                                        settingsFragment2.getClass();
                                                                                                                        try {
                                                                                                                            long parseLong = Long.parseLong(settingsFragment2.f2634W.f2695a.getText().toString().trim());
                                                                                                                            long parseLong2 = Long.parseLong(settingsFragment2.f2634W.f2702h.getText().toString().trim());
                                                                                                                            long parseLong3 = Long.parseLong(settingsFragment2.f2634W.f2696b.getText().toString().trim());
                                                                                                                            long parseLong4 = Long.parseLong(settingsFragment2.f2634W.f2703i.getText().toString().trim());
                                                                                                                            long parseLong5 = Long.parseLong(settingsFragment2.f2634W.f2697c.getText().toString().trim());
                                                                                                                            long parseLong6 = Long.parseLong(settingsFragment2.f2634W.f2704j.getText().toString().trim());
                                                                                                                            long parseLong7 = Long.parseLong(settingsFragment2.f2634W.f2698d.getText().toString().trim());
                                                                                                                            long parseLong8 = Long.parseLong(settingsFragment2.f2634W.f2705k.getText().toString().trim());
                                                                                                                            long parseLong9 = Long.parseLong(settingsFragment2.f2634W.f2699e.getText().toString().trim());
                                                                                                                            long parseLong10 = Long.parseLong(settingsFragment2.f2634W.f2706l.getText().toString().trim());
                                                                                                                            long parseLong11 = Long.parseLong(settingsFragment2.f2634W.f2700f.getText().toString().trim());
                                                                                                                            long parseLong12 = Long.parseLong(settingsFragment2.f2634W.f2707m.getText().toString().trim());
                                                                                                                            long parseLong13 = Long.parseLong(settingsFragment2.f2634W.f2701g.getText().toString().trim());
                                                                                                                            long parseLong14 = Long.parseLong(settingsFragment2.f2634W.f2708n.getText().toString().trim());
                                                                                                                            if (parseLong2 >= parseLong && parseLong4 >= parseLong3 && parseLong6 >= parseLong5 && parseLong8 >= parseLong7 && parseLong10 >= parseLong9 && parseLong12 >= parseLong11 && parseLong14 >= parseLong13) {
                                                                                                                                settingsFragment2.f2638a0.m1781f(1, parseLong);
                                                                                                                                settingsFragment2.f2638a0.m1780e(1, parseLong2);
                                                                                                                                settingsFragment2.f2638a0.m1781f(2, parseLong3);
                                                                                                                                settingsFragment2.f2638a0.m1780e(2, parseLong4);
                                                                                                                                settingsFragment2.f2638a0.m1781f(3, parseLong5);
                                                                                                                                settingsFragment2.f2638a0.m1780e(3, parseLong6);
                                                                                                                                settingsFragment2.f2638a0.m1781f(4, parseLong7);
                                                                                                                                settingsFragment2.f2638a0.m1780e(4, parseLong8);
                                                                                                                                settingsFragment2.f2638a0.m1781f(5, parseLong9);
                                                                                                                                settingsFragment2.f2638a0.m1780e(5, parseLong10);
                                                                                                                                settingsFragment2.f2638a0.m1781f(6, parseLong11);
                                                                                                                                settingsFragment2.f2638a0.m1780e(6, parseLong12);
                                                                                                                                settingsFragment2.f2638a0.m1781f(8, parseLong13);
                                                                                                                                settingsFragment2.f2638a0.m1780e(8, parseLong14);
                                                                                                                                AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                break;
                                                                                                                            }
                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u5931\u8d25\n\u7ed3\u675f\u65f6\u95f4\u4e0d\u80fd\u5927\u4e8e\u5f00\u59cb\u65f6\u95f4");
                                                                                                                        } catch (NumberFormatException unused) {
                                                                                                                            AbstractC0330t.m854f(1, App.f2584k.getApplicationContext(), "\u4fdd\u5b58\u5931\u8d25,\u8f93\u5165\u6570\u636e\u6709\u8bef");
                                                                                                                            return;
                                                                                                                        }
                                                                                                                        break;
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                        this.f2634W.f2715u.setText("\u5f53\u524d\u9009\u62e9\u76d1\u6d4b\u5c4f\u5e55\u5750\u6807: (" + this.f2638a0.f2586a.getInt("MonitorTargetX", 55) + ", " + this.f2638a0.f2586a.getInt("MonitorTargetY", 380) + ")");
                                                                                                        final int i3 = 1;
                                                                                                        this.f2634W.f2714t.setOnClickListener(new View.OnClickListener(this) { // from class: h1.a

                                                                                                            /* renamed from: b */
                                                                                                            public final /* synthetic */ SettingsFragment f3141b;

                                                                                                            {
                                                                                                                this.f3141b = this;
                                                                                                            }

                                                                                                            @Override // android.view.View.OnClickListener
                                                                                                            public final void onClick(View view) {
                                                                                                                switch (i3) {
                                                                                                                    case 0:
                                                                                                                        final SettingsFragment settingsFragment = this.f3141b;
                                                                                                                        if (settingsFragment.f2636Y == null) {
                                                                                                                            C0389B c0389b = settingsFragment.f1376t;
                                                                                                                            final AbstractActivityC1471i abstractActivityC1471i = c0389b == null ? null : c0389b.f1094e;
                                                                                                                            settingsFragment.f2634W.f2713s.setVisibility(0);
                                                                                                                            View view2 = new View(abstractActivityC1471i);
                                                                                                                            settingsFragment.f2636Y = view2;
                                                                                                                            view2.setBackgroundColor(Color.parseColor("#88000000"));
                                                                                                                            settingsFragment.f2634W.f2713s.addView(settingsFragment.f2636Y, new FrameLayout.LayoutParams(-1, -1));
                                                                                                                            settingsFragment.f2636Y.bringToFront();
                                                                                                                            TextView textView3 = new TextView(abstractActivityC1471i);
                                                                                                                            settingsFragment.f2637Z = textView3;
                                                                                                                            textView3.setTextColor(ViewCompat.MEASURED_STATE_MASK);
                                                                                                                            settingsFragment.f2637Z.setTextSize(12.0f);
                                                                                                                            settingsFragment.f2637Z.setText("\u9009\u62e9\u5750\u6807\uff0c3\u79d2\u540e\u81ea\u52a8\u786e\u8ba4");
                                                                                                                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
                                                                                                                            layoutParams.gravity = 8388659;
                                                                                                                            layoutParams.setMargins(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION, 500, 0, 0);
                                                                                                                            settingsFragment.f2634W.f2713s.addView(settingsFragment.f2637Z, layoutParams);
                                                                                                                            settingsFragment.f2637Z.bringToFront();
                                                                                                                            settingsFragment.f2636Y.setOnTouchListener(new View.OnTouchListener() { // from class: h1.b
                                                                                                                                /* JADX WARN: Multi-variable type inference failed */
                                                                                                                                /* JADX WARN: Type inference failed for: r12v9, types: [h1.c, java.lang.Runnable] */
                                                                                                                                @Override // android.view.View.OnTouchListener
                                                                                                                                public final boolean onTouch(View view3, MotionEvent motionEvent) {
                                                                                                                                    AbstractActivityC1471i abstractActivityC1471i2 = abstractActivityC1471i;
                                                                                                                                    final SettingsFragment settingsFragment2 = SettingsFragment.this;
                                                                                                                                    settingsFragment2.getClass();
                                                                                                                                    try {
                                                                                                                                        if (motionEvent.getAction() == 0) {
                                                                                                                                            int x2 = (int) motionEvent.getX();
                                                                                                                                            int y2 = (int) motionEvent.getY();
                                                                                                                                            int[] iArr = new int[2];
                                                                                                                                            view3.getLocationOnScreen(iArr);
                                                                                                                                            final int x3 = iArr[0] + ((int) motionEvent.getX());
                                                                                                                                            final int y3 = iArr[1] + ((int) motionEvent.getY());
                                                                                                                                            settingsFragment2.f2634W.f2715u.setText("\u5f53\u524d\u9009\u62e9\u76d1\u6d4b\u5c4f\u5e55\u5750\u6807: (" + x3 + ", " + y3 + ")");
                                                                                                                                            settingsFragment2.f2637Z.setText("\u9009\u62e9\u5750\u6807\uff0c3\u79d2\u540e\u81ea\u52a8\u786e\u8ba4: (" + x3 + ", " + y3 + ")");
                                                                                                                                            FrameLayout frameLayout2 = settingsFragment2.f2635X;
                                                                                                                                            if (frameLayout2 != null) {
                                                                                                                                                settingsFragment2.f2634W.f2713s.removeView(frameLayout2);
                                                                                                                                            }
                                                                                                                                            FrameLayout frameLayout3 = new FrameLayout(abstractActivityC1471i2);
                                                                                                                                            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(40, 40);
                                                                                                                                            layoutParams2.leftMargin = x2 - 20;
                                                                                                                                            layoutParams2.topMargin = y2 - 20;
                                                                                                                                            frameLayout3.setLayoutParams(layoutParams2);
                                                                                                                                            View view4 = new View(abstractActivityC1471i2);
                                                                                                                                            view4.setBackgroundColor(SupportMenu.CATEGORY_MASK);
                                                                                                                                            FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-1, 4);
                                                                                                                                            layoutParams3.gravity = 16;
                                                                                                                                            frameLayout3.addView(view4, layoutParams3);
                                                                                                                                            View view5 = new View(abstractActivityC1471i2);
                                                                                                                                            view5.setBackgroundColor(SupportMenu.CATEGORY_MASK);
                                                                                                                                            FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(4, -1);
                                                                                                                                            layoutParams4.gravity = 1;
                                                                                                                                            frameLayout3.addView(view5, layoutParams4);
                                                                                                                                            settingsFragment2.f2634W.f2713s.addView(frameLayout3);
                                                                                                                                            settingsFragment2.f2635X = frameLayout3;
                                                                                                                                            RunnableC1509c runnableC1509c = settingsFragment2.f2640c0;
                                                                                                                                            Handler handler = settingsFragment2.f2639b0;
                                                                                                                                            if (runnableC1509c != null) {
                                                                                                                                                handler.removeCallbacks(runnableC1509c);
                                                                                                                                            }
                                                                                                                                            ?? r12 = new Runnable() { // from class: h1.c
                                                                                                                                                @Override // java.lang.Runnable
                                                                                                                                                public final void run() {
                                                                                                                                                    SettingsFragment settingsFragment3 = SettingsFragment.this;
                                                                                                                                                    FrameLayout frameLayout4 = settingsFragment3.f2635X;
                                                                                                                                                    if (frameLayout4 != null) {
                                                                                                                                                        settingsFragment3.f2634W.f2713s.removeView(frameLayout4);
                                                                                                                                                        settingsFragment3.f2635X = null;
                                                                                                                                                    }
                                                                                                                                                    View view6 = settingsFragment3.f2636Y;
                                                                                                                                                    if (view6 != null) {
                                                                                                                                                        settingsFragment3.f2634W.f2713s.removeView(view6);
                                                                                                                                                        settingsFragment3.f2636Y = null;
                                                                                                                                                    }
                                                                                                                                                    TextView textView4 = settingsFragment3.f2637Z;
                                                                                                                                                    if (textView4 != null) {
                                                                                                                                                        settingsFragment3.f2634W.f2713s.removeView(textView4);
                                                                                                                                                        settingsFragment3.f2637Z = null;
                                                                                                                                                    }
                                                                                                                                                    settingsFragment3.f2634W.f2713s.setVisibility(8);
                                                                                                                                                    SharedPreferences.Editor edit = settingsFragment3.f2638a0.f2586a.edit();
                                                                                                                                                    int i32 = x3;
                                                                                                                                                    edit.putInt("MonitorTargetX", i32).apply();
                                                                                                                                                    SharedPreferences.Editor edit2 = settingsFragment3.f2638a0.f2586a.edit();
                                                                                                                                                    int i4 = y3;
                                                                                                                                                    edit2.putInt("MonitorTargetY", i4).apply();
                                                                                                                                                    AbstractC0330t.m854f(1, App.f2584k.getApplicationContext(), "\u8bbe\u7f6e\u5750\u6807\u6210\u529f(" + i32 + "," + i4 + ")");
                                                                                                                                                }
                                                                                                                                            };
                                                                                                                                            settingsFragment2.f2640c0 = r12;
                                                                                                                                            handler.postDelayed(r12, 3000L);
                                                                                                                                        }
                                                                                                                                    } catch (Exception unused) {
                                                                                                                                    }
                                                                                                                                    return true;
                                                                                                                                }
                                                                                                                            });
                                                                                                                            break;
                                                                                                                        }
                                                                                                                        break;
                                                                                                                    default:
                                                                                                                        SettingsFragment settingsFragment2 = this.f3141b;
                                                                                                                        settingsFragment2.getClass();
                                                                                                                        try {
                                                                                                                            long parseLong = Long.parseLong(settingsFragment2.f2634W.f2695a.getText().toString().trim());
                                                                                                                            long parseLong2 = Long.parseLong(settingsFragment2.f2634W.f2702h.getText().toString().trim());
                                                                                                                            long parseLong3 = Long.parseLong(settingsFragment2.f2634W.f2696b.getText().toString().trim());
                                                                                                                            long parseLong4 = Long.parseLong(settingsFragment2.f2634W.f2703i.getText().toString().trim());
                                                                                                                            long parseLong5 = Long.parseLong(settingsFragment2.f2634W.f2697c.getText().toString().trim());
                                                                                                                            long parseLong6 = Long.parseLong(settingsFragment2.f2634W.f2704j.getText().toString().trim());
                                                                                                                            long parseLong7 = Long.parseLong(settingsFragment2.f2634W.f2698d.getText().toString().trim());
                                                                                                                            long parseLong8 = Long.parseLong(settingsFragment2.f2634W.f2705k.getText().toString().trim());
                                                                                                                            long parseLong9 = Long.parseLong(settingsFragment2.f2634W.f2699e.getText().toString().trim());
                                                                                                                            long parseLong10 = Long.parseLong(settingsFragment2.f2634W.f2706l.getText().toString().trim());
                                                                                                                            long parseLong11 = Long.parseLong(settingsFragment2.f2634W.f2700f.getText().toString().trim());
                                                                                                                            long parseLong12 = Long.parseLong(settingsFragment2.f2634W.f2707m.getText().toString().trim());
                                                                                                                            long parseLong13 = Long.parseLong(settingsFragment2.f2634W.f2701g.getText().toString().trim());
                                                                                                                            long parseLong14 = Long.parseLong(settingsFragment2.f2634W.f2708n.getText().toString().trim());
                                                                                                                            if (parseLong2 >= parseLong && parseLong4 >= parseLong3 && parseLong6 >= parseLong5 && parseLong8 >= parseLong7 && parseLong10 >= parseLong9 && parseLong12 >= parseLong11 && parseLong14 >= parseLong13) {
                                                                                                                                settingsFragment2.f2638a0.m1781f(1, parseLong);
                                                                                                                                settingsFragment2.f2638a0.m1780e(1, parseLong2);
                                                                                                                                settingsFragment2.f2638a0.m1781f(2, parseLong3);
                                                                                                                                settingsFragment2.f2638a0.m1780e(2, parseLong4);
                                                                                                                                settingsFragment2.f2638a0.m1781f(3, parseLong5);
                                                                                                                                settingsFragment2.f2638a0.m1780e(3, parseLong6);
                                                                                                                                settingsFragment2.f2638a0.m1781f(4, parseLong7);
                                                                                                                                settingsFragment2.f2638a0.m1780e(4, parseLong8);
                                                                                                                                settingsFragment2.f2638a0.m1781f(5, parseLong9);
                                                                                                                                settingsFragment2.f2638a0.m1780e(5, parseLong10);
                                                                                                                                settingsFragment2.f2638a0.m1781f(6, parseLong11);
                                                                                                                                settingsFragment2.f2638a0.m1780e(6, parseLong12);
                                                                                                                                settingsFragment2.f2638a0.m1781f(8, parseLong13);
                                                                                                                                settingsFragment2.f2638a0.m1780e(8, parseLong14);
                                                                                                                                AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                break;
                                                                                                                            }
                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u5931\u8d25\n\u7ed3\u675f\u65f6\u95f4\u4e0d\u80fd\u5927\u4e8e\u5f00\u59cb\u65f6\u95f4");
                                                                                                                        } catch (NumberFormatException unused) {
                                                                                                                            AbstractC0330t.m854f(1, App.f2584k.getApplicationContext(), "\u4fdd\u5b58\u5931\u8d25,\u8f93\u5165\u6570\u636e\u6709\u8bef");
                                                                                                                            return;
                                                                                                                        }
                                                                                                                        break;
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                        this.f2634W.f2695a.setText(String.valueOf(this.f2638a0.m1777b(1, 0L)));
                                                                                                        this.f2634W.f2702h.setText(String.valueOf(this.f2638a0.m1776a(1, 1170000L)));
                                                                                                        this.f2634W.f2696b.setText(String.valueOf(this.f2638a0.m1777b(2, 5000000L)));
                                                                                                        this.f2634W.f2703i.setText(String.valueOf(this.f2638a0.m1776a(2, 5900000L)));
                                                                                                        this.f2634W.f2697c.setText(String.valueOf(this.f2638a0.m1777b(3, 2000000L)));
                                                                                                        this.f2634W.f2704j.setText(String.valueOf(this.f2638a0.m1776a(3, 3200000L)));
                                                                                                        this.f2634W.f2698d.setText(String.valueOf(this.f2638a0.m1777b(4, 3200000L)));
                                                                                                        this.f2634W.f2705k.setText(String.valueOf(this.f2638a0.m1776a(4, 4000000L)));
                                                                                                        this.f2634W.f2699e.setText(String.valueOf(this.f2638a0.m1777b(5, 4000000L)));
                                                                                                        this.f2634W.f2706l.setText(String.valueOf(this.f2638a0.m1776a(5, 4000000L)));
                                                                                                        this.f2634W.f2700f.setText(String.valueOf(this.f2638a0.m1777b(6, 4000000L)));
                                                                                                        this.f2634W.f2707m.setText(String.valueOf(this.f2638a0.m1776a(6, 5000000L)));
                                                                                                        this.f2634W.f2701g.setText(String.valueOf(this.f2638a0.m1777b(8, 5600000L)));
                                                                                                        this.f2634W.f2708n.setText(String.valueOf(this.f2638a0.m1776a(8, 6800000L)));
                                                                                                        this.f2634W.f2712r.setOnCheckedChangeListener(new C1440b(2, this));
                                                                                                        return constraintLayout;
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(inflate.getResources().getResourceName(i)));
    }

    @Override // p045Y.AbstractComponentCallbacksC0442x
    /* renamed from: v */
    public final void mo1111v() {
        this.f1339D = true;
        this.f2634W = null;
    }
}
