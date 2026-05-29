package pub.frost.client.property.preset;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

public class AbstractPresetSetting {
    @Accessors(chain = true) @Setter
    private Supplier<Boolean> visibility;
    public boolean isVisible() {
        return visibility.get();
    }


}
