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

@Getter
public class AbstractModule implements Named, Described {
    private final String key;
    private final ModuleCategory category;

    private final List<PropertyDescriptor> propertyList = new ArrayList<>();

    public final BooleanProperty enabledProperty = new BooleanProperty(false);

    protected final WMinecraft.Instance mc;

    public AbstractModule() {
        Module annotation = this.getClass().getAnnotation(Module.class);
        if (FrostCore.DEBUG) assert annotation != null : "Missing @Module annotation";

        this.key = "modules." + annotation.key().toLowerCase();
        this.category = annotation.category();

        this.mc = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance();

        enabledProperty.setValueChangeListener((old, current) -> {
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
        this.enabledProperty.set(enabled);
    }
    public boolean isEnabled() {
        return this.enabledProperty.get();
    }
    public void toggle() {
        setEnabled(!isEnabled());
    }

    protected void onEnabled() {}
    protected void onDisabled() {}

    public final void initialize() {
        registerProperties();
        onInitialized();
    }

    private void registerProperties() {
        if (FrostCore.DEBUG) assert propertyList.isEmpty();
        final String propKeyPrefix = this.getKey() + ".props.";

        enabledProperty.enableOverriding();
        enabledProperty.setOverrideDisplayString(this::getName);
        propertyList.add(new PropertyDescriptor(propKeyPrefix + "enabled", enabledProperty) {
            @Override
            public String getName() {
                return FrostCore.getLocalizer().get("strings.enabled");
            }
        });

        this.propertyList.addAll(AbstractProperty.getPropertyDescriptorsForObject(
                this, propKeyPrefix, String::toLowerCase
        ));
    }

    protected void onInitialized() {}

    @Override
    public String toString() {
        return this.getKey();
    }
}
