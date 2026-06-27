package com.xiaomi.vlive.binder;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Reconstructed from {@code p059d1.C1391f}.
 * Interface token: {@link #DESCRIPTOR}
 */
public final class MyBinderClient {
    public static final String DESCRIPTOR = "com.xiaomi.vlive.IMyBinderService";

    public static final int TX_PLAY_SOURCE = 11;
    public static final int TX_STOP_OR_QUERY = 12;
    public static final int TX_POLL_STATE = 13;
    public static final int TX_SET_MODE = 14;
    public static final int TX_GET_STATUS = 15;
    public static final int TX_SET_AUTO_ROTATE = 16;
    public static final int TX_SET_LOOP = 17;
    public static final int TX_SET_ANGLE = 18;
    public static final int TX_SET_MIRROR = 19;
    public static final int TX_SEEK_RANGE = 22;
    public static final int TX_TRANSFORM = 24;
    public static final int TX_HARD_RECOVERY = 25;

    public static final int STATUS_PLAYING = 5;
    public static final int OK_SET_SOURCE = 4;
    public static final int OK_TRANSFORM = 14;

    private final IBinder binder;

    public MyBinderClient(IBinder binder) {
        this.binder = binder;
    }

    public IBinder asBinder() {
        return binder;
    }

    public int playSource(String path, boolean unusedMirrorFlag, boolean loop) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(path);
            data.writeInt(0);
            data.writeInt(loop ? 1 : 0);
            binder.transact(TX_PLAY_SOURCE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int stopOrQuery() throws RemoteException {
        return transactEmpty(TX_STOP_OR_QUERY);
    }

    public int[] pollState() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            binder.transact(TX_POLL_STATE, data, reply, 0);
            reply.readException();
            return reply.createIntArray();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int setMode(int mode, String value) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(mode);
            data.writeString(value);
            binder.transact(TX_SET_MODE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int getStatus() throws RemoteException {
        return transactEmpty(TX_GET_STATUS);
    }

    public int setAutoRotate(boolean enabled) throws RemoteException {
        return transactBool(TX_SET_AUTO_ROTATE, enabled);
    }

    public int setLoop(boolean enabled) throws RemoteException {
        return transactBool(TX_SET_LOOP, enabled);
    }

    public int setAngle(int degrees) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(degrees);
            binder.transact(TX_SET_ANGLE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int setMirror(boolean enabled) throws RemoteException {
        return transactBool(TX_SET_MIRROR, enabled);
    }

    public int seekRange(long beginUs, long endUs) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeLong(beginUs);
            data.writeLong(endUs);
            binder.transact(TX_SEEK_RANGE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int setTransform(int mode, float x, float y, float intensity, float diameter, int colorMode)
            throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(mode);
            data.writeFloat(x);
            data.writeFloat(y);
            data.writeFloat(intensity);
            data.writeFloat(diameter);
            data.writeInt(colorMode);
            binder.transact(TX_TRANSFORM, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int hardRecovery() throws RemoteException {
        return transactEmpty(TX_HARD_RECOVERY);
    }

    private int transactEmpty(int code) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            binder.transact(code, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private int transactBool(int code, boolean value) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(value ? 1 : 0);
            binder.transact(code, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
