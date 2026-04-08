package pub.frost.client.property.impl.number;

import java.text.DecimalFormat;

public class PercentProperty extends FloatProperty {
    private static final DecimalFormat format = new DecimalFormat("0");

    public PercentProperty(float min, float max, float current) {
        super(min, max, 0.01f, current);
    }

    @Override
    public String getValueAsString(Float value) {
        return format.format(value * 100) + "%";
    }
}
