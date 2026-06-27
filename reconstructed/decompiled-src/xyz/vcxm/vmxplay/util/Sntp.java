package xyz.vcxm.vmxplay.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import kotlin.UByte;

/* loaded from: classes4.dex */
public final class Sntp {
    private Sntp() {
    }

    public static Long getOffsetSecondsAny(String[] hosts, int timeoutMs) {
        if (hosts == null) {
            return null;
        }
        for (String h : hosts) {
            Long off = getOffsetSeconds(h, timeoutMs);
            if (off != null) {
                return off;
            }
        }
        return null;
    }

    public static Long getOffsetSeconds(String host, int timeoutMs) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            try {
                socket.setSoTimeout(timeoutMs);
                InetAddress addr = InetAddress.getByName(host);
                byte[] buf = new byte[48];
                buf[0] = 35;
                long t12 = System.currentTimeMillis();
                writeTimeStamp(buf, 40, t12);
                DatagramPacket req = new DatagramPacket(buf, buf.length, addr, 123);
                socket.send(req);
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.receive(resp);
                long t4 = System.currentTimeMillis();
                long t2 = readTimeStamp(buf, 32);
                long t3 = readTimeStamp(buf, 40);
                long offsetMs = ((t2 - t12) + (t3 - t4)) / 2;
                Long valueOf = Long.valueOf(offsetMs / 1000);
                socket.close();
                return valueOf;
            } catch (Exception e2) {
                if (socket == null) {
                    return null;
                }
                socket.close();
                return null;
            } catch (Throwable th) {
                th = th;
                if (socket != null) {
                    socket.close();
                }
                throw th;
            }
        } catch (Exception e3) {
        } catch (Throwable th2) {
            th = th2;
        }
    }

    private static void writeTimeStamp(byte[] buf, int ofs, long ms) {
        long seconds = (ms / 1000) + 2208988800L;
        long fraction = ((ms % 1000) * 4294967296L) / 1000;
        buf[ofs] = (byte) ((seconds >> 24) & 255);
        buf[ofs + 1] = (byte) ((seconds >> 16) & 255);
        buf[ofs + 2] = (byte) ((seconds >> 8) & 255);
        buf[ofs + 3] = (byte) (seconds & 255);
        buf[ofs + 4] = (byte) ((fraction >> 24) & 255);
        buf[ofs + 5] = (byte) ((fraction >> 16) & 255);
        buf[ofs + 6] = (byte) ((fraction >> 8) & 255);
        buf[ofs + 7] = (byte) (fraction & 255);
    }

    private static long read32(byte[] b2, int o2) {
        long v2 = 0;
        for (int i = 0; i < 4; i++) {
            v2 = (v2 << 8) | (b2[o2 + i] & UByte.MAX_VALUE);
        }
        return v2;
    }

    private static long readTimeStamp(byte[] b2, int o2) {
        long seconds = read32(b2, o2) - 2208988800L;
        long frac = read32(b2, o2 + 4);
        return (seconds * 1000) + ((1000 * frac) / 4294967296L);
    }
}
