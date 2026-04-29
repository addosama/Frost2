package pub.frost.client.core;

interface Initializer {
    default <T> T registerToEventBus(T instance) {
        FrostCore.getEventBus().register(instance);
        return instance;
    }
}
