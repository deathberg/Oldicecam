package dev.icecam.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

/** Reconciles prefs with live Binder TX13/TX15 for honest UI. */
public final class StreamStatus {
    public static final int TX15_PLAYING = 5;
    public static final int TX15_OK_SOURCE = 4;

    private StreamStatus() {}

    public static final class View {
        public final boolean prefActive;
        public final boolean nativeLive;
        public final boolean applying;
        public final boolean startStopBusy;
        public final String phase;
        public final int tx15;
        public final int tx13Flag;
        public final int tx13W;
        public final int tx13H;

        View(boolean prefActive, boolean nativeLive, boolean applying, boolean startStopBusy,
             String phase, int tx15, int tx13Flag, int tx13W, int tx13H) {
            this.prefActive = prefActive;
            this.nativeLive = nativeLive;
            this.applying = applying;
            this.startStopBusy = startStopBusy;
            this.phase = phase == null ? "IDLE" : phase;
            this.tx15 = tx15;
            this.tx13Flag = tx13Flag;
            this.tx13W = tx13W;
            this.tx13H = tx13H;
        }

        /** Best-effort live flag: native counters win over stale prefs. */
        public boolean live() {
            if (nativeLive) return true;
            if (startStopBusy) return prefActive;
            if (applying) return prefActive;
            return prefActive && !"PLAY_ERROR".equals(phase) && !"GEOMETRY_APPLY_FAIL".equals(phase);
        }

        public boolean desynced() {
            return prefActive && !nativeLive && !applying && !startStopBusy
                    && !phase.startsWith("START") && !"RECOVERING_BACKEND".equals(phase);
        }

        public String headline() {
            if (startStopBusy) return "BUSY";
            if (applying) return nativeLive ? "APPLY…" : "SYNC…";
            if (nativeLive) return "LIVE";
            if (desynced()) return "STALE";
            if (prefActive) return "WAIT";
            return "IDLE";
        }

        public String detail() {
            return String.format(Locale.US, "TX15=%d TX13[%d %dx%d] %s",
                    tx15, tx13Flag, tx13W, tx13H, phase);
        }
    }

    public static View read(Context ctx, StreamController stream, SharedPreferences prefs, VliveBinderClient binder) {
        boolean prefActive = prefs.getBoolean("ReplacementActive", false);
        String phase = prefs.getString("IceCamState", "IDLE");
        boolean applying = stream.isApplying();
        boolean startStopBusy = stream.isStartStopBusy();

        int tx15 = -999;
        int tx13Flag = 0;
        int tx13W = 0;
        int tx13H = 0;
        boolean nativeLive = false;

        if (binder.connected()) {
            tx15 = binder.getStatus();
            int[] poll = binder.pollState();
            if (poll.length > 0) tx13Flag = poll[0];
            if (poll.length > 1) tx13W = poll[1];
            if (poll.length > 2) tx13H = poll[2];
            nativeLive = tx13Flag == 1 || tx15 == TX15_PLAYING || tx15 == TX15_OK_SOURCE;
        }

        return new View(prefActive, nativeLive, applying, startStopBusy, phase,
                tx15, tx13Flag, tx13W, tx13H);
    }
}
