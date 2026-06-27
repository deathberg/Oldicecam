package dev.icecam.app.runtime;

import dev.icecam.app.TransformState;

public final class AppState {
    public final TransformState transform;
    public final MediaState media;
    public final BackendState backend;
    public final UiState ui;
    public final DiagnosticsState diagnostics;
    public AppState(TransformState transform, MediaState media, BackendState backend, UiState ui, DiagnosticsState diagnostics) {
        this.transform = transform == null ? new TransformState() : transform;
        this.media = media == null ? new MediaState(1, "", "", true) : media;
        this.backend = backend == null ? new BackendState(false, "IDLE", RuntimeTypes.OperationState.empty()) : backend;
        this.ui = ui == null ? new UiState(0, RuntimeTypes.Source.SYSTEM, "init") : ui;
        this.diagnostics = diagnostics == null ? new DiagnosticsState(0, "", "") : diagnostics;
    }
    public static AppState empty() {
        return new AppState(new TransformState(), new MediaState(1, "", "", true), new BackendState(false, "IDLE", RuntimeTypes.OperationState.empty()), new UiState(0, RuntimeTypes.Source.SYSTEM, "init"), new DiagnosticsState(0, "", ""));
    }
    public AppState withTransform(TransformState t) { return new AppState(t, media, backend, ui, diagnostics); }
    public AppState withMedia(MediaState m) { return new AppState(transform, m, backend, ui, diagnostics); }
    public AppState withBackend(BackendState b) { return new AppState(transform, media, b, ui, diagnostics); }
    public AppState withUi(UiState u) { return new AppState(transform, media, backend, u, diagnostics); }
    public AppState withDiagnostics(DiagnosticsState d) { return new AppState(transform, media, backend, ui, d); }
}
