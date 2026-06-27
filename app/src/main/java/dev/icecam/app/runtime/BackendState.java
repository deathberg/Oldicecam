package dev.icecam.app.runtime;

public final class BackendState {
    public final boolean replacementActive;
    public final String phase;
    public final RuntimeTypes.OperationState operations;
    public BackendState(boolean replacementActive, String phase, RuntimeTypes.OperationState operations) {
        this.replacementActive = replacementActive;
        this.phase = phase == null ? "IDLE" : phase;
        this.operations = operations == null ? RuntimeTypes.OperationState.empty() : operations;
    }
    public BackendState withPhase(String phase) { return new BackendState(replacementActive, phase, operations); }
    public BackendState withActive(boolean active, String phase) { return new BackendState(active, phase, operations); }
    public BackendState withOperations(RuntimeTypes.OperationState ops) { return new BackendState(replacementActive, phase, ops); }
}
