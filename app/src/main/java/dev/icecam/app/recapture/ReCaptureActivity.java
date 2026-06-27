package dev.icecam.app.recapture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.icecam.app.AppLogger;
import dev.icecam.app.BuildInfo;
import dev.icecam.app.UiKit;

/**
 * One-tap RE log capture — replaces Termux + frida-inject workflow.
 * Log: /sdcard/Download/icecam_re_capture.log
 */
public final class ReCaptureActivity extends Activity {
    private AppLogger logger;
    private FridaReCapture capture;
    private TextView logView;
    private TextView statusView;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean tailRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new AppLogger(this);
        capture = new FridaReCapture(this, logger);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView title = new TextView(this);
        title.setText("IceCam RE Capture");
        title.setTextColor(UiKit.TEXT);
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView hint = new TextView(this);
        hint.setText("Root + vcplax Frida log for reverse engineering.\n" +
                "Log: " + FridaReCapture.LOG_PUBLIC);
        hint.setTextColor(UiKit.MUTED);
        hint.setTextSize(12f);
        root.addView(hint);

        statusView = new TextView(this);
        statusView.setTextColor(UiKit.CYAN);
        statusView.setTextSize(11f);
        statusView.setPadding(0, dp(8), 0, dp(4));
        root.addView(statusView);

        root.addView(row(
                btn("1. Setup", v -> runTask("setup", () -> capture.setupAll())),
                btn("Status", v -> runTask("status", () -> capture.status()))
        ));
        root.addView(row(
                btn("2. Attach vcplax", v -> runTask("attach", () -> {
                    String out = capture.startAttachCapture();
                    startTail();
                    return out;
                })),
                btn("2b. Spawn vcplax", v -> runTask("spawn", () -> {
                    String out = capture.startSpawnCapture();
                    startTail();
                    return out;
                }))
        ));
        root.addView(row(
                btn("Stop", v -> runTask("stop", () -> {
                    stopTail();
                    return capture.stopCapture();
                })),
                btn("Share log", v -> shareLog())
        ));

        logView = new TextView(this);
        logView.setTextColor(UiKit.TEXT);
        logView.setTextSize(10f);
        logView.setTypeface(android.graphics.Typeface.MONOSPACE);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(logView);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(root);
        logger.setListener(text -> main.post(() -> logView.setText(text)));
        appendUi("Ready · " + BuildInfo.BUILD_LABEL + "\nOpen main IceCam app, then Attach or Spawn.\n");
        refreshStatus();
    }

    @Override
    protected void onDestroy() {
        stopTail();
        super.onDestroy();
    }

    private void runTask(String tag, Task task) {
        appendUi("\n>>> " + tag + " ...\n");
        new Thread(() -> {
            try {
                String out = task.run();
                logger.logBlock("re/" + tag, out);
                main.post(() -> {
                    refreshStatus();
                    toast(tag + " done");
                });
            } catch (Throwable t) {
                logger.log("re/err", tag + ": " + t);
                main.post(() -> toast(tag + " failed: " + t.getMessage()));
            }
        }).start();
    }

    private void startTail() {
        if (tailRunning) return;
        tailRunning = true;
        new Thread(() -> {
            while (tailRunning) {
                try {
                    String tail = capture.tailPublicLog(80);
                    main.post(() -> statusView.setText("Live log tail:\n" + tail));
                    Thread.sleep(2000);
                } catch (Throwable ignored) {
                    break;
                }
            }
        }).start();
    }

    private void stopTail() {
        tailRunning = false;
    }

    private void refreshStatus() {
        new Thread(() -> {
            String st = capture.status();
            main.post(() -> statusView.setText(st));
        }).start();
    }

    private void shareLog() {
        runTask("share", () -> {
            String content = dev.icecam.app.Shell.su(
                    "cat " + FridaReCapture.LOG_PUBLIC + " 2>/dev/null || echo '(empty)'"
            ).out;
            main.post(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, content);
                send.putExtra(Intent.EXTRA_SUBJECT, "icecam_re_capture.log");
                startActivity(Intent.createChooser(send, "Send RE log"));
            });
            return "share bytes=" + content.length();
        });
    }

    private void appendUi(String s) {
        logView.append(s);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    private Button btn(String label, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        b.setBackground(UiKit.fill(UiKit.PANEL, dp(14), 0x44ffffff));
        b.setTextColor(UiKit.TEXT);
        return b;
    }

    private LinearLayout row(View... views) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(0, dp(4), 0, dp(4));
        for (View v : views) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            r.addView(v, lp);
        }
        return r;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private interface Task {
        String run() throws Exception;
    }
}
