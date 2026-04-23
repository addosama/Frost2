package pub.frost.client.property.impl.number;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PercentProperty extends NumberProperty<Float, PercentProperty> {
    private static final DecimalFormat format = new DecimalFormat("0");

    public PercentProperty(float min, float max, float current) {
        super(min, max, 0.01f, current);
    }

    @Override
    public Float castValue(BigDecimal value) {
        return value.floatValue();
    }
    @Override
    public String getValueAsString(Float value) {
        return format.format(value * 100) + "%";
    }
}
