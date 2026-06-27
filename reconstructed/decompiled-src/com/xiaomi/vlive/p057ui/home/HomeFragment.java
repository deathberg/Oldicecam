package com.xiaomi.vlive.p057ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.C0838V;
import androidx.lifecycle.InterfaceC0818B;
import androidx.lifecycle.InterfaceC0836T;
import com.potplayer.music.R;
import com.xiaomi.vlive.App;
import com.xiaomi.vlive.p057ui.home.HomeFragment;
import java.io.File;
import p004C.C0033j;
import p037U.AbstractC0330t;
import p037U.C0332v;
import p045Y.AbstractComponentCallbacksC0442x;
import p045Y.C0389B;
import p055c0.C1028e;
import p068g1.C1492c;
import p081l.AbstractC1807z;
import p099q1.AbstractC1952d;
import p099q1.AbstractC1957i;
import p099q1.C1950b;

/* loaded from: classes.dex */
public class HomeFragment extends AbstractComponentCallbacksC0442x {

    /* renamed from: W */
    public C0033j f2633W;

    @Override // p045Y.AbstractComponentCallbacksC0442x
    /* renamed from: u */
    public final View mo1136u(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        C0838V mo979c = mo979c();
        InterfaceC0836T m1127i = m1127i();
        C1028e mo1074a = mo1074a();
        AbstractC1952d.m2508e(m1127i, "factory");
        C0332v c0332v = new C0332v(mo979c, m1127i, mo1074a);
        C1950b m2512a = AbstractC1957i.m2512a(C1492c.class);
        String m865s = AbstractC0330t.m865s(m2512a);
        if (m865s == null) {
            throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
        }
        View inflate = layoutInflater.inflate(R.layout.fragment_home, viewGroup, false);
        int i = R.id.contact;
        TextView textView = (TextView) AbstractC1807z.m2262i(inflate, R.id.contact);
        if (textView != null) {
            i = R.id.notice;
            TextView textView2 = (TextView) AbstractC1807z.m2262i(inflate, R.id.notice);
            if (textView2 != null) {
                i = R.id.textView;
                if (((TextView) AbstractC1807z.m2262i(inflate, R.id.textView)) != null) {
                    i = R.id.textView3;
                    if (((TextView) AbstractC1807z.m2262i(inflate, R.id.textView3)) != null) {
                        ConstraintLayout constraintLayout = (ConstraintLayout) inflate;
                        this.f2633W = new C0033j(constraintLayout, textView, textView2);
                        App app = (App) m1119G().getApplication();
                        final int i2 = 0;
                        app.f2593h.m1364d(m1130l(), new InterfaceC0818B(this) { // from class: g1.a

                            /* renamed from: b */
                            public final /* synthetic */ HomeFragment f3103b;

                            {
                                this.f3103b = this;
                            }

                            @Override // androidx.lifecycle.InterfaceC0818B
                            /* renamed from: a */
                            public final void mo1099a(Object obj) {
                                String str = (String) obj;
                                switch (i2) {
                                    case 0:
                                        HomeFragment homeFragment = this.f3103b;
                                        ((TextView) homeFragment.f2633W.f56b).setText(Html.fromHtml(str, 0));
                                        ((TextView) homeFragment.f2633W.f56b).setMovementMethod(LinkMovementMethod.getInstance());
                                        break;
                                    default:
                                        HomeFragment homeFragment2 = this.f3103b;
                                        ((TextView) homeFragment2.f2633W.f57c).setText(Html.fromHtml(str, 0));
                                        ((TextView) homeFragment2.f2633W.f57c).setMovementMethod(LinkMovementMethod.getInstance());
                                        break;
                                }
                            }
                        });
                        final int i3 = 1;
                        app.f2594i.m1364d(m1130l(), new InterfaceC0818B(this) { // from class: g1.a

                            /* renamed from: b */
                            public final /* synthetic */ HomeFragment f3103b;

                            {
                                this.f3103b = this;
                            }

                            @Override // androidx.lifecycle.InterfaceC0818B
                            /* renamed from: a */
                            public final void mo1099a(Object obj) {
                                String str = (String) obj;
                                switch (i3) {
                                    case 0:
                                        HomeFragment homeFragment = this.f3103b;
                                        ((TextView) homeFragment.f2633W.f56b).setText(Html.fromHtml(str, 0));
                                        ((TextView) homeFragment.f2633W.f56b).setMovementMethod(LinkMovementMethod.getInstance());
                                        break;
                                    default:
                                        HomeFragment homeFragment2 = this.f3103b;
                                        ((TextView) homeFragment2.f2633W.f57c).setText(Html.fromHtml(str, 0));
                                        ((TextView) homeFragment2.f2633W.f57c).setMovementMethod(LinkMovementMethod.getInstance());
                                        break;
                                }
                            }
                        });
                        if (!new File("/data/camera/libshadowhook.so").exists() && !new File("/data/samera/libshadowhook.so").exists()) {
                            return constraintLayout;
                        }
                        C0389B c0389b = this.f1376t;
                        final int i4 = 0;
                        AlertDialog.Builder positiveButton = new AlertDialog.Builder(c0389b == null ? null : c0389b.f1094e).setTitle("\u8b66\u544a").setMessage("\u68c0\u6d4b\u5230\u7cfb\u7edf\u5b58\u5728\u5176\u4ed6\u865a\u62df\u76f8\u673a\n\u5378\u8f7d\u540e\u4e5f\u4f1a\u6709\u6b8b\u7559\u5bb9\u6613\u89e6\u53d1\u98ce\u63a7\n\u662f\u5426\u9700\u8981\u6e05\u7406\u6b8b\u7559?").setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener() { // from class: g1.b
                            @Override // android.content.DialogInterface.OnClickListener
                            public final void onClick(DialogInterface dialogInterface, int i5) {
                                switch (i4) {
                                    case 0:
                                        AbstractC0330t.m863q("chattr -i /data/camera");
                                        AbstractC0330t.m863q("rm -r /data/camera");
                                        AbstractC0330t.m863q("rm -r /data/samera");
                                        AbstractC0330t.m856g("\u5df2\u5220\u9664");
                                        break;
                                    default:
                                        dialogInterface.dismiss();
                                        break;
                                }
                            }
                        });
                        final int i5 = 1;
                        positiveButton.setNegativeButton("\u53d6\u6d88", new DialogInterface.OnClickListener() { // from class: g1.b
                            @Override // android.content.DialogInterface.OnClickListener
                            public final void onClick(DialogInterface dialogInterface, int i52) {
                                switch (i5) {
                                    case 0:
                                        AbstractC0330t.m863q("chattr -i /data/camera");
                                        AbstractC0330t.m863q("rm -r /data/camera");
                                        AbstractC0330t.m863q("rm -r /data/samera");
                                        AbstractC0330t.m856g("\u5df2\u5220\u9664");
                                        break;
                                    default:
                                        dialogInterface.dismiss();
                                        break;
                                }
                            }
                        }).show();
                        return constraintLayout;
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
        this.f2633W = null;
    }
}
