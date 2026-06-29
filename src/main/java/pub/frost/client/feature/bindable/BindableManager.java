package pub.frost.client.feature.bindable;

import pub.frost.base.input.api.InputListener;
import pub.frost.client.feature.bindable.api.IBindable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BindableManager implements InputListener {
    private final Map<String, IBindable> bindableMap = new HashMap<>();

    public void register(IBindable bindable) {
        bindableMap.put(bindable.toString(), bindable);
    }
    public void unregister(IBindable bindable) {
        bindableMap.remove(bindable.toString());
    }

    public List<IBindable> getBindables(Predicate<IBindable> filter, Comparator<IBindable> comparator) {
        return bindableMap.values().stream().filter(
                filter
        ).sorted(
                comparator
        ).collect(Collectors.toList());
    }

    private void processInput(int mergedCode, boolean state) {
        for (IBindable bindable : bindableMap.values()) {
            if (bindable.getKeybind() == mergedCode) {
                if ((!state && !bindable.shouldActiveWhenRelease())) continue;
                bindable.onActive(state? 1 : 0);
            }
        }
    }

    @Override
    public int inputPriority() {
        return 80;
    }

    @Override
    public boolean onKey(int key, boolean state) {
        processInput(key, state);
        return true;
    }
    @Override
    public boolean onMouseButton(int button, boolean state) {
        processInput(-1 - button, state);
        return true;
    }
}
