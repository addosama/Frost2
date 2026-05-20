package pub.frost.client.property.impl.color;

import com.alibaba.fastjson2.JSONArray;
import lombok.Getter;
import lombok.Setter;
import pub.frost.client.property.AbstractProperty;
import pub.frost.utils.ColorUtils;
import pub.frost.utils.MathUtils;

import java.util.Arrays;

@Getter
public class ColorProperty extends AbstractProperty<float[], ColorProperty> {
    private final float[] value;
    private transient final boolean alphaEnabled;

    public ColorProperty(int colorABGR, boolean alphaEnabled) {
        this.alphaEnabled = alphaEnabled;
        this.value = new float[4];
        System.arraycopy(ColorUtils.BGRtoHSB(colorABGR), 0, value, 0, 3);
        if (alphaEnabled) {
            value[3] = ColorUtils.getAlpha(colorABGR) / 255f;
        } else value[3] = -1;
    }
    public ColorProperty(int colorABGR) {
        this(colorABGR, true);
    }

    @Override
    public float[] getValue() {
        return Arrays.copyOf(value, value.length);
    }
    @Override
    protected boolean setValue(float[] oldValue, float[] newValue) {
        setH(newValue[0]);
        setS(newValue[1]);
        setB(newValue[2]);
        setA(newValue[3]);
        return !Arrays.equals(oldValue, newValue);
    }

    public int getValueABGR() {
        int color = ColorUtils.HSBtoBGR(getH(), getS(), getB());
        if (alphaEnabled) {
            color = ColorUtils.reAlpha(color, (int) (getA() * 255));
        }
        return color;
    }

    @Override
    public float[] deserializeValue(Object obj) {
        JSONArray jsonArray = (JSONArray) obj;
        return new float[] {
                jsonArray.getFloatValue(0),
                jsonArray.getFloatValue(1),
                jsonArray.getFloatValue(2),
                jsonArray.getFloatValue(3)
        };
    }

    public float getH() {
        return value[0];
    }
    public float getS() {
        return value[1];
    }
    public float getB() {
        return value[2];
    }
    public float getA() {
        return value[3];
    }

    public void setH(float h) {
        value[0] = Math.min(1, Math.max(0, h));
    }
    public void setS(float s) {
        value[1] = Math.min(1, Math.max(0, s));
    }
    public void setB(float b) {
        value[2] = Math.min(1, Math.max(0, b));
    }
    public void setA(float a) {
        if (!alphaEnabled) return;
        value[3] = Math.min(1, Math.max(0, a));
    }
}
