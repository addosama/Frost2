package pub.frost.base.wrapping;

import lombok.Getter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.lang.invoke.MethodHandles.Lookup.*;
import static java.lang.invoke.MethodHandles.Lookup.PRIVATE;

public class Wrapper {
    @Getter
    protected final Class<?> targetClass;
    protected final MethodHandles.Lookup lookup;

    public Wrapper(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.lookup = createLookup(targetClass);
    }

    public boolean isTarget(Object object) {
        return isTarget(object.getClass());
    }

    public boolean isTarget(Class<?> clazz) {
        return targetClass.isAssignableFrom(clazz);
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
