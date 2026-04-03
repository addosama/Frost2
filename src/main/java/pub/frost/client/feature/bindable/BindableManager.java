package pub.frost.client.feature.bindable;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventKeyInput;
import pub.frost.client.feature.bindable.api.IBindable;

import java.util.HashMap;
import java.util.Map;

public class BindableManager {
    private final Map<String, IBindable> bindableMap = new HashMap<>();

    public void register(IBindable bindable) {
        bindableMap.put(bindable.toString(), bindable);
    }
    public void unregister(IBindable bindable) {
        bindableMap.remove(bindable.toString());
    }

    @EventHandler(priority = 0)
    private void handleKeyInput(EventKeyInput event) {
        for (IBindable bindable : bindableMap.values()) {
            if (bindable.getKeybind() == event.getKey()) {
                if ((event.getAction() == 0 && !bindable.shouldActiveWhenRelease())) continue;
                bindable.onActive(event.getAction());
            }
        }
    }
}
