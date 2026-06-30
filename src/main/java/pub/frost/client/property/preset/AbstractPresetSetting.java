package pub.frost.client.property.preset;

import lombok.AllArgsConstructor;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.ManualDescriptorProvider;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.descriptor.VisibilitySupplier;

@AllArgsConstructor
public abstract class AbstractPresetSetting<SELF> implements VisibilitySupplier, ManualDescriptorProvider {
    private VisibilitySupplier visibility;
    public SELF setVisibility(VisibilitySupplier visibility) {
        this.visibility = visibility;
        return (SELF) this;
    }

    public AbstractPresetSetting() {
        this(() -> true);
    }

    protected PropertyDescriptor getPropDescriptor(String keyPrefix, String key, String translationKey, AbstractProperty<?, ?> prop) {
        return new PropertyDescriptor(keyPrefix + key, prop, translationKey);
    }

    public boolean isVisible() {
        return visibility.isVisible();
    }
}
