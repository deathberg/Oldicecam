package dev.icecam.app.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class RuntimeTypes {
    private RuntimeTypes() {}

    public enum Source { MAIN, FLOAT, SYSTEM }
    public enum Result { OK, SKIPPED, FAILED }

    public static final class OperationState {
        public final Map<String, String> activeOperations;
        public OperationState(Map<String, String> activeOperations) {
            this.activeOperations = Collections.unmodifiableMap(new HashMap<>(activeOperations));
        }
        public static OperationState empty() { return new OperationState(Collections.emptyMap()); }
        public boolean busy() { return !activeOperations.isEmpty(); }
        public OperationState start(String opId, String label) {
            HashMap<String, String> next = new HashMap<>(activeOperations);
            next.put(opId, label);
            return new OperationState(next);
        }
        public OperationState finish(String opId) {
            HashMap<String, String> next = new HashMap<>(activeOperations);
            next.remove(opId);
            return new OperationState(next);
        }
        public String summary() { return activeOperations.toString(); }
    }
}
