package dev.icecam.app.runtime;

import android.content.Context;
import android.content.SharedPreferences;
import dev.icecam.app.TransformState;

public final class Persistence {
    private final SharedPreferences prefs;
    public Persistence(Context ctx) { prefs = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE); }
    public AppState load() {
        TransformState tx = TransformState.load(prefs);
        MediaState media = new MediaState(prefs.getInt("ActiveSlot", 1), prefs.getString("OriginalPlayFileMp4", ""), prefs.getString("PlayFileMp4", ""), prefs.getBoolean("PlayisLoop", true));
        BackendState backend = new BackendState(prefs.getBoolean("ReplacementActive", false), prefs.getString("IceCamState", "IDLE"), RuntimeTypes.OperationState.empty());
        return new AppState(tx, media, backend, new UiState(prefs.getLong("LastMarkerId", 0L), RuntimeTypes.Source.SYSTEM, prefs.getString("LastTransformReason", "")), new DiagnosticsState(0, "", ""));
    }
    public void save(AppState s) {
        s.transform.save(prefs);
        prefs.edit()
                .putInt("ActiveSlot", s.media.activeSlot)
                .putString("OriginalPlayFileMp4", s.media.originalPath)
                .putString("PlayFileMp4", s.media.playPath)
                .putBoolean("PlayisLoop", s.media.loop)
                .putBoolean("ReplacementActive", s.backend.replacementActive)
                .putString("IceCamState", s.backend.phase)
                .putLong("LastMarkerId", s.ui.markerId)
                .putString("LastTransformSource", s.ui.lastSource.name())
                .putString("LastTransformReason", s.ui.lastReason)
                .apply();
    }
}
