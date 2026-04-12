package pub.frost.base.wrapping.legacy;

import lombok.Getter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.lang.invoke.MethodHandles.Lookup.*;

public abstract class AbstractWrapper {
    @Getter
    protected final Class<?> wrappedClass;
    protected final MethodHandles.Lookup lookup;

    public AbstractWrapper(Class<?> wrappedClass) {
        this.wrappedClass = wrappedClass;
        this.lookup = createLookup(wrappedClass);
    }

    private static MethodHandles.Lookup createLookup(Class<?> targetClass) {
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance(
                    targetClass, PUBLIC | PACKAGE | PROTECTED | PRIVATE
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
