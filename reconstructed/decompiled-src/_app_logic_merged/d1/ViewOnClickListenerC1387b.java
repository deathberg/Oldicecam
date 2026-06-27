package p059d1;

import android.os.RemoteException;
import android.view.View;
import com.xiaomi.vlive.FloatService;
import java.io.File;
import p037U.AbstractC0330t;
import p048Z0.AbstractC0458i;

/* renamed from: d1.b */
/* loaded from: classes.dex */
public final /* synthetic */ class ViewOnClickListenerC1387b implements View.OnClickListener {

    /* renamed from: a */
    public final /* synthetic */ int f2647a;

    public /* synthetic */ ViewOnClickListenerC1387b(int i) {
        this.f2647a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        switch (this.f2647a) {
            case 0:
                int i = FloatService.f2596g;
                AbstractC0458i.m1179k0("\u64ad\u653e");
                if (!AbstractC0330t.m844T().booleanValue()) {
                    AbstractC0330t.m856g("\u64ad\u653e\u5931\u8d25");
                    break;
                }
                break;
            case 1:
                if (!new File("/sdcard/Movies/1.mp4").exists()) {
                    AbstractC0458i.m1179k0("/sdcard/Movies/1.mp4 \u4e0d\u5b58\u5728");
                    break;
                } else {
                    AbstractC0330t.m849a0("/sdcard/Movies/1.mp4", 1);
                    break;
                }
            case 2:
                if (!new File("/sdcard/Movies/2.mp4").exists()) {
                    AbstractC0458i.m1179k0("/sdcard/Movies/2.mp4 \u4e0d\u5b58\u5728");
                    break;
                } else {
                    AbstractC0330t.m849a0("/sdcard/Movies/2.mp4", 1);
                    break;
                }
            case 3:
                if (!new File("/sdcard/Movies/3.mp4").exists()) {
                    AbstractC0458i.m1179k0("/sdcard/Movies/3.mp4 \u4e0d\u5b58\u5728");
                    break;
                } else {
                    AbstractC0330t.m849a0("/sdcard/Movies/3.mp4", 1);
                    break;
                }
            case 4:
                try {
                    ((C1391f) AbstractC0330t.m837E()).m1799l();
                    break;
                } catch (RemoteException | Exception unused) {
                    return;
                }
            default:
                if (!AbstractC0330t.m844T().booleanValue()) {
                    AbstractC0458i.m1179k0("\u64ad\u653e\u5931\u8d25");
                    break;
                }
                break;
        }
    }
}
