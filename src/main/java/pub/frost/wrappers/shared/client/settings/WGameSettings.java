package pub.frost.wrappers.shared.client.settings;

import net.minecraft.client.settings.GameSettings;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WGameSettings extends Wrapper implements FakeInstanceWrapper<GameSettings> {
    public WGameSettings() {
        super(GameSettings.class);
    }

    public float getMouseSensitivity(Object instance) {
        return cast(instance).mouseSensitivity;
    }
}
