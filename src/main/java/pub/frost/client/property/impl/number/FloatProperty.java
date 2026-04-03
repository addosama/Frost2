package pub.frost.client.property.impl.number;

import java.math.BigDecimal;

public class FloatProperty extends NumberProperty<Float> {
    public FloatProperty(float min, float max, float increaseStep, float current) {
        super(min, max, increaseStep, current);
    }

    @Override
    protected Float castValue(BigDecimal value) {
        return value.floatValue();
    }
}
