package pub.frost.client.property.descriptor;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Described;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.annotations.PropertyGroupMain;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class PropertyDescriptor implements Named, Described {
    @JSONField(name = "key")
    private final String key;
    @JSONField(name = "data")
    private final AbstractProperty property;
    @JSONField(name = "childList")
    private final List<PropertyDescriptor> childProperties;
    private transient final Supplier<Boolean> visibilitySupplier;

    private transient final String translationKey;

    public PropertyDescriptor(String key, AbstractProperty property, String translationKey) {
        this(key, property, null, property::isVisible, translationKey);
    }

    public boolean isGroup() {
        return childProperties != null && !childProperties.isEmpty();
    }

    public boolean isVisible() {
        return visibilitySupplier.get();
    }

    @Override
    public String toString() {
        return this.getKey();
    }

    public String getTranslationKey() {
        return format(translationKey);
    }
}
