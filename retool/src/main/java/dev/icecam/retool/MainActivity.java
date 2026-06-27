package dev.icecam.retool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Standalone root companion for testicecam2.apk — deploy Frida, inject vcplax, pull artifacts.
 */
public final class MainActivity extends Activity {
    private AppLogger logger;
    private ReToolEngine engine;
    private TextView logView;
    private TextView statusView;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean tailRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new AppLogger(this);
        engine = new ReToolEngine(this, logger);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView title = new TextView(this);
        title.setText("RE Tool");
        title.setTextColor(UiKit.TEXT);
        title.setTextSize(20f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView hint = new TextView(this);
        hint.setText(
                "Companion for testicecam2.apk (root).\n" +
                "1) Install & open testicecam2\n" +
                "2) Setup → Attach (or Spawn)\n" +
                "3) Use camera app 1–2 min → Share log\n\n" +
                "Log: " + ReToolEngine.LOG_PUBLIC + "\n" +
                "Pull: " + ReToolEngine.PULL_DIR);
        hint.setTextColor(UiKit.MUTED);
        hint.setTextSize(12f);
        root.addView(hint);

        statusView = new TextView(this);
        statusView.setTextColor(UiKit.CYAN);
        statusView.setTextSize(10f);
        statusView.setTypeface(android.graphics.Typeface.MONOSPACE);
        statusView.setPadding(0, dp(8), 0, dp(4));
        root.addView(statusView);

        root.addView(row(
                btn("Setup", v -> runTask("setup", () -> engine.setupAll())),
                btn("Status", v -> runTask("status", () -> engine.status()))
        ));
        root.addView(row(
                btn("Attach vcplax", v -> runTask("attach", () -> {
                    String out = engine.startAttachCapture();
                    startTail();
                    return out;
                })),
                btn("Spawn vcplax", v -> runTask("spawn", () -> {
                    String out = engine.startSpawnCapture();
                    startTail();
                    return out;
                }))
        ));
        root.addView(row(
                btn("Pull binaries", v -> runTask("pull", () -> engine.pullRuntimeArtifacts())),
                btn("Stop", v -> runTask("stop", () -> {
                    stopTail();
                    return engine.stopCapture();
                }))
        ));
        root.addView(row(
                btn("Share log", v -> shareLog()),
                btn("Open pull dir", v -> openPullDir())
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
        appendUi("Ready · " + BuildInfo.BUILD_LABEL + "\nInstall testicecam2.apk, open it once, then Setup here.\n");
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
                logger.logBlock(tag, out);
                main.post(() -> {
                    refreshStatus();
                    toast(tag + " done");
                });
            } catch (Throwable t) {
                logger.log("err", tag + ": " + t);
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
                    String tail = engine.tailPublicLog(60);
                    main.post(() -> statusView.setText("Live:\n" + tail));
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
            String st = engine.status();
            main.post(() -> statusView.setText(st));
        }).start();
    }

    private void shareLog() {
        runTask("share", () -> {
            String content = Shell.su(
                    "cat " + ReToolEngine.LOG_PUBLIC + " 2>/dev/null || echo '(empty)'"
            ).out;
            main.post(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, content);
                send.putExtra(Intent.EXTRA_SUBJECT, "re_tool_capture.log");
                startActivity(Intent.createChooser(send, "Send RE log"));
            });
            return "bytes=" + content.length();
        });
    }

    private void openPullDir() {
        toast("Files: " + ReToolEngine.PULL_DIR);
        runTask("pull-ls", () -> Shell.su("ls -laR " + ReToolEngine.PULL_DIR + " 2>/dev/null || echo empty").all());
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
        b.setBackground(UiKit.fill(UiKit.PANEL, dp(12), 0x44ffffff));
        b.setTextColor(UiKit.TEXT);
        b.setTextSize(11f);
        return b;
    }

    private LinearLayout row(View... views) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(0, dp(3), 0, dp(3));
        for (View v : views) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            lp.setMargins(dp(2), 0, dp(2), 0);
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
