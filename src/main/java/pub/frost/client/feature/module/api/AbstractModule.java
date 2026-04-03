package pub.frost.client.feature.module.api;

import lombok.Getter;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.i18n.interfaces.Described;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.BooleanProperty;
import pub.frost.wrappers.shared.client.WMinecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class AbstractModule implements Named, Described {
    private final String key;
    private final ModuleCategory category;

    private final List<PropertyDescriptor> propertyList = new ArrayList<>();

    public final BooleanProperty enabled = new BooleanProperty(false);

    protected final WMinecraft.Instance mc;

    public AbstractModule() {
        Module annotation = this.getClass().getAnnotation(Module.class);
        if (FrostCore.DEBUG) assert annotation != null : "Missing @Module annotation";

        this.key = "modules." + annotation.key();
        this.category = annotation.category();

        this.mc = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance();

        enabled.setValueChangeListener((old, current) -> {
            if (current) {
                onEnabled();
                FrostCore.getInstance().getEventBus().register(AbstractModule.this);
            } else {
                FrostCore.getInstance().getEventBus().unregister(AbstractModule.this);
                onDisabled();
            }
        });
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
    public boolean isEnabled() {
        return this.enabled.get();
    }
    public void toggle() {
        setEnabled(!isEnabled());
    }

    protected void onEnabled() {}
    protected void onDisabled() {}

    public final void registerProperties() {
        if (FrostCore.DEBUG) assert propertyList.isEmpty();
        final String propKeyPrefix = "modules." + this.getKey() + ".props.";

        enabled.enableOverriding();
        enabled.setOverrideDisplayString(this::getName);
        propertyList.add(new PropertyDescriptor(propKeyPrefix + "enabled", enabled));

        this.propertyList.addAll(AbstractProperty.getPropertyDescriptorsForObject(
                this, propKeyPrefix, String::toLowerCase
        ));
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
