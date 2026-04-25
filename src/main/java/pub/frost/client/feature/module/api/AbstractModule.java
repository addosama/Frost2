package pub.frost.client.feature.module.api;

import lombok.Getter;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.i18n.interfaces.Described;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.wrappers.shared.client.WMinecraft;

import java.util.*;

@Getter
public class AbstractModule implements Wrappers, Named, Described {
    private final String key;
    private final ModuleCategory category;

    private final Map<String, PropertyDescriptor> propertyMap = new LinkedHashMap<>();
    private final Map<String, PropertyDescriptor> flattenedPropertyMap = new HashMap<>();

    public final BooleanProperty enabledProperty = new BooleanProperty(false);

    protected final Object mc;
    protected final WMinecraft mcWrapper = Minecraft;

    public AbstractModule() {
        Module annotation = this.getClass().getAnnotation(Module.class);
        if (FrostCore.DEBUG) assert annotation != null : "Missing @Module annotation";

        this.key = "modules." + annotation.key().toLowerCase();
        this.category = annotation.category();

        this.mc = Minecraft.getInstance();

        enabledProperty.setValueChangeListener((old, current) -> {
            if (current) {
                onEnabled();
                registerToEventBus();
            } else {
                unregisterFromEventBus();
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

    protected void registerToEventBus() {
        FrostCore.getInstance().getEventBus().register(AbstractModule.this);
    }
    protected void unregisterFromEventBus() {
        FrostCore.getInstance().getEventBus().unregister(AbstractModule.this);
    }

    public final void initialize() {
        registerProperties();
        onInitialized();
    }

    private void registerProperties() {
        if (FrostCore.DEBUG) assert propertyMap.isEmpty();
        final String propKeyPrefix = this.getKey() + ".props.";

        {
            enabledProperty.enableOverriding();
            enabledProperty.setOverrideDisplayString(this::getName);
            regProperty(new PropertyDescriptor(propKeyPrefix + "enabled", enabledProperty) {
                @Override
                public String getName() {
                    return FrostCore.getLocalizer().get("strings.enabled");
                }
            });
        }

        regProperty(PropertyDescriptor.buildDescriptorListForObject(
                this, propKeyPrefix, null
        ));
    }

    protected void onInitialized() {}

    @Override
    public String toString() {
        return this.getKey();
    }

    public List<PropertyDescriptor> getPropertyList() {
        return new ArrayList<>(propertyMap.values());
    }
    private void regProperty(List<PropertyDescriptor> descriptorList) {
        descriptorList.forEach(this::regProperty);
    }
    private void regProperty(PropertyDescriptor descriptor) {
        propertyMap.put(descriptor.getKey(), descriptor);
        recordProperty(descriptor);
    }

    private void recordProperty(PropertyDescriptor descriptor) {
        flattenedPropertyMap.put(descriptor.getKey(), descriptor);
        if (descriptor.isGroup()) {
            descriptor.getChildProperties().forEach(this::recordProperty);
        }
    }

    public PropertyDescriptor getDescriptor(String key) {
        return flattenedPropertyMap.get(key);
    }
}
