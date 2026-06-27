package p059d1;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.xiaomi.vlive.FloatService;

/* renamed from: d1.c */
/* loaded from: classes.dex */
public final class ViewOnTouchListenerC1388c implements View.OnTouchListener {

    /* renamed from: a */
    public final /* synthetic */ int f2648a;

    /* renamed from: b */
    public int f2649b;

    /* renamed from: c */
    public int f2650c;

    /* renamed from: d */
    public int f2651d;

    /* renamed from: e */
    public int f2652e;

    /* renamed from: f */
    public boolean f2653f = false;

    /* renamed from: g */
    public final /* synthetic */ Object f2654g;

    public /* synthetic */ ViewOnTouchListenerC1388c(int i, Object obj) {
        this.f2648a = i;
        this.f2654g = obj;
    }

    @Override // android.view.View.OnTouchListener
    public final boolean onTouch(View view, MotionEvent motionEvent) {
        switch (this.f2648a) {
            case 0:
                int rawX = (int) motionEvent.getRawX();
                int rawY = (int) motionEvent.getRawY();
                int action = motionEvent.getAction();
                FloatService floatService = (FloatService) this.f2654g;
                if (action == 0) {
                    this.f2653f = false;
                    this.f2649b = rawX;
                    this.f2650c = rawY;
                    WindowManager.LayoutParams layoutParams = floatService.f2599c;
                    this.f2651d = layoutParams.x;
                    this.f2652e = layoutParams.y;
                    break;
                } else if (action == 1) {
                    break;
                } else if (action == 2) {
                    int i = rawX - this.f2649b;
                    int i2 = rawY - this.f2650c;
                    if (Math.abs(i) > 5 || Math.abs(i2) > 5) {
                        this.f2653f = true;
                        WindowManager.LayoutParams layoutParams2 = floatService.f2599c;
                        layoutParams2.x = this.f2651d + i;
                        layoutParams2.y = this.f2652e + i2;
                        floatService.f2597a.updateViewLayout(floatService.f2598b, layoutParams2);
                    }
                    break;
                }
                break;
            default:
                int rawX2 = (int) motionEvent.getRawX();
                int rawY2 = (int) motionEvent.getRawY();
                int action2 = motionEvent.getAction();
                C1390e c1390e = (C1390e) this.f2654g;
                if (action2 == 0) {
                    this.f2653f = false;
                    this.f2649b = rawX2;
                    this.f2650c = rawY2;
                    WindowManager.LayoutParams layoutParams3 = c1390e.f2659c;
                    this.f2651d = layoutParams3.x;
                    this.f2652e = layoutParams3.y;
                    break;
                } else if (action2 == 1) {
                    break;
                } else if (action2 == 2) {
                    int i3 = rawX2 - this.f2649b;
                    int i4 = rawY2 - this.f2650c;
                    if (Math.abs(i3) > 5 || Math.abs(i4) > 5) {
                        this.f2653f = true;
                        WindowManager.LayoutParams layoutParams4 = c1390e.f2659c;
                        layoutParams4.x = this.f2651d + i3;
                        layoutParams4.y = this.f2652e + i4;
                        c1390e.f2657a.updateViewLayout(c1390e.f2658b, layoutParams4);
                    }
                    break;
                }
                break;
        }
        return false;
    }
}
