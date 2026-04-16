package pub.frost.client.property.impl.bool;

import com.alibaba.fastjson2.annotation.JSONField;
import pub.frost.client.property.AbstractProperty;
import pub.frost.utils.EnumUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MultipleBooleanProperty<T extends Enum<T>> extends AbstractProperty<Map<T, Boolean>> {
    private final transient Class<T> typeClass;
    @JSONField(name = "value")
    private final Map<T, Boolean> values;

    @SafeVarargs
    public MultipleBooleanProperty(Class<T> typeClass, T... defaultEnabled) {
        this.typeClass = typeClass;
        this.values = new EnumMap<>(typeClass);
        List<T> defaultValues = Arrays.asList(defaultEnabled);
        Arrays.stream(typeClass.getEnumConstants()).forEach(
                v -> this.values.put(
                        v,
                        defaultValues.contains(v)
                )
        );
    }

    @Override
    public Map<T, Boolean> getValue() {
        return new EnumMap<>(values);
    }
    @Override @Deprecated
    protected boolean setValue(Map<T, Boolean> oldValue, Map<T, Boolean> newValue) {
        values.putAll(newValue);
        for (Map.Entry<T, Boolean> entry : oldValue.entrySet()) {
            if (newValue.getOrDefault(entry.getKey(), entry.getValue()) != entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    public Set<Map.Entry<T, Boolean>> getEntrySet() {
        return values.entrySet();
    }

    public Set<T> getEnabled() {
        return values.keySet().stream().filter(values::get).collect(Collectors.toSet());
    }
    public boolean isEnabled(T value) {
        return values.get(value).equals(Boolean.TRUE);
    }
    public void setEnabled(T value, boolean enabled) {
        values.put(value, enabled);
    }

    @Override
    public Map<T, Boolean> deserializeValue(Object obj) {
        if (!(obj instanceof Map)) return null;

        Map<?, ?> input = (Map<?, ?>) obj;
        EnumMap<T, Boolean> map = new EnumMap<>(typeClass);

        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = (String) entry.getKey();
            Boolean val = (Boolean) entry.getValue();

            T type = EnumUtils.getEnumByString(typeClass, key);
            if (type != null) {
                map.put(type, val);
            }
        }

        return map;
    }
}
