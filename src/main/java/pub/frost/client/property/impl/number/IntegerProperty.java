package pub.frost.client.property.impl.number;

import java.math.BigDecimal;

public class IntegerProperty extends NumberProperty<Integer, IntegerProperty> {
    public IntegerProperty(int min, int max, int increaseStep, int current) {
        super(min, max, increaseStep, current);
    }

    @Override
    public Integer castValue(BigDecimal value) {
        return value.intValue();
    }
}
