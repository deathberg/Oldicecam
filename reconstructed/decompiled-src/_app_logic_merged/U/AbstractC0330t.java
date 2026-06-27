package p037U;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.AbstractC0834Q;
import androidx.lifecycle.C0838V;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.textfield.TextInputLayout;
import com.xiaomi.vlive.App;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;
import kotlin.UShort;
import okhttp3.HttpUrl;
import org.xmlpull.v1.XmlPullParserException;
import p001A0.C0019c;
import p008E.AbstractC0065a;
import p020L.AbstractC0144S;
import p031R.InterfaceC0272j;
import p039V.C0341b;
import p045Y.C0440v;
import p055c0.C1024a;
import p059d1.AbstractBinderC1392g;
import p059d1.C1391f;
import p059d1.C1394i;
import p059d1.InterfaceC1393h;
import p064f0.AbstractC1407H;
import p064f0.AbstractC1428p;
import p064f0.AbstractC1434v;
import p064f0.C1411L;
import p064f0.C1413a;
import p064f0.C1420h;
import p064f0.C1427o;
import p064f0.C1435w;
import p064f0.InterfaceC1409J;
import p066g.AbstractActivityC1471i;
import p072i1.C1528e;
import p072i1.C1529f;
import p072i1.C1530g;
import p072i1.C1531h;
import p072i1.EnumC1526c;
import p072i1.InterfaceC1525b;
import p081l.C1721K0;
import p096p1.InterfaceC1917a;
import p096p1.InterfaceC1928l;
import p099q1.AbstractC1952d;
import p099q1.AbstractC1957i;
import p099q1.C1950b;
import p115w1.AbstractC2077f;

/* renamed from: U.t */
/* loaded from: classes.dex */
public abstract class AbstractC0330t implements InterfaceC0272j {

    /* renamed from: a */
    public static Process f894a;

    /* renamed from: b */
    public static DataOutputStream f895b;

    /* renamed from: c */
    public static InterfaceC1393h f896c;

    /* renamed from: d */
    public static int f897d;

    /* renamed from: B */
    public static String m836B(Class cls) {
        LinkedHashMap linkedHashMap = C1411L.f2781b;
        String str = (String) linkedHashMap.get(cls);
        if (str == null) {
            InterfaceC1409J interfaceC1409J = (InterfaceC1409J) cls.getAnnotation(InterfaceC1409J.class);
            str = interfaceC1409J != null ? interfaceC1409J.value() : null;
            if (str == null || str.length() <= 0) {
                throw new IllegalArgumentException("No @Navigator.Name annotation found for ".concat(cls.getSimpleName()).toString());
            }
            linkedHashMap.put(cls, str);
        }
        AbstractC1952d.m2505b(str);
        return str;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v11, types: [d1.h] */
    /* renamed from: E */
    public static InterfaceC1393h m837E() {
        C1391f c1391f;
        try {
            InterfaceC1393h interfaceC1393h = f896c;
            if (interfaceC1393h != null) {
                return interfaceC1393h;
            }
            m863q("setenforce 0");
            IBinder iBinder = (IBinder) Class.forName("android.os.ServiceManager").getMethod("getService", String.class).invoke(null, App.f2584k.m1779d());
            if (iBinder == null) {
                int i = f897d + 1;
                f897d = i;
                if (i > 5) {
                    if (!m863q("id").contains("uid=0")) {
                        m856g("\u7a0b\u5e8f\u83b7\u53d6root\u6743\u9650\u5931\u8d25");
                    } else if (m863q("getenforce").contains("Permissive")) {
                        m856g("\u65e0\u6cd5\u4e0e\u8fdb\u7a0b\u901a\u4fe1\n\u8bf7\u5173\u95edAPP\u540e\u91cd\u65b0\u542f\u52a8\u518d\u8bd5");
                    } else {
                        m856g("\u7cfb\u7edf\u8bbe\u7f6e\u5931\u8d25,\u8bf7\u66f4\u6362\u4f4e\u7248\u672c\u7cfb\u7edf");
                    }
                }
                return null;
            }
            f897d = 0;
            iBinder.linkToDeath(new C1394i(), 0);
            m863q("setenforce 1");
            int i2 = AbstractBinderC1392g.f2664a;
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.xiaomi.vlive.IMyBinderService");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof InterfaceC1393h)) {
                C1391f c1391f2 = new C1391f();
                c1391f2.f2663a = iBinder;
                c1391f = c1391f2;
            } else {
                c1391f = (InterfaceC1393h) queryLocalInterface;
            }
            f896c = c1391f;
            return c1391f;
        } catch (Exception unused) {
            return null;
        }
    }

