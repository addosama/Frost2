package pub.frost.base.wrapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class WrapperManager {
    private final Map<Class<? extends Wrapper>, Wrapper> cachedWrappers;

    public WrapperManager() {
        this.cachedWrappers = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Wrapper> T getWrapper(Class<T> clazz) {
        return (T) cachedWrappers.computeIfAbsent(
                clazz,
                wrapperClazz -> {
                    try {
                        Constructor<? extends Wrapper> constructor = wrapperClazz.getConstructor();
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
