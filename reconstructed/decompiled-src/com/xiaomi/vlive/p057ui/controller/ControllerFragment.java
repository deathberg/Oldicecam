package com.xiaomi.vlive.p057ui.controller;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.C0817A;
import androidx.lifecycle.C0838V;
import androidx.lifecycle.InterfaceC0818B;
import androidx.lifecycle.InterfaceC0836T;
import com.kusu.loadingbutton.LoadingButton;
import com.potplayer.music.R;
import com.xiaomi.vlive.App;
import com.xiaomi.vlive.p057ui.controller.ControllerFragment;
import java.nio.ByteBuffer;
import kotlin.UByte;
import okhttp3.HttpUrl;
import p004C.RunnableC0038o;
import p007D0.RunnableC0063i;
import p011F0.RunnableC0078b;
import p020L.C0164g;
import p037U.AbstractC0330t;
import p037U.C0332v;
import p045Y.AbstractComponentCallbacksC0442x;
import p045Y.C0400M;
import p045Y.C0435q;
import p048Z0.AbstractC0458i;
import p055c0.C1028e;
import p059d1.C1391f;
import p062e1.C1397a;
import p065f1.C1439a;
import p065f1.C1440b;
import p065f1.C1442d;
import p065f1.C1446h;
import p065f1.C1448j;
import p065f1.C1450l;
import p065f1.TextureViewSurfaceTextureListenerC1449k;
import p066g.AbstractActivityC1471i;
import p081l.AbstractC1807z;
import p099q1.AbstractC1952d;
import p099q1.AbstractC1957i;
import p099q1.C1950b;

/* loaded from: classes.dex */
public class ControllerFragment extends AbstractComponentCallbacksC0442x {

    /* renamed from: W */
    public C1397a f2604W;

    /* renamed from: X */
    public App f2605X;

    /* renamed from: Y */
    public AbstractActivityC1471i f2606Y;

    /* renamed from: Z */
    public C0435q f2607Z;

    /* renamed from: a0 */
    public C0435q f2608a0;

    /* renamed from: b0 */
    public CameraDevice f2609b0;

    /* renamed from: c0 */
    public CameraCaptureSession f2610c0;

    /* renamed from: d0 */
    public CameraManager f2611d0;

    /* renamed from: e0 */
    public Handler f2612e0;

    /* renamed from: f0 */
    public HandlerThread f2613f0;

    /* renamed from: g0 */
    public String f2614g0;

    /* renamed from: h0 */
    public C1442d f2615h0;

    /* renamed from: i0 */
    public C0435q f2616i0;

    /* renamed from: j0 */
    public MediaProjectionManager f2617j0;

    /* renamed from: k0 */
    public MediaProjection f2618k0;

    /* renamed from: l0 */
    public VirtualDisplay f2619l0;

    /* renamed from: m0 */
    public ImageReader f2620m0;

    /* renamed from: n0 */
    public Handler f2621n0;

    /* renamed from: o0 */
    public int f2622o0;

    /* renamed from: p0 */
    public int f2623p0;

    /* renamed from: q0 */
    public int f2624q0;

    /* renamed from: r0 */
    public int f2625r0 = 160;

    /* renamed from: s0 */
    public int f2626s0 = 160;

    /* renamed from: t0 */
    public int f2627t0 = 0;

    /* renamed from: u0 */
    public int f2628u0 = 0;

    /* renamed from: v0 */
    public Handler f2629v0 = null;

    /* renamed from: w0 */
    public RunnableC0063i f2630w0 = null;

    /* renamed from: x0 */
    public final TextureViewSurfaceTextureListenerC1449k f2631x0 = new TextureViewSurfaceTextureListenerC1449k(this);

    /* renamed from: y0 */
    public final C0435q f2632y0 = m1118F(new C0400M(3), new C1439a(this, 0));

