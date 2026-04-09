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
        this.minValue = new BigDecimal(min.toString());
        this.maxValue = new BigDecimal(max.toString());
        this.increaseStep = new BigDecimal(increaseStep.toString());

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
        return getProcessedValue(value).toPlainString();
    }
    public final BigDecimal getProcessedValue(T value) {
        return MathUtils.roundToStep(
                MathUtils.clamp(
                        new BigDecimal(value.toString()),
                        minValue, maxValue
                ),
                increaseStep
        );
    }
    public abstract T castValue(BigDecimal value);
}
