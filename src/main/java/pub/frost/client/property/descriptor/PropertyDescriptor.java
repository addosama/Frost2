package pub.frost.client.property.descriptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.client.core.FrostCore;
import pub.frost.client.property.AbstractProperty;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyDescriptor {
    private final String key;
    private final AbstractProperty<?> property;
    private final List<PropertyDescriptor> childProperties;

    public PropertyDescriptor(String key, AbstractProperty<?> property) {
        this(key, property, null);
    }
    public PropertyDescriptor(String key, List<PropertyDescriptor> childProperties) {
        this(key, null, childProperties);
    }

    public boolean isGroup() {
        return childProperties != null;
    }

    public String getName() {
        return FrostCore.getLocalizer().get(this.getKey() + ".name");
    }
    public String getDescription() {
        return FrostCore.getLocalizer().get(this.getKey() + ".description");
    }
}
