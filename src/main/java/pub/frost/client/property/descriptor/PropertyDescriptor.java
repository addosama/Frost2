package pub.frost.client.property.descriptor;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.interfaces.Described;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;

import java.util.List;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class PropertyDescriptor implements Named, Described {
    @JSONField(name = "key")
    private final String key;
    @JSONField(name = "data")
    private final AbstractProperty<?, ?> property;
    @JSONField(name = "childList")
    private final List<PropertyDescriptor> childProperties;
    private transient final VisibilitySupplier visibilitySupplier;

    private transient final String translationKey;

    public PropertyDescriptor(String key, AbstractProperty<?, ?> property, String translationKey) {
        this(key, property, null, property::isVisible, translationKey);
    }

    public boolean isGroup() {
        return childProperties != null && !childProperties.isEmpty();
    }

    public boolean isVisible() {
        return visibilitySupplier.isVisible();
    }

    @Override
    public String toString() {
        return this.getKey();
    }

    public String getTranslationKey() {
        return format(translationKey);
    }
}
