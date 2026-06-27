package p059d1;

import android.os.IBinder;
import android.os.Parcel;

/* renamed from: d1.f */
/* loaded from: classes.dex */
public final class C1391f implements InterfaceC1393h {

    /* renamed from: a */
    public IBinder f2663a;

    /* renamed from: a */
    public final int m1788a(boolean z2) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeInt(z2 ? 1 : 0);
            this.f2663a.transact(17, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    @Override // android.os.IInterface
    public final IBinder asBinder() {
        return this.f2663a;
    }

    /* renamed from: b */
    public final int m1789b(boolean z2) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeInt(z2 ? 1 : 0);
            this.f2663a.transact(16, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: c */
    public final int[] m1790c() {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            this.f2663a.transact(13, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.createIntArray();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: d */
    public final int m1791d(boolean z2) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeInt(z2 ? 1 : 0);
            this.f2663a.transact(19, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: e */
    public final int m1792e(long j2, long j3) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeLong(j2);
            obtain.writeLong(j3);
            this.f2663a.transact(22, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: f */
    public final int m1793f() {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            this.f2663a.transact(15, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: g */
    public final int m1794g(int i) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeInt(i);
            this.f2663a.transact(18, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: h */
    public final int m1795h(int i, float f2, float f3, float f4, float f5, int i2) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeInt(i);
            obtain.writeFloat(f2);
            obtain.writeFloat(f3);
            obtain.writeFloat(f4);
            obtain.writeFloat(f5);
            obtain.writeInt(i2);
            this.f2663a.transact(24, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: i */
    public final int m1796i(String str, int i) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeInt(i);
            obtain.writeString(str);
            this.f2663a.transact(14, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: j */
    public final int m1797j(String str, boolean z2, boolean z3) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            obtain.writeString(str);
            obtain.writeInt(z2 ? 1 : 0);
            obtain.writeInt(z3 ? 1 : 0);
            this.f2663a.transact(11, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: k */
    public final int m1798k() {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            this.f2663a.transact(12, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    /* renamed from: l */
    public final int m1799l() {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.xiaomi.vlive.IMyBinderService");
            this.f2663a.transact(25, obtain, obtain2, 0);
            obtain2.readException();
            return obtain2.readInt();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }
}
