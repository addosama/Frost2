package pub.frost.client.property.impl.color;

import lombok.Getter;
import lombok.Setter;
import pub.frost.client.property.AbstractProperty;
import pub.frost.utils.ColorUtils;
import pub.frost.utils.MathUtils;

import java.util.Arrays;

@Getter
public class ColorProperty extends AbstractProperty<float[], ColorProperty> {
    private float h, s, b, a;
    private transient final boolean alphaEnabled;

    public ColorProperty(int colorABGR, boolean alphaEnabled) {
        this.alphaEnabled = alphaEnabled;
        float[] hsb = ColorUtils.BGRtoHSB(colorABGR);
        this.h = hsb[0];
        this.s = hsb[1];
        this.b = hsb[2];
        if (alphaEnabled) {
            this.a = ColorUtils.getAlpha(colorABGR) / 255f;
        } else a = -1;
    }
    public ColorProperty(int colorABGR) {
        this(colorABGR, true);
    }

    @Override
    public float[] getValue() {
        return new float[] {h, s, b, a};
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
        int color = ColorUtils.HSBtoBGR(h, s, b);
        if (alphaEnabled) {
            color = ColorUtils.reAlpha(color, (int) (a * 255));
        }
        return color;
    }

    public void setH(float h) {
        this.h = Math.min(1, Math.max(0, h));
    }
    public void setS(float s) {
        this.s = Math.min(1, Math.max(0, s));
    }
    public void setB(float b) {
        this.b = Math.min(1, Math.max(0, b));
    }
    public void setA(float a) {
        if (!alphaEnabled) return;
        this.a = Math.min(1, Math.max(0, a));
    }
}
