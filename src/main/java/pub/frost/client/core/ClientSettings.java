package pub.frost.client.core;

import pub.frost.client.i18n.I18n;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.descriptor.PropertyDescriptorFactory;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;

import java.util.Map;

public class ClientSettings {
    @InsertProperty("client.Localizing")
    public Localizing localizing = new Localizing();

    public static class Localizing {
        @Property("client.localizing.Language")
        public ModeProperty<I18n.Language> language = new ModeProperty<>(I18n.Language.ENGLISH)
                .setValueChangeListener((o, n) -> {
                    FrostCore.getInstance().getI18nHelper().setLanguage(n);
                });
        @Property("client.localizing.SmartLocalize")
        public BooleanProperty smartLocalize = new BooleanProperty(true);
        @Property("client.localizing.MarkUnlocalizedString")
        public BooleanProperty markUnlocalizedString = new BooleanProperty(false)
                .setVisibilitySupplier(smartLocalize::get);
    }

    private Map<String, PropertyDescriptor> descMap;
    private Map<String, PropertyDescriptor> flattenedDescMap;
    private boolean mapCheck() {
        return descMap == null || flattenedDescMap == null;
    }
    private void initializeMaps() {
        PropertyDescriptorFactory factory = PropertyDescriptorFactory.createForObject(
                FrostCore.getInstance().getClientSettings()
        ).build();
        descMap = factory.getDescriptorMap();
        flattenedDescMap = factory.getFlattenedDescriptorMap();
    }

    public Map<String, PropertyDescriptor> getDescMap() {
        if (mapCheck()) initializeMaps();
        return descMap;
    }
    public PropertyDescriptor getDescriptor(String key) {
        if (mapCheck()) initializeMaps();
        return flattenedDescMap.get(key);
    }
}
