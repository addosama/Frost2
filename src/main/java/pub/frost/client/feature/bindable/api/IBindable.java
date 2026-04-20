package pub.frost.client.feature.bindable.api;

import java.util.function.Supplier;

public interface IBindable {
    int getKeybind();
    void onActive(int action);

    default boolean shouldActiveWhenRelease() {
        return false;
    }

    default Supplier<String> getDisplayNameSupplier() {
        return null;
    }
    default boolean isActive() {
        return false;
    }
}
