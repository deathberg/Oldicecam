package dev.icecam.app.runtime;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;

public final class StateStore {
    public interface Listener { void onState(AppState state); }
    private final Object lock = new Object();
    private final Handler main = new Handler(Looper.getMainLooper());
    private AppState state;
    private final List<Listener> listeners = new ArrayList<>();
    public StateStore(AppState initial) { state = initial == null ? AppState.empty() : initial; }
    public AppState get() { synchronized (lock) { return state; } }
    public void set(AppState next) {
        List<Listener> copy;
        synchronized (lock) { state = next; copy = new ArrayList<>(listeners); }
        main.post(() -> { for (Listener l : copy) l.onState(next); });
    }
    public void addListener(Listener l) { if (l == null) return; synchronized (lock) { listeners.add(l); } main.post(() -> l.onState(get())); }
}
