package pub.frost.client.feature.bindable.api;

public interface IBindable {
    int getKeybind();
    void onActive(int action);

    default boolean shouldActiveWhenRelease() {
        return false;
    }
}
