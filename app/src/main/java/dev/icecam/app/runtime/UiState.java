package dev.icecam.app.runtime;

public final class UiState {
    public final long markerId;
    public final RuntimeTypes.Source lastSource;
    public final String lastReason;
    public UiState(long markerId, RuntimeTypes.Source lastSource, String lastReason) {
        this.markerId = markerId;
        this.lastSource = lastSource == null ? RuntimeTypes.Source.SYSTEM : lastSource;
        this.lastReason = lastReason == null ? "" : lastReason;
    }
    public UiState marker(long id, RuntimeTypes.Source source, String reason) { return new UiState(id, source, reason); }
}
