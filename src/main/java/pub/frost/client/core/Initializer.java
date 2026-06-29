package pub.frost.client.core;

import pub.frost.base.input.api.InputListener;

interface Initializer {
    default <T> T registerToEventBus(T instance) {
        FrostCore.getEventBus().register(instance);
        return instance;
    }
    default <T extends InputListener> T registerToInputManager(T instance) {
        FrostCore.getInputManager().register(instance);
        return instance;
    }
}
