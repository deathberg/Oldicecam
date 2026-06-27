package com.xiaomi.vlive.binder;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Hand-written Binder proxy for "com.xiaomi.vlive.IMyBinderService".
 *
 * Wire-compatible with the original native daemon and clone/native/libvc_clone.cpp:
 * the transaction codes are pinned (11..25) and the parcel layout matches the
 * original obfuscated proxy (recovered as d1.f / C1391f in the reconstruction).
 *
 * The native daemon is the server; there is no Java Stub.
 */
public final class VcBinderClient {

    public static final String DESCRIPTOR = "com.xiaomi.vlive.IMyBinderService";

    // Pinned transaction codes (android.os.IBinder.FIRST_CALL_TRANSACTION == 1).
    public static final int TX_SET_VIDEO_SOURCE = 11;
    public static final int TX_COMMAND_12       = 12;
    public static final int TX_GET_STATUS_ARRAY = 13;
    public static final int TX_SELECT_VIDEO     = 14; // reply int == 4 => ok
    public static final int TX_GET_PLAY_STATE   = 15; // reply int == 5 => playing
    public static final int TX_SET_FLAG_B       = 16;
    public static final int TX_SET_FLAG_A       = 17;
    public static final int TX_SET_PLAY_ANGLE   = 18;
    public static final int TX_SET_MIRROR       = 19;
    public static final int TX_SET_PLAY_RANGE   = 22;
    public static final int TX_SET_AUTO_COLOR   = 24; // reply int == 14 => ok
    public static final int TX_REPLACE          = 25;

    private final IBinder remote;

    public VcBinderClient(IBinder remote) {
        this.remote = remote;
    }

    public IBinder asBinder() {
        return remote;
    }

    /** tx 11 — setVideoSource(path, loop, flag). */
    public int setVideoSource(String path, boolean loop, boolean flag) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(path);
            data.writeInt(loop ? 1 : 0);
            data.writeInt(flag ? 1 : 0);
            remote.transact(TX_SET_VIDEO_SOURCE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    /** tx 12 — no-arg command (stop/reset). */
    public int command12() throws RemoteException {
        return noArgCall(TX_COMMAND_12);
    }

    /** tx 13 — getStatusArray() -> int[]. */
    public int[] getStatusArray() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(TX_GET_STATUS_ARRAY, data, reply, 0);
            reply.readException();
            return reply.createIntArray();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    /** tx 14 — selectVideo(index, path); writeInt(index) then writeString(path). Returns 4 on success. */
    public int selectVideo(int index, String path) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(index);
            data.writeString(path);
            remote.transact(TX_SELECT_VIDEO, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    /** tx 15 — getPlayState(); 5 means playing. */
    public int getPlayState() throws RemoteException {
        return noArgCall(TX_GET_PLAY_STATE);
    }

    /** tx 16 — setFlagB(boolean). */
    public int setFlagB(boolean v) throws RemoteException {
        return boolCall(TX_SET_FLAG_B, v);
    }

    /** tx 17 — setFlagA(boolean); used for loop toggle. */
    public int setFlagA(boolean v) throws RemoteException {
        return boolCall(TX_SET_FLAG_A, v);
    }

    /** tx 18 — setPlayAngle(degrees). */
    public int setPlayAngle(int degrees) throws RemoteException {
        return intCall(TX_SET_PLAY_ANGLE, degrees);
    }

    /** tx 19 — setMirror(boolean). */
    public int setMirror(boolean mirror) throws RemoteException {
        return boolCall(TX_SET_MIRROR, mirror);
    }

    /** tx 22 — setPlayRange(beginUs, endUs). */
    public int setPlayRange(long beginUs, long endUs) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeLong(beginUs);
            data.writeLong(endUs);
            remote.transact(TX_SET_PLAY_RANGE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    /** tx 24 — setAutoColor(...); writeInt(mode), 4 floats, writeInt(colorMode). Returns 14 on success. */
    public int setAutoColor(int onOff, float x, float y, float intensity, float diameter, int colorMode)
            throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(onOff);
            data.writeFloat(x);
            data.writeFloat(y);
            data.writeFloat(intensity);
            data.writeFloat(diameter);
            data.writeInt(colorMode);
            remote.transact(TX_SET_AUTO_COLOR, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    /** tx 25 — replaceCommand(); original reply has no return int. */
    public int replaceCommand() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(TX_REPLACE, data, reply, 0);
            reply.readException();
            return 0;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private int noArgCall(int code) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(code, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private int intCall(int code, int value) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(value);
            remote.transact(code, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private int boolCall(int code, boolean value) throws RemoteException {
        return intCall(code, value ? 1 : 0);
    }
}
