package pub.frost.base.input;

import pub.frost.base.input.api.InputListener;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class InputManager {
    private final Set<InputListener> listenerSet = new ConcurrentSkipListSet<>(Comparator.comparingInt(l -> -l.inputPriority()));

    public void register(InputListener listener) {
        listenerSet.add(listener);
    }
    public void unregister(InputListener listener) {
        listenerSet.remove(listener);
    }

    public boolean onMouseButton(int button, boolean state) {
        for (InputListener listener : listenerSet) {
            if (!listener.onMouseButton(button, state)) return false;
        }
        return true;
    }
    public boolean onMouseScroll(float scrollX, float scrollY) {
        for (InputListener listener : listenerSet) {
            if (!listener.onMouseScroll(scrollX, scrollY)) return false;
        }
        return true;
    }

    public boolean onKey(int key, boolean state) {
        for (InputListener listener : listenerSet) {
            if (!listener.onKey(key, state)) return false;
        }
        return true;
    }
    public boolean onChar(char c) {
        for (InputListener listener : listenerSet) {
            if (!listener.onChar(c)) return false;
        }
        return true;
    }
}
