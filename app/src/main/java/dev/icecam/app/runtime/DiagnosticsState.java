package dev.icecam.app.runtime;

public final class DiagnosticsState {
    public final long lastCommandId;
    public final String lastCommand;
    public final String lastResult;
    public DiagnosticsState(long lastCommandId, String lastCommand, String lastResult) {
        this.lastCommandId = lastCommandId;
        this.lastCommand = lastCommand == null ? "" : lastCommand;
        this.lastResult = lastResult == null ? "" : lastResult;
    }
    public DiagnosticsState command(long id, String name, String result) { return new DiagnosticsState(id, name, result); }
}
