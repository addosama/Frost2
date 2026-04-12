package pub.frost.wrappers;

import pub.frost.base.wrapping.legacy.impl.InstanceWrapper;

@SuppressWarnings("unchecked")
public interface FakeInstanceWrapper<T> {
    default T cast() {
        if (this instanceof InstanceWrapper) {
            return (T) ((InstanceWrapper) this).getWrappedObject();
        }
        return null;
    }
    default <A> A cast(Object in, Class<A> clazz) {
        return clazz.cast(in);
    }
}