    /* renamed from: H */
    public static boolean m838H(EditText editText) {
        return editText.getInputType() != 0;
    }

    /* renamed from: L */
    public static InterfaceC1525b m839L(EnumC1526c enumC1526c, InterfaceC1917a interfaceC1917a) {
        int ordinal = enumC1526c.ordinal();
        if (ordinal == 0) {
            return new C1529f(interfaceC1917a);
        }
        C1530g c1530g = C1530g.f3226b;
        if (ordinal == 1) {
            C1528e c1528e = new C1528e();
            c1528e.f3221a = interfaceC1917a;
            c1528e.f3222b = c1530g;
            return c1528e;
        }
        if (ordinal != 2) {
            throw new C0440v();
        }
        C1531h c1531h = new C1531h();
        c1531h.f3229a = interfaceC1917a;
        c1531h.f3230b = c1530g;
        return c1531h;
    }

    /* renamed from: M */
    public static List m840M(Object obj) {
        List singletonList = Collections.singletonList(obj);
        AbstractC1952d.m2507d(singletonList, "singletonList(...)");
        return singletonList;
    }

    /* renamed from: N */
    public static final boolean m841N(int i, AbstractC1434v abstractC1434v) {
        AbstractC1952d.m2508e(abstractC1434v, "<this>");
        int i2 = AbstractC1434v.f2860f;
        Iterator it = AbstractC2077f.m2785y(abstractC1434v, new C1413a(3)).iterator();
        while (it.hasNext()) {
            if (((AbstractC1434v) it.next()).f2862b.f3207a == i) {
                return true;
            }
        }
        return false;
    }

