package com.xiaomi.vlive.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Persistent root shell helper (clean-room rebuild of the original's exec("su") bridge).
 *
 * Opens a single {@code su} process and reuses it; each {@link #exec(String)} runs a
 * command and reads stdout up to a unique EOF marker. Mirrors the original
 * AbstractC0330t.q(String) behavior recovered in the reconstruction.
 */
public final class RootShell {

    private static Process suProcess;
    private static DataOutputStream suStdin;

    private RootShell() {}

    public static synchronized String exec(String command) {
        StringBuilder out = new StringBuilder();
        try {
            if (suProcess == null || suStdin == null) {
                try {
                    suProcess = Runtime.getRuntime().exec("su");
                    suStdin = new DataOutputStream(suProcess.getOutputStream());
                } catch (IOException e) {
                    return "";
                }
            }
            String marker = "EOF_MARK_" + System.currentTimeMillis();
            suStdin.writeBytes(command + "\n");
            suStdin.writeBytes("echo " + marker + "\n");
            suStdin.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(marker)) break;
                out.append(line).append("\n");
            }
        } catch (IOException e) {
            out.append("ERROR: ").append(e.getMessage());
        }
        return out.toString().trim();
    }

    public static boolean hasRoot() {
        return exec("id").contains("uid=0");
    }
}
