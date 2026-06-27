package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;

public final class RootBootstrap {
    public static final String FIXED_SERVICE_NAME = "privsam_service";
    private final Context ctx;
    private final AppLogger log;
    public RootBootstrap(Context c, AppLogger logger) { ctx = c.getApplicationContext(); log = logger; }

    public String serverName() {
        SharedPreferences p = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        String s = p.getString("ServerName", FIXED_SERVICE_NAME);
        // v10: stable Binder service name. Random names break reconnects after media changes.
        if (s == null || s.trim().isEmpty() || !FIXED_SERVICE_NAME.equals(s.trim())) {
            s = FIXED_SERVICE_NAME;
            p.edit().putString("ServerName", s).apply();
        }
        return s;
    }

    public String resetServerName() {
        ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE).edit().putString("ServerName", FIXED_SERVICE_NAME).apply();
        log.log("root", "ServerName fixed=" + FIXED_SERVICE_NAME);
        return FIXED_SERVICE_NAME;
    }

    public String bootstrap() {
        NativeExtractor.Result ex = NativeExtractor.extract(ctx, log);
        String server = serverName();
        String src = ex.dir.getAbsolutePath();
        String script = "set -x\n" +
                "SRC=" + Shell.q(src) + "\n" +
                "SERVER=" + Shell.q(server) + "\n" +
                "echo selected_abi=" + Shell.q(ex.abi) + " server=$SERVER src=$SRC\n" +
                "id\n" +
                "getenforce 2>/dev/null || true\n" +
                "setenforce 0 2>/dev/null || true\n" +
                "killall vcplax 2>/dev/null || true\n" +
                "rm -rf /data/camera /data/samera\n" +
                "mkdir -p /data/camera /data/local/tmp/icecam\n" +
                "chattr -i /data/camera 2>/dev/null || true\n" +
                "cp -f $SRC/libvc.so /data/libvc.so\n" +
                "cp -f $SRC/libshadowhook.so /data/libvc++.so\n" +
                "cp -f $SRC/libshadowhook.so /data/camera/libshadowhook.so\n" +
                "cp -f $SRC/libvc.so /data/camera/libvc.so\n" +
                "cp -f $SRC/vcplax.so /data/camera/vcplax\n" +
                "cp -f $SRC/vcplax.so /data/vcplax 2>/dev/null || true\n" +
                "chmod 700 /data/camera/vcplax /data/vcplax 2>/dev/null || true\n" +
                "chmod 644 /data/libvc.so /data/libvc++.so /data/camera/libvc.so /data/camera/libshadowhook.so 2>/dev/null || true\n" +
                "rm -f /data/camera/vcplax.log /data/camera/vcplax.err\n" +
                "export LD_LIBRARY_PATH=/data/camera:/data:/system/lib64:/system_ext/lib64:/vendor/lib64:/system/lib:/system_ext/lib:/vendor/lib:$LD_LIBRARY_PATH\n" +
                "export ICECAM_SERVER=$SERVER\n" +
                "EXEC=/data/vcplax\n" +
                "[ -x /data/vcplax ] || EXEC=/data/camera/vcplax\n" +
                "echo ---launch $EXEC $SERVER---\n" +
                "nohup $EXEC $SERVER >/data/camera/vcplax.log 2>/data/camera/vcplax.err &\n" +
                "echo spawned_pid=$! exec=$EXEC\n" +
                "for i in 1 2 3 4 5; do sleep 1; service check $SERVER 2>&1 | grep -qi found && break; done\n" +
                "echo ---process---\nps -A | grep -i vcplax || ps | grep -i vcplax || true\n" +
                "echo ---expected-service---\nservice check $SERVER 2>&1 || true\n" +
                "echo ---service-list-filtered---\nservice list 2>/dev/null | grep -iE \"$SERVER|vlive|camera|media|ice|vcplax\" || true\n" +
                "echo ---files---\nls -l /data/camera 2>&1; ls -l /data/vcplax /data/libvc.so /data/libvc++.so 2>&1 || true\n" +
                "echo ---vcplax.log---\ncat /data/camera/vcplax.log 2>/dev/null || true\n" +
                "echo ---vcplax.err---\ncat /data/camera/vcplax.err 2>/dev/null || true\n" +
                "echo ---selinux-after---\ngetenforce 2>/dev/null || true\n";
        Shell.Result r = Shell.su(script);
        String all = r.all();
        log.logBlock("root", all);
        return all;
    }

    public String restoreCamera() {
        String server = serverName();
        String script = "set -x\n" +
                "SERVER=" + Shell.q(server) + "\n" +
                "echo restore_server=$SERVER\n" +
                "id\n" +
                "getenforce 2>/dev/null || true\n" +
                "echo ---soft-stop-binder---\n" +
                "service check $SERVER 2>&1 || true\n" +
                "echo ---kill-daemon---\n" +
                "killall vcplax 2>/dev/null || true\n" +
                "pkill -f /data/vcplax 2>/dev/null || true\n" +
                "pkill -f /data/camera/vcplax 2>/dev/null || true\n" +
                "sleep 1\n" +
                "echo ---after-process---\n" +
                "ps -A | grep -i vcplax || ps | grep -i vcplax || true\n" +
                "echo ---after-service---\n" +
                "service check $SERVER 2>&1 || true\n" +
                "echo ---camera-services---\n" +
                "service list 2>/dev/null | grep -iE \"camera|media.camera|$SERVER|vcplax\" || true\n" +
                "echo restore_done\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("restore", r.all());
        return r.all();
    }

    public String status() {
        String server = serverName();
        String script = "SERVER=" + Shell.q(server) + "\n" +
                "id\n" +
                "echo server=$SERVER\n" +
                "echo ---selinux---\ngetenforce 2>/dev/null || true\n" +
                "echo ---process---\nps -A | grep -i vcplax || ps | grep -i vcplax || true\n" +
                "echo ---expected-service---\nservice check $SERVER 2>&1 || true\n" +
                "echo ---service-list-filtered---\nservice list 2>/dev/null | grep -iE \"$SERVER|vlive|camera|media|ice|vcplax\" || true\n" +
                "echo ---files---\nls -l /data/camera 2>&1; ls -l /data/vcplax /data/libvc.so /data/libvc++.so 2>&1 || true\n" +
                "echo ---vcplax-log---\ntail -160 /data/camera/vcplax.log 2>/dev/null || true\n" +
                "echo ---vcplax-err---\ntail -160 /data/camera/vcplax.err 2>/dev/null || true\n" +
                "echo ---logcat-native---\nlogcat -d -t 220 2>/dev/null | grep -iE \"icecam|vcplax|vlive|libvc|shadowhook|binder|servicemanager|avc: denied|Parcel\" || true\n";
        Shell.Result r = Shell.su(script);
        log.logBlock("status", r.all());
        return r.all();
    }
}
