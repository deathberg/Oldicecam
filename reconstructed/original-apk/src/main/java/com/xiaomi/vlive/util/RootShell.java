package com.xiaomi.vlive.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/** Reconstructed from {@code p037U.AbstractC0330t#m863q}. */
public final class RootShell {
    private static Process suProcess;
    private static DataOutputStream suOut;

    private RootShell() {}

    public static synchronized String exec(String command) {
        StringBuilder output = new StringBuilder();
        try {
            if (suProcess == null || suOut == null) {
                suProcess = Runtime.getRuntime().exec("su");
                suOut = new DataOutputStream(suProcess.getOutputStream());
            }
            String marker = "EOF_MARK_" + System.currentTimeMillis();
            suOut.writeBytes(command + "\n");
            suOut.writeBytes("echo " + marker + "\n");
            suOut.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (marker.equals(line)) break;
                output.append(line).append('\n');
            }
        } catch (IOException e) {
            output.append("ERROR: ").append(e.getMessage());
        }
        return output.toString().trim();
    }
}
