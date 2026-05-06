package pub.frost.client.property.impl.color;

import lombok.Getter;
import pub.frost.client.property.AbstractProperty;
import pub.frost.utils.ColorUtils;

public class ColorProperty extends AbstractProperty<Integer, ColorProperty> {
    private int value;
    @Getter
    private transient final boolean alphaEnabled;

    public ColorProperty(int colorABGR, boolean alphaEnabled) {
        this.alphaEnabled = alphaEnabled;
        this.value = processValue(colorABGR);
    }
    public ColorProperty(int colorABGR) {
        this(colorABGR, true);
    }

    public Integer getValue() {
        return value;
    }

    protected boolean setValue(Integer oldValue, Integer newValue) {
        this.value = processValue(newValue);
        return value != oldValue;
    }

    private int processValue(int value) {
        if (!alphaEnabled) {
            return ColorUtils.reAlpha(value, 255);
        }
        return value;
    }
}
