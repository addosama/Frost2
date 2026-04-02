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
    // Map<ListenerInstance, List<HandlerWrapper>> 用于 unregister
    private final Map<Object, List<HandlerWrapper>> listenerMap = new ConcurrentHashMap<>();

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public void register(Object listener) {
        List<HandlerWrapper> listenerHandlers = new ArrayList<>();

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) continue;

            Class<? extends Event> eventClass = (Class<? extends Event>) params[0];
            int priority = method.getAnnotation(EventHandler.class).priority();

            HandlerWrapper wrapper = new HandlerWrapper(listener, method, priority);

            handlers
                    .computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                    .add(wrapper);

            listenerHandlers.add(wrapper);
        }

        // 统一排序（只排相关的事件列表）
        for (HandlerWrapper wrapper : listenerHandlers) {
            Class<? extends Event> eventClass = wrapper.methodType;
            handlers.get(eventClass).sort(Comparator.comparingInt(h -> -h.priority));
        }

        listenerMap.put(listener, listenerHandlers);
    }

    public void unregister(Object listener) {
        List<HandlerWrapper> wrappers = listenerMap.remove(listener);
        if (wrappers == null) return;

        for (HandlerWrapper wrapper : wrappers) {
            List<HandlerWrapper> list = handlers.get(wrapper.methodType);
            if (list != null) {
                list.remove(wrapper);
            }
        }
    }

    public void call(Event event) {
        List<HandlerWrapper> list = handlers.get(event.getClass());
        if (list == null) return;

        for (HandlerWrapper wrapper : list) {
            wrapper.invoke(event);
        }
    }

    private static class HandlerWrapper {
        final Object instance;
        final MethodHandle handle;
        final int priority;
        final Class<? extends Event> methodType;

        HandlerWrapper(Object instance, Method method, int priority) {
            this.instance = instance;
            this.priority = priority;
            this.methodType = (Class<? extends Event>) method.getParameterTypes()[0];

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