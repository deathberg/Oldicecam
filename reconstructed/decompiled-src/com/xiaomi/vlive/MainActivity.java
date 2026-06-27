package com.xiaomi.vlive;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.potplayer.music.R;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import p000A.AbstractC0001b;
import p004C.C0033j;
import p026O0.C0249i;
import p029Q.C0257b;
import p064f0.AbstractC1434v;
import p064f0.C1413a;
import p064f0.C1421i;
import p064f0.C1437y;
import p066g.AbstractActivityC1471i;
import p066g.C1469g;
import p066g.C1470h;
import p071i0.C1520j;
import p075j1.C1553h;
import p075j1.C1562q;
import p077k0.C1603a;
import p081l.AbstractC1807z;
import p099q1.AbstractC1952d;
import p115w1.AbstractC2077f;
import p115w1.C2074c;
import p115w1.C2079h;
import xyz.vcxm.vmxplay.patch.PreviewPatcher;

/* loaded from: classes.dex */
public class MainActivity extends AbstractActivityC1471i {

    /* renamed from: x */
    public C0249i f2603x;

    public MainActivity() {
        ((C0033j) this.f1460e.f57c).m127I("androidx:appcompat", new C1469g(this));
        m1208g(new C1470h(this));
    }

    @Override // p066g.AbstractActivityC1471i, p049a.AbstractActivityC0469j, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        View inflate = getLayoutInflater().inflate(R.layout.activity_main, (ViewGroup) null, false);
        ConstraintLayout constraintLayout = (ConstraintLayout) inflate;
        BottomNavigationView bottomNavigationView = (BottomNavigationView) AbstractC1807z.m2262i(inflate, R.id.nav_view);
        if (bottomNavigationView == null) {
            throw new NullPointerException("Missing required view with ID: ".concat(inflate.getResources().getResourceName(R.id.nav_view)));
        }
        this.f2603x = new C0249i(constraintLayout, bottomNavigationView);
        setContentView(constraintLayout);
        PreviewPatcher.attachToPreviewButton(this);
        int[] iArr = {R.id.navigation_home, R.id.navigation_controller, R.id.navigation_settings};
        HashSet hashSet = new HashSet();
        for (int i = 0; i < 3; i++) {
            hashSet.add(Integer.valueOf(iArr[i]));
        }
        View view = (View) AbstractC0001b.m3a(this, R.id.nav_host_fragment_activity_main);
        AbstractC1952d.m2507d(view, "requireViewById(...)");
        C2074c c2074c = new C2074c(new C1562q(1, new C2079h(AbstractC2077f.m2785y(view, new C1413a(5)), new C1413a(6), 1)));
        C1437y c1437y = (C1437y) (c2074c.hasNext() ? c2074c.next() : null);
        if (c1437y == null) {
            throw new IllegalStateException("Activity " + this + " does not have a NavController set on 2131231077");
        }
        BottomNavigationView bottomNavigationView2 = this.f2603x.f634a;
        bottomNavigationView2.setOnItemSelectedListener(new C0257b(c1437y));
        C1603a c1603a = new C1603a(new WeakReference(bottomNavigationView2), c1437y);
        C1520j c1520j = c1437y.f2870b;
        c1520j.getClass();
        c1520j.f3194o.add(c1603a);
        C1553h c1553h = c1520j.f3185f;
        if (c1553h.isEmpty()) {
            return;
        }
        C1421i c1421i = (C1421i) c1553h.m2002e();
        AbstractC1434v abstractC1434v = c1421i.f2801b;
        c1421i.f2807h.m1954a();
        c1603a.m2091a(c1520j.f3180a, abstractC1434v);
    }
}
