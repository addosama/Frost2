package pub.frost.wrappers;

import pub.frost.base.wrapping.AbstractWrapper;

public interface ClassEnum {
    Class<?> getClazz();

    static boolean isInstanceOf(AbstractWrapper instance, ClassEnum target) {
        return target.getClazz().isAssignableFrom(instance.getWrappedClass());
    }
}
