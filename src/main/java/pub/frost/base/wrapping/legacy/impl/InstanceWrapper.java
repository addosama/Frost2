package pub.frost.base.wrapping.legacy.impl;

import lombok.Getter;
import pub.frost.base.wrapping.legacy.AbstractWrapper;

import java.lang.reflect.InvocationTargetException;

@Getter
public class InstanceWrapper extends AbstractWrapper {
    private final Object wrappedObject;

    public InstanceWrapper(Object wrappedObject) {
        super(wrappedObject.getClass());
        this.wrappedObject = wrappedObject;
    }

    public <T extends InstanceWrapper> T castTo(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor(Object.class).newInstance(getWrappedObject());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
