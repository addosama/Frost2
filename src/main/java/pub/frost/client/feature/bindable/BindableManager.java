package pub.frost.client.feature.bindable;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventKeyInput;
import pub.frost.client.feature.bindable.api.IBindable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BindableManager {
    private final Map<String, IBindable> bindableMap = new HashMap<>();

    public void register(IBindable bindable) {
        bindableMap.put(bindable.toString(), bindable);
    }
    public void unregister(IBindable bindable) {
        bindableMap.remove(bindable.toString());
    }

    @EventHandler(priority = 5)
    private void handleKeyInput(EventKeyInput event) {
        if (event.getKey() == 0) return;
        if (event.isCancelled()) return;
        for (IBindable bindable : bindableMap.values()) {
            if (bindable.getKeybind() == event.getKey()) {
                if ((event.getAction() == 0 && !bindable.shouldActiveWhenRelease())) continue;
                bindable.onActive(event.getAction());
            }
        }
    }

    public List<IBindable> getBindables(Predicate<IBindable> filter, Comparator<IBindable> comparator) {
        return bindableMap.values().stream().filter(
                filter
        ).sorted(
                comparator
        ).collect(Collectors.toList());
    }
}
