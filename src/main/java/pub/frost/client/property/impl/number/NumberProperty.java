package pub.frost.client.property.impl.number;

import pub.frost.client.property.AbstractProperty;
import pub.frost.utils.MathUtils;

import java.math.BigDecimal;

public abstract class NumberProperty<T extends Number & Comparable<T>> extends AbstractProperty<T> {
    private final BigDecimal minValue, maxValue, increaseStep;
    private BigDecimal value;

    public NumberProperty(T min, T max, T increaseStep, T current) {
        this.minValue = BigDecimal.valueOf(min.doubleValue());
        this.maxValue = BigDecimal.valueOf(max.doubleValue());
        this.increaseStep = BigDecimal.valueOf(increaseStep.doubleValue());

        setValue(null, current);
    }

    @Override
    protected T getValue() {
        return castValue(value);
    }
    @Override
    protected boolean setValue(T oldValue, T newValue) {
        BigDecimal castedNewValue = MathUtils.roundToStep(
                MathUtils.clamp(
                        BigDecimal.valueOf(newValue.doubleValue()),
                        minValue, maxValue
                ),
                increaseStep
        );
        boolean flag = castedNewValue.equals(value);
        this.value = castedNewValue;
        return flag;
    }

    protected abstract T castValue(BigDecimal value);
}
