package pub.frost.wrappers;

import pub.frost.base.wrapping.legacy.impl.StaticWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class WrapperManager {
    private final Map<Class<? extends StaticWrapper>, StaticWrapper> cachedWrappers;

    public WrapperManager() {
        this.cachedWrappers = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends StaticWrapper> T getWrapper(Class<T> clazz) {
        return (T) cachedWrappers.computeIfAbsent(
                clazz,
                wrapperClazz -> {
                    try {
                        Constructor<? extends StaticWrapper> constructor = wrapperClazz.getConstructor();
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
