package dev.icecam.retool;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public final class Shell {
    public static final class Result {
        public final int code;
        public final String out;
        public final String err;

        Result(int c, String o, String e) {
            code = c;
            out = o == null ? "" : o;
            err = e == null ? "" : e;
        }

        public String all() {
            return "code=" + code + "\n" + out + (err.length() > 0 ? "\nERR:\n" + err : "");
        }
    }

    public static String q(String s) {
        return "'" + String.valueOf(s).replace("'", "'\\''") + "'";
    }

    public static Result sh(String script) {
        return run(new String[]{"sh"}, script);
    }

    public static Result su(String script) {
        return run(new String[]{"su"}, script);
    }

    private static Result run(String[] cmd, String script) {
        Process p = null;
        try {
            p = new ProcessBuilder(cmd).redirectErrorStream(false).start();
            OutputStream os = p.getOutputStream();
            os.write((script + "\nexit\n").getBytes("UTF-8"));
            os.flush();
            os.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            Thread t1 = pump(p.getInputStream(), out);
            Thread t2 = pump(p.getErrorStream(), err);
            int code = p.waitFor();
            t1.join(1500);
            t2.join(1500);
            return new Result(code, out.toString("UTF-8"), err.toString("UTF-8"));
        } catch (Throwable t) {
            return new Result(-1, "", String.valueOf(t));
        } finally {
            if (p != null) p.destroy();
        }
    }

    private static Thread pump(final java.io.InputStream in, final ByteArrayOutputStream out) {
        Thread t = new Thread(() -> {
            try {
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) >= 0) out.write(b, 0, n);
            } catch (Throwable ignored) {
            }
        });
        t.start();
        return t;
    }
}
