package pub.frost.base.wrapping.impl;

import lombok.Getter;
import pub.frost.base.wrapping.AbstractWrapper;

@Getter
public class InstanceWrapper extends AbstractWrapper {
    private final Object wrappedObject;

    public InstanceWrapper(Object wrappedObject) {
        super(wrappedObject.getClass());
        this.wrappedObject = wrappedObject;
    }
}
