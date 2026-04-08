package pub.frost.base.event.api;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.api.interfaces.Event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class EventBus {
    // Map<EventClass, List<HandlerWrapper>>
    private final Map<Class<? extends Event>, CopyOnWriteArrayList<HandlerWrapper>> handlers = new ConcurrentHashMap<>();
    // identity key，避免 listener equals/hashCode override 影响 unregister
    private final Map<IdentityKey, List<HandlerWrapper>> listenerMap = new ConcurrentHashMap<>();
    // 保护 register/unregister 的复合修改原子性
    private final Object mutationLock = new Object();

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public void register(Object listener) {
        Objects.requireNonNull(listener, "listener");

        List<HandlerWrapper> newWrappers = scanHandlers(listener);
        IdentityKey key = new IdentityKey(listener);

        synchronized (mutationLock) {
            // 防止重复注册同一实例导致旧 wrapper 残留
            List<HandlerWrapper> oldWrappers = listenerMap.remove(key);
            removeWrappers(oldWrappers);

            if (newWrappers.isEmpty()) {
                return;
            }

            Set<Class<? extends Event>> touchedEventTypes = new HashSet<>();
            for (HandlerWrapper wrapper : newWrappers) {
                handlers.computeIfAbsent(wrapper.methodType, k -> new CopyOnWriteArrayList<>()).add(wrapper);
                touchedEventTypes.add(wrapper.methodType);
            }

            // 仅排序受影响的事件列表
            for (Class<? extends Event> eventClass : touchedEventTypes) {
                CopyOnWriteArrayList<HandlerWrapper> list = handlers.get(eventClass);
                if (list != null) {
                    list.sort(Comparator.comparingInt((HandlerWrapper h) -> h.priority).reversed());
                }
            }

            listenerMap.put(key, newWrappers);
        }
    }

    public void unregister(Object listener) {
        Objects.requireNonNull(listener, "listener");

        synchronized (mutationLock) {
            List<HandlerWrapper> wrappers = listenerMap.remove(new IdentityKey(listener));
            removeWrappers(wrappers);
        }
    }

    public void call(Event event) {
        List<HandlerWrapper> list = handlers.get(event.getClass());
        if (list == null) return;

        for (HandlerWrapper wrapper : list) {
            wrapper.invoke(event);
        }
    }

    private List<HandlerWrapper> scanHandlers(Object listener) {
        List<HandlerWrapper> result = new ArrayList<>();

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isBridge() || method.isSynthetic()) continue;
            if (!method.isAnnotationPresent(EventHandler.class)) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) continue;

            Class<? extends Event> eventClass = (Class<? extends Event>) params[0];
            int priority = method.getAnnotation(EventHandler.class).priority();

            result.add(new HandlerWrapper(listener, method, priority, eventClass));
        }

        return result;
    }

    private void removeWrappers(List<HandlerWrapper> wrappers) {
        if (wrappers == null || wrappers.isEmpty()) return;

        for (HandlerWrapper wrapper : wrappers) {
            CopyOnWriteArrayList<HandlerWrapper> list = handlers.get(wrapper.methodType);
            if (list != null) {
                list.remove(wrapper);
                if (list.isEmpty()) {
                    handlers.remove(wrapper.methodType, list);
                }
            }
        }
    }

    private static final class IdentityKey {
        private final Object ref;
        private final int hash;

        IdentityKey(Object ref) {
            this.ref = Objects.requireNonNull(ref, "ref");
            this.hash = System.identityHashCode(ref);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof IdentityKey && ((IdentityKey) obj).ref == this.ref);
        }
    }

    private static class HandlerWrapper {
        final Object instance;
        final MethodHandle handle;
        final int priority;
        final Class<? extends Event> methodType;

        HandlerWrapper(Object instance, Method method, int priority, Class<? extends Event> methodType) {
            this.instance = instance;
            this.priority = priority;
            this.methodType = methodType;

            try {
                method.setAccessible(true);
                this.handle = LOOKUP.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to create MethodHandle for " + method, e);
            }
        }

        void invoke(Event event) {
            try {
                handle.invoke(instance, event);
            } catch (Throwable t) {
                throw new RuntimeException("Error invoking event handler", t);
            }
        }
    }
}