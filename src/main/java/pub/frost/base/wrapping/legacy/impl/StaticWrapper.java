package pub.frost.base.wrapping.legacy.impl;

import lombok.Getter;
import pub.frost.base.wrapping.legacy.AbstractWrapper;

@Getter
public class StaticWrapper extends AbstractWrapper {
    public StaticWrapper(Class<?> wrappedClass) {
        super(wrappedClass);
    }

//    private final Map<ImmutablePair<String, Class<?>>, MethodHandle> cachedFieldGetters = new HashMap<>();
//    private final Map<ImmutablePair<String, MethodType>, MethodHandle> cachedMethods = new HashMap<>();
//
//    @SuppressWarnings("unchecked")
//    protected <T> T getField(String name, Class<T> type) {
//        try {
//            return ((T) cachedFieldGetters.computeIfAbsent(
//                    new ImmutablePair<>(name, type),
//                    pair -> {
//                        try {
//                            return lookup.findStaticGetter(
//                                    getWrappedClass(),
//                                    pair.getLeft(), pair.getRight()
//                            );
//                        } catch (NoSuchFieldException | IllegalAccessException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//            ).invoke());
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    protected Object invokeMethod(String name, MethodType type, Object... args) throws Throwable {
//        return cachedMethods.computeIfAbsent(
//                new ImmutablePair<>(name, type),
//                pair -> {
//                    try {
//                        return lookup.findStatic(
//                                getWrappedClass(),
//                                pair.getLeft(), pair.getRight()
//                        );
//                    } catch (NoSuchMethodException | IllegalAccessException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//        ).invokeWithArguments(args);
//    }
}