    /* renamed from: L */
    public static int m1784L(Image image, int i, int i2) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (i >= 0 && i < width && i2 >= 0 && i2 < height) {
            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            int pixelStride = (i * plane.getPixelStride()) + (plane.getRowStride() * i2);
            if (pixelStride + 4 <= buffer.capacity()) {
                byte[] bArr = new byte[4];
                buffer.position(pixelStride);
                buffer.get(bArr, 0, 4);
                int i3 = bArr[0] & UByte.MAX_VALUE;
                int i4 = bArr[1] & UByte.MAX_VALUE;
                int i5 = bArr[2] & UByte.MAX_VALUE;
                int i6 = bArr[3] & UByte.MAX_VALUE;
                float[] fArr = new float[3];
                Color.RGBToHSV(i3, i4, i5, fArr);
                float f2 = fArr[1];
                boolean z2 = fArr[2] > 0.7f;
                boolean z3 = f2 > 0.6f;
                if (z2 && z3) {
                    return (i3 << 16) | (i6 << 24) | (i4 << 8) | i5;
                }
            }
        }
        return 0;
    }

    /* renamed from: M */
    public final void m1785M() {
        AbstractC0330t.m844T();
        try {
            String[] cameraIdList = this.f2611d0.getCameraIdList();
            int length = cameraIdList.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String str = cameraIdList[i];
                Integer num = (Integer) this.f2611d0.getCameraCharacteristics(str).get(CameraCharacteristics.LENS_FACING);
                if (num != null && num.intValue() == 0) {
                    this.f2614g0 = str;
                    break;
                }
                i++;
            }
            if (this.f2614g0 != null) {
                if (this.f2604W.f2672d.isAvailable()) {
                    m1786N();
                } else {
                    this.f2604W.f2672d.setSurfaceTextureListener(this.f2631x0);
                }
            }
        } catch (CameraAccessException e2) {
            e2.printStackTrace();
        }
    }

    /* renamed from: N */
    public final void m1786N() {
        try {
            this.f2611d0.openCamera(this.f2614g0, new C1448j(this), this.f2612e0);
        } catch (CameraAccessException | SecurityException e2) {
            e2.printStackTrace();
        }
    }

    @Override // p045Y.AbstractComponentCallbacksC0442x
    /* renamed from: u */
    public final View mo1136u(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        this.f2605X = (App) m1119G().getApplication();
        this.f2606Y = m1119G();
        C0838V mo979c = mo979c();
        InterfaceC0836T m1127i = m1127i();
        C1028e mo1074a = mo1074a();
        AbstractC1952d.m2508e(m1127i, "factory");
        C0332v c0332v = new C0332v(mo979c, m1127i, mo1074a);
        C1950b m2512a = AbstractC1957i.m2512a(C1450l.class);
        String m865s = AbstractC0330t.m865s(m2512a);
        if (m865s == null) {
            throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
        }
        View inflate = layoutInflater.inflate(R.layout.fragment_controller, viewGroup, false);
        int i = R.id.auto_color;
        Switch r7 = (Switch) AbstractC1807z.m2262i(inflate, R.id.auto_color);
        if (r7 != null) {
            i = R.id.auto_rotate;
            Switch r8 = (Switch) AbstractC1807z.m2262i(inflate, R.id.auto_rotate);
            if (r8 != null) {
                i = R.id.camera_preview;
                Button button = (Button) AbstractC1807z.m2262i(inflate, R.id.camera_preview);
                if (button != null) {
                    i = R.id.cameraTextureView;
                    TextureView textureView = (TextureView) AbstractC1807z.m2262i(inflate, R.id.cameraTextureView);
                    if (textureView != null) {
                        i = R.id.controls;
                        LinearLayout linearLayout = (LinearLayout) AbstractC1807z.m2262i(inflate, R.id.controls);
                        if (linearLayout != null) {
                            i = R.id.controls1;
                            if (((LinearLayout) AbstractC1807z.m2262i(inflate, R.id.controls1)) != null) {
                                i = R.id.expiration_time;
                                if (((TextView) AbstractC1807z.m2262i(inflate, R.id.expiration_time)) != null) {
                                    i = R.id.file_type_mp4;
                                    RadioButton radioButton = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.file_type_mp4);
                                    if (radioButton != null) {
                                        i = R.id.file_type_rtmp;
                                        RadioButton radioButton2 = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.file_type_rtmp);
                                        if (radioButton2 != null) {
                                            i = R.id.filetypetxt;
                                            TextView textView = (TextView) AbstractC1807z.m2262i(inflate, R.id.filetypetxt);
                                            if (textView != null) {
                                                i = R.id.logmsg;
                                                TextView textView2 = (TextView) AbstractC1807z.m2262i(inflate, R.id.logmsg);
                                                if (textView2 != null) {
                                                    i = R.id.play_loop;
                                                    Switch r16 = (Switch) AbstractC1807z.m2262i(inflate, R.id.play_loop);
                                                    if (r16 != null) {
                                                        i = R.id.playfile;
                                                        EditText editText = (EditText) AbstractC1807z.m2262i(inflate, R.id.playfile);
                                                        if (editText != null) {
                                                            i = R.id.preview_overlay;
                                                            FrameLayout frameLayout = (FrameLayout) AbstractC1807z.m2262i(inflate, R.id.preview_overlay);
                                                            if (frameLayout != null) {
                                                                i = R.id.radioGroup_file_type;
                                                                RadioGroup radioGroup = (RadioGroup) AbstractC1807z.m2262i(inflate, R.id.radioGroup_file_type);
                                                                if (radioGroup != null) {
                                                                    i = R.id.seekBar;
                                                                    SeekBar seekBar = (SeekBar) AbstractC1807z.m2262i(inflate, R.id.seekBar);
                                                                    if (seekBar != null) {
                                                                        i = R.id.seekBar2;
                                                                        SeekBar seekBar2 = (SeekBar) AbstractC1807z.m2262i(inflate, R.id.seekBar2);
                                                                        if (seekBar2 != null) {
                                                                            i = R.id.seekBar3;
                                                                            SeekBar seekBar3 = (SeekBar) AbstractC1807z.m2262i(inflate, R.id.seekBar3);
                                                                            if (seekBar3 != null) {
                                                                                i = R.id.seekBar4;
                                                                                SeekBar seekBar4 = (SeekBar) AbstractC1807z.m2262i(inflate, R.id.seekBar4);
                                                                                if (seekBar4 != null) {
                                                                                    i = R.id.selectsave;
                                                                                    LoadingButton loadingButton = (LoadingButton) AbstractC1807z.m2262i(inflate, R.id.selectsave);
                                                                                    if (loadingButton != null) {
                                                                                        i = R.id.ssmode;
                                                                                        RadioGroup radioGroup2 = (RadioGroup) AbstractC1807z.m2262i(inflate, R.id.ssmode);
                                                                                        if (radioGroup2 != null) {
                                                                                            i = R.id.ssmode1;
                                                                                            RadioButton radioButton3 = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.ssmode1);
                                                                                            if (radioButton3 != null) {
                                                                                                RadioButton radioButton4 = (RadioButton) AbstractC1807z.m2262i(inflate, R.id.ssmode2);
                                                                                                if (radioButton4 != null) {
                                                                                                    int i2 = R.id.start_hook;
                                                                                                    LoadingButton loadingButton2 = (LoadingButton) AbstractC1807z.m2262i(inflate, R.id.start_hook);
                                                                                                    if (loadingButton2 != null) {
                                                                                                        i2 = R.id.switchfloatingwindow;
                                                                                                        Switch r29 = (Switch) AbstractC1807z.m2262i(inflate, R.id.switchfloatingwindow);
                                                                                                        if (r29 != null) {
                                                                                                            i2 = R.id.test_color;
                                                                                                            Button button2 = (Button) AbstractC1807z.m2262i(inflate, R.id.test_color);
                                                                                                            if (button2 != null) {
                                                                                                                i2 = R.id.valueText;
                                                                                                                TextView textView3 = (TextView) AbstractC1807z.m2262i(inflate, R.id.valueText);
                                                                                                                if (textView3 != null) {
                                                                                                                    i2 = R.id.valueText2;
                                                                                                                    TextView textView4 = (TextView) AbstractC1807z.m2262i(inflate, R.id.valueText2);
                                                                                                                    if (textView4 != null) {
                                                                                                                        i2 = R.id.valueText3;
                                                                                                                        TextView textView5 = (TextView) AbstractC1807z.m2262i(inflate, R.id.valueText3);
                                                                                                                        if (textView5 != null) {
                                                                                                                            i2 = R.id.valueText4;
                                                                                                                            TextView textView6 = (TextView) AbstractC1807z.m2262i(inflate, R.id.valueText4);
                                                                                                                            if (textView6 != null) {
                                                                                                                                i2 = R.id.valueTextmode;
                                                                                                                                if (((TextView) AbstractC1807z.m2262i(inflate, R.id.valueTextmode)) != null) {
                                                                                                                                    ConstraintLayout constraintLayout = (ConstraintLayout) inflate;
                                                                                                                                    this.f2604W = new C1397a(constraintLayout, r7, r8, button, textureView, linearLayout, radioButton, radioButton2, textView, textView2, r16, editText, frameLayout, radioGroup, seekBar, seekBar2, seekBar3, seekBar4, loadingButton, radioGroup2, radioButton3, radioButton4, loadingButton2, r29, button2, textView3, textView4, textView5, textView6);
                                                                                                                                    this.f2607Z = m1118F(new C0400M(2), new C0164g(3));
                                                                                                                                    m1118F(new C0400M(2), new C1439a(this, 3));
                                                                                                                                    this.f2608a0 = m1118F(new C0400M(3), new C1439a(this, 1));
                                                                                                                                    int i3 = this.f2605X.f2586a.getInt("PlayFileType", 1);
                                                                                                                                    if (i3 == 1) {
                                                                                                                                        this.f2604W.f2676h.setText("\u6587\u4ef6\u8def\u5f84:");
                                                                                                                                        this.f2604W.f2686r.setButtonText("\u9009\u62e9\u89c6\u9891");
                                                                                                                                        String string = this.f2605X.f2586a.getString("PlayFileMp4", HttpUrl.FRAGMENT_ENCODE_SET);
                                                                                                                                        if (!string.isEmpty()) {
                                                                                                                                            string = "\u5df2\u8bbe\u7f6eMP4\u89c6\u9891\u6587\u4ef6";
                                                                                                                                        }
                                                                                                                                        this.f2604W.f2679k.setText(string);
                                                                                                                                        C1397a c1397a = this.f2604W;
                                                                                                                                        c1397a.f2681m.check(c1397a.f2674f.getId());
                                                                                                                                    } else if (i3 == 2) {
                                                                                                                                        this.f2604W.f2676h.setText("rtmp\u94fe\u63a5:");
                                                                                                                                        this.f2604W.f2686r.setButtonText("\u4fdd\u5b58\u94fe\u63a5");
                                                                                                                                        this.f2604W.f2679k.setText(this.f2605X.f2586a.getString("PlayRtmpUrl", "rtmp://ns8.indexforce.com/home/mystream"));
                                                                                                                                        C1397a c1397a2 = this.f2604W;
                                                                                                                                        c1397a2.f2681m.check(c1397a2.f2675g.getId());
                                                                                                                                    }
                                                                                                                                    final int i4 = 0;
                                                                                                                                    this.f2604W.f2681m.setOnCheckedChangeListener(new C1440b(i4, this));
                                                                                                                                    this.f2604W.f2686r.setOnClickListener(new View.OnClickListener(this) { // from class: f1.c

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2885b;

                                                                                                                                        {
                                                                                                                                            this.f2885b = this;
                                                                                                                                        }

                                                                                                                                        @Override // android.view.View.OnClickListener
                                                                                                                                        public final void onClick(View view) {
                                                                                                                                            switch (i4) {
                                                                                                                                                case 0:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2885b;
                                                                                                                                                    if (!controllerFragment.f2604W.f2686r.getButtonText().equals("\u4fdd\u5b58\u94fe\u63a5")) {
                                                                                                                                                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                                                                                                                                                        intent.setType("video/*");
                                                                                                                                                        intent.addCategory("android.intent.category.OPENABLE");
                                                                                                                                                        controllerFragment.f2608a0.mo1115a(intent);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        String trim = controllerFragment.f2604W.f2679k.getText().toString().trim();
                                                                                                                                                        controllerFragment.f2605X.f2586a.edit().putString("PlayRtmpUrl", trim).apply();
                                                                                                                                                        Object obj = controllerFragment.f2605X.f2591f.f1855e;
                                                                                                                                                        if (obj == C0817A.f1850j) {
                                                                                                                                                            obj = null;
                                                                                                                                                        }
                                                                                                                                                        if (((Integer) obj).intValue() != 1) {
                                                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            controllerFragment.f2604W.f2686r.setEnabled(false);
                                                                                                                                                            controllerFragment.f2604W.f2686r.m1773e();
                                                                                                                                                            new Thread(new RunnableC0038o(controllerFragment, 1, trim)).start();
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 1:
                                                                                                                                                    ControllerFragment controllerFragment2 = this.f2885b;
                                                                                                                                                    Object obj2 = controllerFragment2.f2605X.f2591f.f1855e;
                                                                                                                                                    if (obj2 == C0817A.f1850j) {
                                                                                                                                                        obj2 = null;
                                                                                                                                                    }
                                                                                                                                                    if (((Integer) obj2).intValue() == 0) {
                                                                                                                                                        App app = controllerFragment2.f2605X;
                                                                                                                                                        if (!(app.f2586a.getInt("PlayFileType", 1) == 1 ? app.f2586a.getString("PlayFileMp4", HttpUrl.FRAGMENT_ENCODE_SET) : app.f2586a.getString("PlayRtmpUrl", "rtmp://ns8.indexforce.com/home/mystream")).isEmpty()) {
                                                                                                                                                            controllerFragment2.f2604W.f2690v.setEnabled(false);
                                                                                                                                                            controllerFragment2.f2604W.f2690v.m1773e();
                                                                                                                                                            new Thread(new RunnableC0078b(14, controllerFragment2)).start();
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            AbstractC0330t.m856g("\u8bf7\u5148\u9009\u62e9\u8981\u64ad\u653e\u7684\u6587\u4ef6 \u6216 rtmp");
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        try {
                                                                                                                                                            ((C1391f) AbstractC0330t.m837E()).m1798k();
                                                                                                                                                            break;
                                                                                                                                                        } catch (RemoteException | Exception unused) {
                                                                                                                                                            return;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 2:
                                                                                                                                                    ControllerFragment controllerFragment3 = this.f2885b;
                                                                                                                                                    controllerFragment3.f2604W.f2680l.setVisibility(0);
                                                                                                                                                    HandlerThread handlerThread = new HandlerThread("CameraBackground");
                                                                                                                                                    controllerFragment3.f2613f0 = handlerThread;
                                                                                                                                                    handlerThread.start();
                                                                                                                                                    controllerFragment3.f2612e0 = new Handler(controllerFragment3.f2613f0.getLooper());
                                                                                                                                                    if (AbstractC0458i.m1176j(controllerFragment3.m1120H(), "android.permission.CAMERA") == 0) {
                                                                                                                                                        controllerFragment3.m1785M();
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment3.f2607Z.mo1115a("android.permission.CAMERA");
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                case 3:
                                                                                                                                                    ControllerFragment controllerFragment4 = this.f2885b;
                                                                                                                                                    CameraCaptureSession cameraCaptureSession = controllerFragment4.f2610c0;
                                                                                                                                                    if (cameraCaptureSession != null) {
                                                                                                                                                        cameraCaptureSession.close();
                                                                                                                                                        controllerFragment4.f2610c0 = null;
                                                                                                                                                    }
                                                                                                                                                    CameraDevice cameraDevice = controllerFragment4.f2609b0;
                                                                                                                                                    if (cameraDevice != null) {
                                                                                                                                                        cameraDevice.close();
                                                                                                                                                        controllerFragment4.f2609b0 = null;
                                                                                                                                                    }
                                                                                                                                                    HandlerThread handlerThread2 = controllerFragment4.f2613f0;
                                                                                                                                                    if (handlerThread2 != null) {
                                                                                                                                                        handlerThread2.quitSafely();
                                                                                                                                                        try {
                                                                                                                                                            controllerFragment4.f2613f0.join();
                                                                                                                                                            controllerFragment4.f2613f0 = null;
                                                                                                                                                            controllerFragment4.f2612e0 = null;
                                                                                                                                                        } catch (InterruptedException e2) {
                                                                                                                                                            e2.printStackTrace();
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    controllerFragment4.f2604W.f2680l.setVisibility(8);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment5 = this.f2885b;
                                                                                                                                                    if (controllerFragment5.f2629v0 != null) {
                                                                                                                                                        if (controllerFragment5.f2630w0 != null) {
                                                                                                                                                            AbstractC0330t.m856g("\u505c\u6b62\u95ea\u5149");
                                                                                                                                                            controllerFragment5.f2629v0.removeCallbacks(controllerFragment5.f2630w0);
                                                                                                                                                            controllerFragment5.f2629v0 = null;
                                                                                                                                                            controllerFragment5.f2630w0 = null;
                                                                                                                                                            if (AbstractC0330t.m848Y(0).booleanValue()) {
                                                                                                                                                                controllerFragment5.f2627t0 = 0;
                                                                                                                                                            }
                                                                                                                                                            controllerFragment5.f2604W.f2680l.setBackgroundColor(-2013265920);
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        AbstractC0330t.m856g("\u5f00\u542f\u95ea\u70c1");
                                                                                                                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                                                                                                                        controllerFragment5.f2629v0 = handler;
                                                                                                                                                        RunnableC0063i runnableC0063i = new RunnableC0063i(10, controllerFragment5);
                                                                                                                                                        controllerFragment5.f2630w0 = runnableC0063i;
                                                                                                                                                        handler.post(runnableC0063i);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                    break;
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    final int i5 = 1;
                                                                                                                                    this.f2604W.f2690v.setOnClickListener(new View.OnClickListener(this) { // from class: f1.c

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2885b;

                                                                                                                                        {
                                                                                                                                            this.f2885b = this;
                                                                                                                                        }

                                                                                                                                        @Override // android.view.View.OnClickListener
                                                                                                                                        public final void onClick(View view) {
                                                                                                                                            switch (i5) {
                                                                                                                                                case 0:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2885b;
                                                                                                                                                    if (!controllerFragment.f2604W.f2686r.getButtonText().equals("\u4fdd\u5b58\u94fe\u63a5")) {
                                                                                                                                                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                                                                                                                                                        intent.setType("video/*");
                                                                                                                                                        intent.addCategory("android.intent.category.OPENABLE");
                                                                                                                                                        controllerFragment.f2608a0.mo1115a(intent);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        String trim = controllerFragment.f2604W.f2679k.getText().toString().trim();
                                                                                                                                                        controllerFragment.f2605X.f2586a.edit().putString("PlayRtmpUrl", trim).apply();
                                                                                                                                                        Object obj = controllerFragment.f2605X.f2591f.f1855e;
                                                                                                                                                        if (obj == C0817A.f1850j) {
                                                                                                                                                            obj = null;
                                                                                                                                                        }
                                                                                                                                                        if (((Integer) obj).intValue() != 1) {
                                                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            controllerFragment.f2604W.f2686r.setEnabled(false);
                                                                                                                                                            controllerFragment.f2604W.f2686r.m1773e();
                                                                                                                                                            new Thread(new RunnableC0038o(controllerFragment, 1, trim)).start();
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 1:
                                                                                                                                                    ControllerFragment controllerFragment2 = this.f2885b;
                                                                                                                                                    Object obj2 = controllerFragment2.f2605X.f2591f.f1855e;
                                                                                                                                                    if (obj2 == C0817A.f1850j) {
                                                                                                                                                        obj2 = null;
                                                                                                                                                    }
                                                                                                                                                    if (((Integer) obj2).intValue() == 0) {
                                                                                                                                                        App app = controllerFragment2.f2605X;
                                                                                                                                                        if (!(app.f2586a.getInt("PlayFileType", 1) == 1 ? app.f2586a.getString("PlayFileMp4", HttpUrl.FRAGMENT_ENCODE_SET) : app.f2586a.getString("PlayRtmpUrl", "rtmp://ns8.indexforce.com/home/mystream")).isEmpty()) {
                                                                                                                                                            controllerFragment2.f2604W.f2690v.setEnabled(false);
                                                                                                                                                            controllerFragment2.f2604W.f2690v.m1773e();
                                                                                                                                                            new Thread(new RunnableC0078b(14, controllerFragment2)).start();
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            AbstractC0330t.m856g("\u8bf7\u5148\u9009\u62e9\u8981\u64ad\u653e\u7684\u6587\u4ef6 \u6216 rtmp");
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        try {
                                                                                                                                                            ((C1391f) AbstractC0330t.m837E()).m1798k();
                                                                                                                                                            break;
                                                                                                                                                        } catch (RemoteException | Exception unused) {
                                                                                                                                                            return;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 2:
                                                                                                                                                    ControllerFragment controllerFragment3 = this.f2885b;
                                                                                                                                                    controllerFragment3.f2604W.f2680l.setVisibility(0);
                                                                                                                                                    HandlerThread handlerThread = new HandlerThread("CameraBackground");
                                                                                                                                                    controllerFragment3.f2613f0 = handlerThread;
                                                                                                                                                    handlerThread.start();
                                                                                                                                                    controllerFragment3.f2612e0 = new Handler(controllerFragment3.f2613f0.getLooper());
                                                                                                                                                    if (AbstractC0458i.m1176j(controllerFragment3.m1120H(), "android.permission.CAMERA") == 0) {
                                                                                                                                                        controllerFragment3.m1785M();
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment3.f2607Z.mo1115a("android.permission.CAMERA");
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                case 3:
                                                                                                                                                    ControllerFragment controllerFragment4 = this.f2885b;
                                                                                                                                                    CameraCaptureSession cameraCaptureSession = controllerFragment4.f2610c0;
                                                                                                                                                    if (cameraCaptureSession != null) {
                                                                                                                                                        cameraCaptureSession.close();
                                                                                                                                                        controllerFragment4.f2610c0 = null;
                                                                                                                                                    }
                                                                                                                                                    CameraDevice cameraDevice = controllerFragment4.f2609b0;
                                                                                                                                                    if (cameraDevice != null) {
                                                                                                                                                        cameraDevice.close();
                                                                                                                                                        controllerFragment4.f2609b0 = null;
                                                                                                                                                    }
                                                                                                                                                    HandlerThread handlerThread2 = controllerFragment4.f2613f0;
                                                                                                                                                    if (handlerThread2 != null) {
                                                                                                                                                        handlerThread2.quitSafely();
                                                                                                                                                        try {
                                                                                                                                                            controllerFragment4.f2613f0.join();
                                                                                                                                                            controllerFragment4.f2613f0 = null;
                                                                                                                                                            controllerFragment4.f2612e0 = null;
                                                                                                                                                        } catch (InterruptedException e2) {
                                                                                                                                                            e2.printStackTrace();
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    controllerFragment4.f2604W.f2680l.setVisibility(8);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment5 = this.f2885b;
                                                                                                                                                    if (controllerFragment5.f2629v0 != null) {
                                                                                                                                                        if (controllerFragment5.f2630w0 != null) {
                                                                                                                                                            AbstractC0330t.m856g("\u505c\u6b62\u95ea\u5149");
                                                                                                                                                            controllerFragment5.f2629v0.removeCallbacks(controllerFragment5.f2630w0);
                                                                                                                                                            controllerFragment5.f2629v0 = null;
                                                                                                                                                            controllerFragment5.f2630w0 = null;
                                                                                                                                                            if (AbstractC0330t.m848Y(0).booleanValue()) {
                                                                                                                                                                controllerFragment5.f2627t0 = 0;
                                                                                                                                                            }
                                                                                                                                                            controllerFragment5.f2604W.f2680l.setBackgroundColor(-2013265920);
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        AbstractC0330t.m856g("\u5f00\u542f\u95ea\u70c1");
                                                                                                                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                                                                                                                        controllerFragment5.f2629v0 = handler;
                                                                                                                                                        RunnableC0063i runnableC0063i = new RunnableC0063i(10, controllerFragment5);
                                                                                                                                                        controllerFragment5.f2630w0 = runnableC0063i;
                                                                                                                                                        handler.post(runnableC0063i);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                    break;
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    this.f2604W.f2670b.setChecked(this.f2605X.f2586a.getBoolean("PlayAutoRotate", false));
                                                                                                                                    this.f2604W.f2678j.setChecked(this.f2605X.m1778c());
                                                                                                                                    C1442d c1442d = new C1442d(0, this);
                                                                                                                                    this.f2615h0 = c1442d;
                                                                                                                                    this.f2604W.f2691w.setOnCheckedChangeListener(c1442d);
                                                                                                                                    this.f2604W.f2670b.setOnCheckedChangeListener(new C1442d(1, this));
                                                                                                                                    this.f2604W.f2678j.setOnCheckedChangeListener(new C1442d(2, this));
                                                                                                                                    this.f2616i0 = m1118F(new C0400M(3), new C1439a(this, 2));
                                                                                                                                    this.f2604W.f2669a.setOnCheckedChangeListener(new C1442d(3, this));
                                                                                                                                    this.f2611d0 = (CameraManager) this.f2606Y.getSystemService("camera");
                                                                                                                                    final int i6 = 2;
                                                                                                                                    this.f2604W.f2671c.setOnClickListener(new View.OnClickListener(this) { // from class: f1.c

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2885b;

                                                                                                                                        {
                                                                                                                                            this.f2885b = this;
                                                                                                                                        }

                                                                                                                                        @Override // android.view.View.OnClickListener
                                                                                                                                        public final void onClick(View view) {
                                                                                                                                            switch (i6) {
                                                                                                                                                case 0:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2885b;
                                                                                                                                                    if (!controllerFragment.f2604W.f2686r.getButtonText().equals("\u4fdd\u5b58\u94fe\u63a5")) {
                                                                                                                                                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                                                                                                                                                        intent.setType("video/*");
                                                                                                                                                        intent.addCategory("android.intent.category.OPENABLE");
                                                                                                                                                        controllerFragment.f2608a0.mo1115a(intent);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        String trim = controllerFragment.f2604W.f2679k.getText().toString().trim();
                                                                                                                                                        controllerFragment.f2605X.f2586a.edit().putString("PlayRtmpUrl", trim).apply();
                                                                                                                                                        Object obj = controllerFragment.f2605X.f2591f.f1855e;
                                                                                                                                                        if (obj == C0817A.f1850j) {
                                                                                                                                                            obj = null;
                                                                                                                                                        }
                                                                                                                                                        if (((Integer) obj).intValue() != 1) {
                                                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            controllerFragment.f2604W.f2686r.setEnabled(false);
                                                                                                                                                            controllerFragment.f2604W.f2686r.m1773e();
                                                                                                                                                            new Thread(new RunnableC0038o(controllerFragment, 1, trim)).start();
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 1:
                                                                                                                                                    ControllerFragment controllerFragment2 = this.f2885b;
                                                                                                                                                    Object obj2 = controllerFragment2.f2605X.f2591f.f1855e;
                                                                                                                                                    if (obj2 == C0817A.f1850j) {
                                                                                                                                                        obj2 = null;
                                                                                                                                                    }
                                                                                                                                                    if (((Integer) obj2).intValue() == 0) {
                                                                                                                                                        App app = controllerFragment2.f2605X;
                                                                                                                                                        if (!(app.f2586a.getInt("PlayFileType", 1) == 1 ? app.f2586a.getString("PlayFileMp4", HttpUrl.FRAGMENT_ENCODE_SET) : app.f2586a.getString("PlayRtmpUrl", "rtmp://ns8.indexforce.com/home/mystream")).isEmpty()) {
                                                                                                                                                            controllerFragment2.f2604W.f2690v.setEnabled(false);
                                                                                                                                                            controllerFragment2.f2604W.f2690v.m1773e();
                                                                                                                                                            new Thread(new RunnableC0078b(14, controllerFragment2)).start();
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            AbstractC0330t.m856g("\u8bf7\u5148\u9009\u62e9\u8981\u64ad\u653e\u7684\u6587\u4ef6 \u6216 rtmp");
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        try {
                                                                                                                                                            ((C1391f) AbstractC0330t.m837E()).m1798k();
                                                                                                                                                            break;
                                                                                                                                                        } catch (RemoteException | Exception unused) {
                                                                                                                                                            return;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 2:
                                                                                                                                                    ControllerFragment controllerFragment3 = this.f2885b;
                                                                                                                                                    controllerFragment3.f2604W.f2680l.setVisibility(0);
                                                                                                                                                    HandlerThread handlerThread = new HandlerThread("CameraBackground");
                                                                                                                                                    controllerFragment3.f2613f0 = handlerThread;
                                                                                                                                                    handlerThread.start();
                                                                                                                                                    controllerFragment3.f2612e0 = new Handler(controllerFragment3.f2613f0.getLooper());
                                                                                                                                                    if (AbstractC0458i.m1176j(controllerFragment3.m1120H(), "android.permission.CAMERA") == 0) {
                                                                                                                                                        controllerFragment3.m1785M();
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment3.f2607Z.mo1115a("android.permission.CAMERA");
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                case 3:
                                                                                                                                                    ControllerFragment controllerFragment4 = this.f2885b;
                                                                                                                                                    CameraCaptureSession cameraCaptureSession = controllerFragment4.f2610c0;
                                                                                                                                                    if (cameraCaptureSession != null) {
                                                                                                                                                        cameraCaptureSession.close();
                                                                                                                                                        controllerFragment4.f2610c0 = null;
                                                                                                                                                    }
                                                                                                                                                    CameraDevice cameraDevice = controllerFragment4.f2609b0;
                                                                                                                                                    if (cameraDevice != null) {
                                                                                                                                                        cameraDevice.close();
                                                                                                                                                        controllerFragment4.f2609b0 = null;
                                                                                                                                                    }
                                                                                                                                                    HandlerThread handlerThread2 = controllerFragment4.f2613f0;
                                                                                                                                                    if (handlerThread2 != null) {
                                                                                                                                                        handlerThread2.quitSafely();
                                                                                                                                                        try {
                                                                                                                                                            controllerFragment4.f2613f0.join();
                                                                                                                                                            controllerFragment4.f2613f0 = null;
                                                                                                                                                            controllerFragment4.f2612e0 = null;
                                                                                                                                                        } catch (InterruptedException e2) {
                                                                                                                                                            e2.printStackTrace();
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    controllerFragment4.f2604W.f2680l.setVisibility(8);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment5 = this.f2885b;
                                                                                                                                                    if (controllerFragment5.f2629v0 != null) {
                                                                                                                                                        if (controllerFragment5.f2630w0 != null) {
                                                                                                                                                            AbstractC0330t.m856g("\u505c\u6b62\u95ea\u5149");
                                                                                                                                                            controllerFragment5.f2629v0.removeCallbacks(controllerFragment5.f2630w0);
                                                                                                                                                            controllerFragment5.f2629v0 = null;
                                                                                                                                                            controllerFragment5.f2630w0 = null;
                                                                                                                                                            if (AbstractC0330t.m848Y(0).booleanValue()) {
                                                                                                                                                                controllerFragment5.f2627t0 = 0;
                                                                                                                                                            }
                                                                                                                                                            controllerFragment5.f2604W.f2680l.setBackgroundColor(-2013265920);
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        AbstractC0330t.m856g("\u5f00\u542f\u95ea\u70c1");
                                                                                                                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                                                                                                                        controllerFragment5.f2629v0 = handler;
                                                                                                                                                        RunnableC0063i runnableC0063i = new RunnableC0063i(10, controllerFragment5);
                                                                                                                                                        controllerFragment5.f2630w0 = runnableC0063i;
                                                                                                                                                        handler.post(runnableC0063i);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                    break;
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    final int i7 = 3;
                                                                                                                                    this.f2604W.f2680l.setOnClickListener(new View.OnClickListener(this) { // from class: f1.c

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2885b;

                                                                                                                                        {
                                                                                                                                            this.f2885b = this;
                                                                                                                                        }

                                                                                                                                        @Override // android.view.View.OnClickListener
                                                                                                                                        public final void onClick(View view) {
                                                                                                                                            switch (i7) {
                                                                                                                                                case 0:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2885b;
                                                                                                                                                    if (!controllerFragment.f2604W.f2686r.getButtonText().equals("\u4fdd\u5b58\u94fe\u63a5")) {
                                                                                                                                                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                                                                                                                                                        intent.setType("video/*");
                                                                                                                                                        intent.addCategory("android.intent.category.OPENABLE");
                                                                                                                                                        controllerFragment.f2608a0.mo1115a(intent);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        String trim = controllerFragment.f2604W.f2679k.getText().toString().trim();
                                                                                                                                                        controllerFragment.f2605X.f2586a.edit().putString("PlayRtmpUrl", trim).apply();
                                                                                                                                                        Object obj = controllerFragment.f2605X.f2591f.f1855e;
                                                                                                                                                        if (obj == C0817A.f1850j) {
                                                                                                                                                            obj = null;
                                                                                                                                                        }
                                                                                                                                                        if (((Integer) obj).intValue() != 1) {
                                                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            controllerFragment.f2604W.f2686r.setEnabled(false);
                                                                                                                                                            controllerFragment.f2604W.f2686r.m1773e();
                                                                                                                                                            new Thread(new RunnableC0038o(controllerFragment, 1, trim)).start();
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 1:
                                                                                                                                                    ControllerFragment controllerFragment2 = this.f2885b;
                                                                                                                                                    Object obj2 = controllerFragment2.f2605X.f2591f.f1855e;
                                                                                                                                                    if (obj2 == C0817A.f1850j) {
                                                                                                                                                        obj2 = null;
                                                                                                                                                    }
                                                                                                                                                    if (((Integer) obj2).intValue() == 0) {
                                                                                                                                                        App app = controllerFragment2.f2605X;
                                                                                                                                                        if (!(app.f2586a.getInt("PlayFileType", 1) == 1 ? app.f2586a.getString("PlayFileMp4", HttpUrl.FRAGMENT_ENCODE_SET) : app.f2586a.getString("PlayRtmpUrl", "rtmp://ns8.indexforce.com/home/mystream")).isEmpty()) {
                                                                                                                                                            controllerFragment2.f2604W.f2690v.setEnabled(false);
                                                                                                                                                            controllerFragment2.f2604W.f2690v.m1773e();
                                                                                                                                                            new Thread(new RunnableC0078b(14, controllerFragment2)).start();
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            AbstractC0330t.m856g("\u8bf7\u5148\u9009\u62e9\u8981\u64ad\u653e\u7684\u6587\u4ef6 \u6216 rtmp");
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        try {
                                                                                                                                                            ((C1391f) AbstractC0330t.m837E()).m1798k();
                                                                                                                                                            break;
                                                                                                                                                        } catch (RemoteException | Exception unused) {
                                                                                                                                                            return;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 2:
                                                                                                                                                    ControllerFragment controllerFragment3 = this.f2885b;
                                                                                                                                                    controllerFragment3.f2604W.f2680l.setVisibility(0);
                                                                                                                                                    HandlerThread handlerThread = new HandlerThread("CameraBackground");
                                                                                                                                                    controllerFragment3.f2613f0 = handlerThread;
                                                                                                                                                    handlerThread.start();
                                                                                                                                                    controllerFragment3.f2612e0 = new Handler(controllerFragment3.f2613f0.getLooper());
                                                                                                                                                    if (AbstractC0458i.m1176j(controllerFragment3.m1120H(), "android.permission.CAMERA") == 0) {
                                                                                                                                                        controllerFragment3.m1785M();
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment3.f2607Z.mo1115a("android.permission.CAMERA");
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                case 3:
                                                                                                                                                    ControllerFragment controllerFragment4 = this.f2885b;
                                                                                                                                                    CameraCaptureSession cameraCaptureSession = controllerFragment4.f2610c0;
                                                                                                                                                    if (cameraCaptureSession != null) {
                                                                                                                                                        cameraCaptureSession.close();
                                                                                                                                                        controllerFragment4.f2610c0 = null;
                                                                                                                                                    }
                                                                                                                                                    CameraDevice cameraDevice = controllerFragment4.f2609b0;
                                                                                                                                                    if (cameraDevice != null) {
                                                                                                                                                        cameraDevice.close();
                                                                                                                                                        controllerFragment4.f2609b0 = null;
                                                                                                                                                    }
                                                                                                                                                    HandlerThread handlerThread2 = controllerFragment4.f2613f0;
                                                                                                                                                    if (handlerThread2 != null) {
                                                                                                                                                        handlerThread2.quitSafely();
                                                                                                                                                        try {
                                                                                                                                                            controllerFragment4.f2613f0.join();
                                                                                                                                                            controllerFragment4.f2613f0 = null;
                                                                                                                                                            controllerFragment4.f2612e0 = null;
                                                                                                                                                        } catch (InterruptedException e2) {
                                                                                                                                                            e2.printStackTrace();
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    controllerFragment4.f2604W.f2680l.setVisibility(8);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment5 = this.f2885b;
                                                                                                                                                    if (controllerFragment5.f2629v0 != null) {
                                                                                                                                                        if (controllerFragment5.f2630w0 != null) {
                                                                                                                                                            AbstractC0330t.m856g("\u505c\u6b62\u95ea\u5149");
                                                                                                                                                            controllerFragment5.f2629v0.removeCallbacks(controllerFragment5.f2630w0);
                                                                                                                                                            controllerFragment5.f2629v0 = null;
                                                                                                                                                            controllerFragment5.f2630w0 = null;
                                                                                                                                                            if (AbstractC0330t.m848Y(0).booleanValue()) {
                                                                                                                                                                controllerFragment5.f2627t0 = 0;
                                                                                                                                                            }
                                                                                                                                                            controllerFragment5.f2604W.f2680l.setBackgroundColor(-2013265920);
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        AbstractC0330t.m856g("\u5f00\u542f\u95ea\u70c1");
                                                                                                                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                                                                                                                        controllerFragment5.f2629v0 = handler;
                                                                                                                                                        RunnableC0063i runnableC0063i = new RunnableC0063i(10, controllerFragment5);
                                                                                                                                                        controllerFragment5.f2630w0 = runnableC0063i;
                                                                                                                                                        handler.post(runnableC0063i);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                    break;
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    final int i8 = 0;
                                                                                                                                    this.f2605X.f2592g.m1364d(this, new InterfaceC0818B(this) { // from class: f1.g

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2892b;

                                                                                                                                        {
                                                                                                                                            this.f2892b = this;
                                                                                                                                        }

                                                                                                                                        @Override // androidx.lifecycle.InterfaceC0818B
                                                                                                                                        /* renamed from: a */
                                                                                                                                        public final void mo1099a(Object obj) {
                                                                                                                                            switch (i8) {
                                                                                                                                                case 0:
                                                                                                                                                    this.f2892b.f2604W.f2677i.setText((String) obj);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2892b;
                                                                                                                                                    controllerFragment.getClass();
                                                                                                                                                    if (((Integer) obj).intValue() != 0) {
                                                                                                                                                        controllerFragment.f2604W.f2690v.setTextColor(-16711936);
                                                                                                                                                        controllerFragment.f2604W.f2690v.setButtonText("\u8fd8\u539f\u76f8\u673a");
                                                                                                                                                        controllerFragment.f2604W.f2673e.setVisibility(0);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment.f2604W.f2690v.setTextColor(-1);
                                                                                                                                                        controllerFragment.f2604W.f2690v.setButtonText("\u66ff\u6362\u76f8\u673a");
                                                                                                                                                        controllerFragment.f2604W.f2673e.setVisibility(8);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    final int i9 = 1;
                                                                                                                                    this.f2605X.f2591f.m1364d(this, new InterfaceC0818B(this) { // from class: f1.g

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2892b;

                                                                                                                                        {
                                                                                                                                            this.f2892b = this;
                                                                                                                                        }

                                                                                                                                        @Override // androidx.lifecycle.InterfaceC0818B
                                                                                                                                        /* renamed from: a */
                                                                                                                                        public final void mo1099a(Object obj) {
                                                                                                                                            switch (i9) {
                                                                                                                                                case 0:
                                                                                                                                                    this.f2892b.f2604W.f2677i.setText((String) obj);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2892b;
                                                                                                                                                    controllerFragment.getClass();
                                                                                                                                                    if (((Integer) obj).intValue() != 0) {
                                                                                                                                                        controllerFragment.f2604W.f2690v.setTextColor(-16711936);
                                                                                                                                                        controllerFragment.f2604W.f2690v.setButtonText("\u8fd8\u539f\u76f8\u673a");
                                                                                                                                                        controllerFragment.f2604W.f2673e.setVisibility(0);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment.f2604W.f2690v.setTextColor(-1);
                                                                                                                                                        controllerFragment.f2604W.f2690v.setButtonText("\u66ff\u6362\u76f8\u673a");
                                                                                                                                                        controllerFragment.f2604W.f2673e.setVisibility(8);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    final int i10 = 4;
                                                                                                                                    this.f2604W.f2692x.setOnClickListener(new View.OnClickListener(this) { // from class: f1.c

                                                                                                                                        /* renamed from: b */
                                                                                                                                        public final /* synthetic */ ControllerFragment f2885b;

                                                                                                                                        {
                                                                                                                                            this.f2885b = this;
                                                                                                                                        }

                                                                                                                                        @Override // android.view.View.OnClickListener
                                                                                                                                        public final void onClick(View view) {
                                                                                                                                            switch (i10) {
                                                                                                                                                case 0:
                                                                                                                                                    ControllerFragment controllerFragment = this.f2885b;
                                                                                                                                                    if (!controllerFragment.f2604W.f2686r.getButtonText().equals("\u4fdd\u5b58\u94fe\u63a5")) {
                                                                                                                                                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                                                                                                                                                        intent.setType("video/*");
                                                                                                                                                        intent.addCategory("android.intent.category.OPENABLE");
                                                                                                                                                        controllerFragment.f2608a0.mo1115a(intent);
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        String trim = controllerFragment.f2604W.f2679k.getText().toString().trim();
                                                                                                                                                        controllerFragment.f2605X.f2586a.edit().putString("PlayRtmpUrl", trim).apply();
                                                                                                                                                        Object obj = controllerFragment.f2605X.f2591f.f1855e;
                                                                                                                                                        if (obj == C0817A.f1850j) {
                                                                                                                                                            obj = null;
                                                                                                                                                        }
                                                                                                                                                        if (((Integer) obj).intValue() != 1) {
                                                                                                                                                            AbstractC0330t.m856g("\u4fdd\u5b58\u6210\u529f");
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            controllerFragment.f2604W.f2686r.setEnabled(false);
                                                                                                                                                            controllerFragment.f2604W.f2686r.m1773e();
                                                                                                                                                            new Thread(new RunnableC0038o(controllerFragment, 1, trim)).start();
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 1:
                                                                                                                                                    ControllerFragment controllerFragment2 = this.f2885b;
                                                                                                                                                    Object obj2 = controllerFragment2.f2605X.f2591f.f1855e;
                                                                                                                                                    if (obj2 == C0817A.f1850j) {
                                                                                                                                                        obj2 = null;
                                                                                                                                                    }
                                                                                                                                                    if (((Integer) obj2).intValue() == 0) {
                                                                                                                                                        App app = controllerFragment2.f2605X;
                                                                                                                                                        if (!(app.f2586a.getInt("PlayFileType", 1) == 1 ? app.f2586a.getString("PlayFileMp4", HttpUrl.FRAGMENT_ENCODE_SET) : app.f2586a.getString("PlayRtmpUrl", "rtmp://ns8.indexforce.com/home/mystream")).isEmpty()) {
                                                                                                                                                            controllerFragment2.f2604W.f2690v.setEnabled(false);
                                                                                                                                                            controllerFragment2.f2604W.f2690v.m1773e();
                                                                                                                                                            new Thread(new RunnableC0078b(14, controllerFragment2)).start();
                                                                                                                                                            break;
                                                                                                                                                        } else {
                                                                                                                                                            AbstractC0330t.m856g("\u8bf7\u5148\u9009\u62e9\u8981\u64ad\u653e\u7684\u6587\u4ef6 \u6216 rtmp");
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        try {
                                                                                                                                                            ((C1391f) AbstractC0330t.m837E()).m1798k();
                                                                                                                                                            break;
                                                                                                                                                        } catch (RemoteException | Exception unused) {
                                                                                                                                                            return;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                case 2:
                                                                                                                                                    ControllerFragment controllerFragment3 = this.f2885b;
                                                                                                                                                    controllerFragment3.f2604W.f2680l.setVisibility(0);
                                                                                                                                                    HandlerThread handlerThread = new HandlerThread("CameraBackground");
                                                                                                                                                    controllerFragment3.f2613f0 = handlerThread;
                                                                                                                                                    handlerThread.start();
                                                                                                                                                    controllerFragment3.f2612e0 = new Handler(controllerFragment3.f2613f0.getLooper());
                                                                                                                                                    if (AbstractC0458i.m1176j(controllerFragment3.m1120H(), "android.permission.CAMERA") == 0) {
                                                                                                                                                        controllerFragment3.m1785M();
                                                                                                                                                        break;
                                                                                                                                                    } else {
                                                                                                                                                        controllerFragment3.f2607Z.mo1115a("android.permission.CAMERA");
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                case 3:
                                                                                                                                                    ControllerFragment controllerFragment4 = this.f2885b;
                                                                                                                                                    CameraCaptureSession cameraCaptureSession = controllerFragment4.f2610c0;
                                                                                                                                                    if (cameraCaptureSession != null) {
                                                                                                                                                        cameraCaptureSession.close();
                                                                                                                                                        controllerFragment4.f2610c0 = null;
                                                                                                                                                    }
                                                                                                                                                    CameraDevice cameraDevice = controllerFragment4.f2609b0;
                                                                                                                                                    if (cameraDevice != null) {
                                                                                                                                                        cameraDevice.close();
                                                                                                                                                        controllerFragment4.f2609b0 = null;
                                                                                                                                                    }
                                                                                                                                                    HandlerThread handlerThread2 = controllerFragment4.f2613f0;
                                                                                                                                                    if (handlerThread2 != null) {
                                                                                                                                                        handlerThread2.quitSafely();
                                                                                                                                                        try {
                                                                                                                                                            controllerFragment4.f2613f0.join();
                                                                                                                                                            controllerFragment4.f2613f0 = null;
                                                                                                                                                            controllerFragment4.f2612e0 = null;
                                                                                                                                                        } catch (InterruptedException e2) {
                                                                                                                                                            e2.printStackTrace();
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    controllerFragment4.f2604W.f2680l.setVisibility(8);
                                                                                                                                                    break;
                                                                                                                                                default:
                                                                                                                                                    ControllerFragment controllerFragment5 = this.f2885b;
                                                                                                                                                    if (controllerFragment5.f2629v0 != null) {
                                                                                                                                                        if (controllerFragment5.f2630w0 != null) {
                                                                                                                                                            AbstractC0330t.m856g("\u505c\u6b62\u95ea\u5149");
                                                                                                                                                            controllerFragment5.f2629v0.removeCallbacks(controllerFragment5.f2630w0);
                                                                                                                                                            controllerFragment5.f2629v0 = null;
                                                                                                                                                            controllerFragment5.f2630w0 = null;
                                                                                                                                                            if (AbstractC0330t.m848Y(0).booleanValue()) {
                                                                                                                                                                controllerFragment5.f2627t0 = 0;
                                                                                                                                                            }
                                                                                                                                                            controllerFragment5.f2604W.f2680l.setBackgroundColor(-2013265920);
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        AbstractC0330t.m856g("\u5f00\u542f\u95ea\u70c1");
                                                                                                                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                                                                                                                        controllerFragment5.f2629v0 = handler;
                                                                                                                                                        RunnableC0063i runnableC0063i = new RunnableC0063i(10, controllerFragment5);
                                                                                                                                                        controllerFragment5.f2630w0 = runnableC0063i;
                                                                                                                                                        handler.post(runnableC0063i);
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                    break;
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                    if (this.f2605X.f2586a.getInt("PlayAutoColor_mode", 1) == 1) {
                                                                                                                                        this.f2604W.f2687s.check(R.id.ssmode1);
                                                                                                                                    } else {
                                                                                                                                        this.f2604W.f2687s.check(R.id.ssmode2);
                                                                                                                                    }
                                                                                                                                    this.f2604W.f2687s.setOnCheckedChangeListener(new C1440b(1, this));
                                                                                                                                    this.f2604W.f2693y.setText("\u7167\u5c04\u5f3a\u5ea6:" + ((int) (this.f2605X.f2586a.getFloat("AutoColor_intensity", 0.3f) * 100.0f)) + "%");
                                                                                                                                    this.f2604W.f2682n.setProgress((int) (this.f2605X.f2586a.getFloat("AutoColor_intensity", 0.3f) * 100.0f));
                                                                                                                                    this.f2604W.f2682n.setOnSeekBarChangeListener(new C1446h(this, 0));
                                                                                                                                    this.f2604W.f2694z.setText("\u7167\u5c04\u76f4\u5f84:" + ((int) (this.f2605X.f2586a.getFloat("AutoColor_diameter", 0.6f) * 100.0f)) + "%");
                                                                                                                                    this.f2604W.f2683o.setProgress((int) (this.f2605X.f2586a.getFloat("AutoColor_diameter", 0.6f) * 100.0f));
                                                                                                                                    this.f2604W.f2683o.setOnSeekBarChangeListener(new C1446h(this, 1));
                                                                                                                                    this.f2604W.f2667A.setText("X\u5750\u6807:" + this.f2605X.f2586a.getFloat("AutoColor_X", 50.0f) + "%");
                                                                                                                                    this.f2604W.f2684p.setProgress((int) this.f2605X.f2586a.getFloat("AutoColor_X", 50.0f));
                                                                                                                                    this.f2604W.f2684p.setOnSeekBarChangeListener(new C1446h(this, 2));
                                                                                                                                    this.f2604W.f2668B.setText("Y\u5750\u6807:" + this.f2605X.f2586a.getFloat("AutoColor_Y", 50.0f) + "%");
                                                                                                                                    this.f2604W.f2685q.setProgress((int) this.f2605X.f2586a.getFloat("AutoColor_Y", 50.0f));
                                                                                                                                    this.f2604W.f2685q.setOnSeekBarChangeListener(new C1446h(this, 3));
                                                                                                                                    return constraintLayout;
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                    i = i2;
                                                                                                } else {
                                                                                                    i = R.id.ssmode2;
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
        RunnableC0063i runnableC0063i;
        this.f1339D = true;
        Handler handler = this.f2629v0;
        if (handler != null && (runnableC0063i = this.f2630w0) != null) {
            handler.removeCallbacks(runnableC0063i);
            this.f2629v0 = null;
            this.f2630w0 = null;
        }
        VirtualDisplay virtualDisplay = this.f2619l0;
        if (virtualDisplay != null) {
            virtualDisplay.release();
            this.f2619l0 = null;
        }
        MediaProjection mediaProjection = this.f2618k0;
        if (mediaProjection != null) {
            mediaProjection.stop();
            this.f2618k0 = null;
        }
        ImageReader imageReader = this.f2620m0;
        if (imageReader != null) {
            imageReader.close();
            this.f2620m0 = null;
        }
        Handler handler2 = this.f2621n0;
        if (handler2 != null) {
            handler2.getLooper().quitSafely();
            this.f2621n0 = null;
        }
        this.f2604W = null;
    }
}
