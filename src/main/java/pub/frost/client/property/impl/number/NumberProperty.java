package pub.frost.client.property.impl.number;

import lombok.Getter;
import pub.frost.client.property.AbstractProperty;
import pub.frost.utils.MathUtils;

import java.math.BigDecimal;

public abstract class NumberProperty<T extends Number & Comparable<T>> extends AbstractProperty<T> {
    @Getter
    private final BigDecimal minValue, maxValue, increaseStep;
    private BigDecimal value;

    public NumberProperty(T min, T max, T increaseStep, T current) {
        this.minValue = BigDecimal.valueOf(min.doubleValue());
        this.maxValue = BigDecimal.valueOf(max.doubleValue());
        this.increaseStep = BigDecimal.valueOf(increaseStep.doubleValue());

        this.value = getProcessedValue(current);
    }

    @Override
    public T getValue() {
        return castValue(value);
    }
    @Override
    protected boolean setValue(T oldValue, T newValue) {
        BigDecimal castedNewValue = getProcessedValue(newValue);
        boolean flag = castedNewValue.equals(value);
        this.value = castedNewValue;
        return flag;
    }

    public String getValueAsString(T value) {
        return BigDecimal.valueOf(value.doubleValue()).toPlainString();
    }
    public final BigDecimal getProcessedValue(T value) {
        return MathUtils.roundToStep(
                MathUtils.clamp(
                        BigDecimal.valueOf(value.doubleValue()),
                        minValue, maxValue
                ),
                increaseStep
        );
    }
    public abstract T castValue(BigDecimal value);
}