    /* renamed from: O */
    public static final ArrayList m842O(Map map, InterfaceC1928l interfaceC1928l) {
        AbstractC1952d.m2508e(map, "<this>");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry entry : map.entrySet()) {
            C1420h c1420h = (C1420h) entry.getValue();
            Boolean valueOf = c1420h != null ? Boolean.valueOf(c1420h.f2797b) : null;
            AbstractC1952d.m2505b(valueOf);
            if (!valueOf.booleanValue() && !c1420h.f2798c) {
                linkedHashMap.put(entry.getKey(), entry.getValue());
            }
        }
        Set keySet = linkedHashMap.keySet();
        ArrayList arrayList = new ArrayList();
        for (Object obj : keySet) {
            if (((Boolean) interfaceC1928l.mo1215c((String) obj)).booleanValue()) {
                arrayList.add(obj);
            }
        }
        return arrayList;
    }

    /* renamed from: R */
    public static void m843R(long j2, long j3) {
        try {
            ((C1391f) m837E()).m1792e(j2, j3);
        } catch (RemoteException | Exception unused) {
        }
    }

    /* renamed from: T */
    public static Boolean m844T() {
        try {
            return Boolean.valueOf(((C1391f) m837E()).m1793f() == 5);
        } catch (RemoteException | Exception unused) {
            return Boolean.FALSE;
        }
    }

    /* renamed from: U */
    public static C0341b m845U(MappedByteBuffer mappedByteBuffer) {
        long j2;
        ByteBuffer duplicate = mappedByteBuffer.duplicate();
        duplicate.order(ByteOrder.BIG_ENDIAN);
        duplicate.position(duplicate.position() + 4);
        int i = duplicate.getShort() & UShort.MAX_VALUE;
        if (i > 100) {
            throw new IOException("Cannot read metadata.");
        }
        duplicate.position(duplicate.position() + 6);
        int i2 = 0;
        while (true) {
            if (i2 >= i) {
                j2 = -1;
                break;
            }
            int i3 = duplicate.getInt();
            duplicate.position(duplicate.position() + 4);
            j2 = duplicate.getInt() & 4294967295L;
            duplicate.position(duplicate.position() + 4);
            if (1835365473 == i3) {
                break;
            }
            i2++;
        }
        if (j2 != -1) {
            duplicate.position(duplicate.position() + ((int) (j2 - duplicate.position())));
            duplicate.position(duplicate.position() + 12);
            long j3 = duplicate.getInt() & 4294967295L;
            for (int i4 = 0; i4 < j3; i4++) {
                int i5 = duplicate.getInt();
                long j4 = duplicate.getInt() & 4294967295L;
                duplicate.getInt();
                if (1164798569 == i5 || 1701669481 == i5) {
                    duplicate.position((int) (j4 + j2));
                    C0341b c0341b = new C0341b();
                    duplicate.order(ByteOrder.LITTLE_ENDIAN);
                    int position = duplicate.position() + duplicate.getInt(duplicate.position());
                    c0341b.f330d = duplicate;
                    c0341b.f327a = position;
                    int i6 = position - duplicate.getInt(position);
                    c0341b.f328b = i6;
                    c0341b.f329c = ((ByteBuffer) c0341b.f330d).getShort(i6);
                    return c0341b;
                }
            }
        }
        throw new IOException("Cannot read metadata.");
    }

    /* renamed from: V */
    public static void m846V(TextInputLayout textInputLayout, CheckableImageButton checkableImageButton, ColorStateList colorStateList) {
        Drawable drawable = checkableImageButton.getDrawable();
        if (checkableImageButton.getDrawable() == null || colorStateList == null || !colorStateList.isStateful()) {
            return;
        }
        int[] drawableState = textInputLayout.getDrawableState();
        int[] drawableState2 = checkableImageButton.getDrawableState();
        int length = drawableState.length;
        int[] copyOf = Arrays.copyOf(drawableState, drawableState.length + drawableState2.length);
        System.arraycopy(drawableState2, 0, copyOf, length, drawableState2.length);
        int colorForState = colorStateList.getColorForState(copyOf, colorStateList.getDefaultColor());
        Drawable mutate = drawable.mutate();
        AbstractC0065a.m211h(mutate, ColorStateList.valueOf(colorForState));
        checkableImageButton.setImageDrawable(mutate);
    }

    /* renamed from: W */
    public static final void m847W(Object[] objArr, int i, int i2) {
        AbstractC1952d.m2508e(objArr, "<this>");
        while (i < i2) {
            objArr[i] = null;
            i++;
        }
    }

    /* renamed from: Y */
    public static Boolean m848Y(int i) {
        try {
            boolean z2 = true;
            if (((C1391f) m837E()).m1795h(i, App.f2584k.f2586a.getFloat("AutoColor_X", 50.0f), App.f2584k.f2586a.getFloat("AutoColor_Y", 50.0f), App.f2584k.f2586a.getFloat("AutoColor_intensity", 0.3f), App.f2584k.f2586a.getFloat("AutoColor_diameter", 0.6f), App.f2584k.f2586a.getInt("PlayAutoColor_mode", 1)) != 14) {
                z2 = false;
            }
            return Boolean.valueOf(z2);
        } catch (RemoteException | Exception unused) {
            return Boolean.FALSE;
        }
    }

    /* renamed from: a0 */
    public static Boolean m849a0(String str, int i) {
        try {
            return Boolean.valueOf(((C1391f) m837E()).m1796i(str, i) == 4);
        } catch (RemoteException | Exception unused) {
            return Boolean.FALSE;
        }
    }

    /* renamed from: b0 */
    public static void m850b0(CheckableImageButton checkableImageButton, View.OnLongClickListener onLongClickListener) {
        WeakHashMap weakHashMap = AbstractC0144S.f335a;
        boolean hasOnClickListeners = checkableImageButton.hasOnClickListeners();
        boolean z2 = onLongClickListener != null;
        boolean z3 = hasOnClickListeners || z2;
        checkableImageButton.setFocusable(z3);
        checkableImageButton.setClickable(hasOnClickListeners);
        checkableImageButton.setPressable(hasOnClickListeners);
        checkableImageButton.setLongClickable(z2);
        checkableImageButton.setImportantForAccessibility(z3 ? 1 : 2);
    }

    /* renamed from: c0 */
    public static void m851c0(Boolean bool) {
        try {
            ((C1391f) m837E()).m1791d(bool.booleanValue());
        } catch (RemoteException | Exception unused) {
        }
    }

    /* renamed from: d0 */
    public static void m852d0(int i) {
        try {
            ((C1391f) m837E()).m1794g(i);
        } catch (RemoteException | Exception unused) {
        }
    }

    /* renamed from: e */
    public static void m853e(Boolean bool) {
        try {
            ((C1391f) m837E()).m1788a(bool.booleanValue());
        } catch (RemoteException | Exception unused) {
        }
    }

    /* renamed from: f */
    public static void m854f(int i, Context context, String str) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setPadding(36, 36, 36, 36);
        linearLayout.setGravity(16);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#CC000000"));
        gradientDrawable.setCornerRadius(24.0f);
        linearLayout.setBackground(gradientDrawable);
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(-1);
        linearLayout.addView(textView);
        Toast toast = new Toast(context);
        toast.setView(linearLayout);
        toast.setDuration(i);
        toast.setGravity(17, 0, 0);
        toast.show();
    }

    /* renamed from: f0 */
    public static int m855f0(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(R.style.Animation.Activity, new int[]{i});
        int resourceId = obtainStyledAttributes.getResourceId(0, -1);
        obtainStyledAttributes.recycle();
        return resourceId;
    }

    /* renamed from: g */
    public static void m856g(String str) {
        m854f(0, App.f2584k.getApplicationContext(), str);
    }

    /* renamed from: h */
    public static void m857h(TextInputLayout textInputLayout, CheckableImageButton checkableImageButton, ColorStateList colorStateList, PorterDuff.Mode mode) {
        Drawable drawable = checkableImageButton.getDrawable();
        if (drawable != null) {
            drawable = drawable.mutate();
            if (colorStateList == null || !colorStateList.isStateful()) {
                AbstractC0065a.m211h(drawable, colorStateList);
            } else {
                int[] drawableState = textInputLayout.getDrawableState();
                int[] drawableState2 = checkableImageButton.getDrawableState();
                int length = drawableState.length;
                int[] copyOf = Arrays.copyOf(drawableState, drawableState.length + drawableState2.length);
                System.arraycopy(drawableState2, 0, copyOf, length, drawableState2.length);
                AbstractC0065a.m211h(drawable, ColorStateList.valueOf(colorStateList.getColorForState(copyOf, colorStateList.getDefaultColor())));
            }
            if (mode != null) {
                AbstractC0065a.m212i(drawable, mode);
            }
        }
        if (checkableImageButton.getDrawable() != drawable) {
            checkableImageButton.setImageDrawable(drawable);
        }
    }

    /* renamed from: i */
    public static void m858i(Boolean bool) {
        try {
            ((C1391f) m837E()).m1789b(bool.booleanValue());
        } catch (RemoteException | Exception unused) {
        }
    }

    /* renamed from: l */
    public static AbstractC1407H m859l(TypedValue typedValue, AbstractC1407H abstractC1407H, AbstractC1407H abstractC1407H2, String str, String str2) {
        if (abstractC1407H == null || abstractC1407H == abstractC1407H2) {
            return abstractC1407H == null ? abstractC1407H2 : abstractC1407H;
        }
        throw new XmlPullParserException("Type is " + str + " but found " + str2 + ": " + typedValue.data);
    }

    /* renamed from: m */
    public static void m860m(int i, int i2, int i3) {
        if (i < 0 || i2 > i3) {
            throw new IndexOutOfBoundsException("fromIndex: " + i + ", toIndex: " + i2 + ", size: " + i3);
        }
        if (i <= i2) {
            return;
        }
        throw new IllegalArgumentException("fromIndex: " + i + " > toIndex: " + i2);
    }

    /* renamed from: n */
    public static ImageView.ScaleType m861n(int i) {
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? i != 5 ? i != 6 ? ImageView.ScaleType.CENTER : ImageView.ScaleType.CENTER_INSIDE : ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_END : ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_START : ImageView.ScaleType.FIT_XY;
    }

    /* renamed from: p */
    public static AbstractC0834Q m862p(Class cls) {
        try {
            Object newInstance = cls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            AbstractC1952d.m2505b(newInstance);
            return (AbstractC0834Q) newInstance;
        } catch (IllegalAccessException e2) {
            throw new RuntimeException("Cannot create an instance of " + cls, e2);
        } catch (InstantiationException e3) {
            throw new RuntimeException("Cannot create an instance of " + cls, e3);
        } catch (NoSuchMethodException e4) {
            throw new RuntimeException("Cannot create an instance of " + cls, e4);
        }
    }

    /* renamed from: q */
    public static String m863q(String str) {
        boolean z2;
        StringBuilder sb = new StringBuilder();
        try {
            if (f894a == null || f895b == null) {
                try {
                    f894a = Runtime.getRuntime().exec("su");
                    f895b = new DataOutputStream(f894a.getOutputStream());
                    z2 = true;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    z2 = false;
                }
                if (!z2) {
                    return HttpUrl.FRAGMENT_ENCODE_SET;
                }
            }
            String str2 = "EOF_MARK_" + System.currentTimeMillis();
            f895b.writeBytes(str + "\n");
            f895b.writeBytes("echo " + str2 + "\n");
            f895b.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f894a.getInputStream()));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null || readLine.equals(str2)) {
                    break;
                }
                sb.append(readLine);
                sb.append("\n");
            }
        } catch (IOException e3) {
            sb.append("ERROR: ");
            sb.append(e3.getMessage());
        }
        return sb.toString().trim();
    }

    /* renamed from: r */
    public static AbstractC1434v m864r(C1435w c1435w) {
        Iterator it = AbstractC2077f.m2785y(c1435w, new C1413a(4)).iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("Sequence is empty.");
        }
        Object next = it.next();
        while (it.hasNext()) {
            next = it.next();
        }
        return (AbstractC1434v) next;
    }

    /* renamed from: s */
    public static final String m865s(C1950b c1950b) {
        String str;
        Class cls = c1950b.f4278a;
        AbstractC1952d.m2508e(cls, "jClass");
        String str2 = null;
        if (cls.isAnonymousClass() || cls.isLocalClass()) {
            return null;
        }
        boolean isArray = cls.isArray();
        HashMap hashMap = C1950b.f4276c;
        if (!isArray) {
            String str3 = (String) hashMap.get(cls.getName());
            return str3 == null ? cls.getCanonicalName() : str3;
        }
        Class<?> componentType = cls.getComponentType();
        if (componentType.isPrimitive() && (str = (String) hashMap.get(componentType.getName())) != null) {
            str2 = str.concat("Array");
        }
        return str2 == null ? "kotlin.Array" : str2;
    }

    /* renamed from: t */
    public static String m866t(C0323m c0323m, int i) {
        AbstractC1952d.m2508e(c0323m, "context");
        if (i <= 16777215) {
            return String.valueOf(i);
        }
        try {
            String resourceName = c0323m.f878a.getResources().getResourceName(i);
            AbstractC1952d.m2505b(resourceName);
            return resourceName;
        } catch (Resources.NotFoundException unused) {
            return String.valueOf(i);
        }
    }

    /* renamed from: u */
    public static Drawable m867u(Context context, int i) {
        return C1721K0.m2159b().m2162c(context, i);
    }

    /* renamed from: y */
    public static C1427o m868y(C0838V c0838v) {
        C0019c c0019c = AbstractC1428p.f2825a;
        C1024a c1024a = C1024a.f2087b;
        AbstractC1952d.m2508e(c0019c, "factory");
        AbstractC1952d.m2508e(c1024a, "extras");
        C0332v c0332v = new C0332v(c0838v, c0019c, c1024a);
        C1950b m2512a = AbstractC1957i.m2512a(C1427o.class);
        String m865s = m865s(m2512a);
        if (m865s != null) {
            return (C1427o) c0332v.m900i(m2512a, "androidx.lifecycle.ViewModelProvider.DefaultKey:".concat(m865s));
        }
        throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
    }

    /* renamed from: A */
    public abstract int mo869A();

    /* renamed from: C */
    public abstract int mo870C(View view);

    /* renamed from: D */
    public abstract int mo871D(CoordinatorLayout coordinatorLayout);

    /* renamed from: F */
    public abstract int mo872F();

    /* renamed from: G */
    public C0019c mo873G(AbstractActivityC1471i abstractActivityC1471i, Object obj) {
        return null;
    }

    /* renamed from: I */
    public abstract boolean mo874I(float f2);

    /* renamed from: J */
    public abstract boolean mo875J(View view);

    /* renamed from: K */
    public abstract boolean mo876K(float f2, float f3);

    /* renamed from: P */
    public abstract View mo877P(int i);

    /* renamed from: Q */
    public abstract boolean mo878Q();

    /* renamed from: S */
    public abstract Object mo879S(int i, Intent intent);

    /* renamed from: X */
    public abstract void mo880X(boolean z2);

    /* renamed from: Z */
    public abstract void mo881Z(boolean z2);

    /* renamed from: e0 */
    public abstract boolean mo882e0(View view, float f2);

    /* renamed from: g0 */
    public abstract void mo883g0(ViewGroup.MarginLayoutParams marginLayoutParams, int i, int i2);

    /* renamed from: j */
    public abstract int mo884j(ViewGroup.MarginLayoutParams marginLayoutParams);

    /* renamed from: k */
    public abstract float mo885k(int i);

    /* renamed from: o */
    public abstract Intent mo886o(AbstractActivityC1471i abstractActivityC1471i, Object obj);

    /* renamed from: v */
    public abstract int mo887v();

    /* renamed from: w */
    public abstract InputFilter[] mo888w(InputFilter[] inputFilterArr);

    /* renamed from: x */
    public abstract int mo889x();

    /* renamed from: z */
    public abstract int mo890z();
}
