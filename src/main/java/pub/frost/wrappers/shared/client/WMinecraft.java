package pub.frost.wrappers.shared.client;

import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.base.wrapping.impl.StaticWrapper;
import pub.frost.client.core.FrostCore;

public class WMinecraft extends StaticWrapper {
    public WMinecraft(Class<?> wrappedClass) {
        super(wrappedClass);
    }

    private Instance cachedInstance;
    public Instance getInstance() {
        if (cachedInstance == null) {

        }
        return cachedInstance;
    }


    public class Instance extends InstanceWrapper {
        public Instance(Object wrappedObject) {
            super(wrappedObject);
        }
    }
}
