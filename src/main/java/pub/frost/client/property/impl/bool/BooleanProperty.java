package pub.frost.client.property.impl.bool;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import pub.frost.client.property.AbstractProperty;

@AllArgsConstructor
public class BooleanProperty extends AbstractProperty<Boolean> {
    @JSONField(name = "value")
    private boolean value;

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    protected boolean setValue(Boolean oldValue, Boolean value) {
        this.value = value;
        return oldValue != this.value;
    }
}
