package pub.frost.client.feature.screen.components;

import pub.frost.base.event.impl.types.InputDevice;

public interface InputListener {
    default void onInput(InputDevice device, int code, int action) {}
}
