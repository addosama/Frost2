package pub.frost.client.property.preset.impl;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.ManualDescriptorProvider;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.descriptor.VisibilitySupplier;
import pub.frost.client.property.impl.color.ColorProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorSetting implements VisibilitySupplier, ManualDescriptorProvider {
    private final Supplier<Boolean> visibility;

    public final ModeProperty<EnumColorType> type = new ModeProperty<>(EnumColorType.STATIC);

    public final IntegerProperty speed = new IntegerProperty(1, 10, 1, 4)
            .setVisibilitySupplier(() -> !type.is(EnumColorType.STATIC));
    public final IntegerProperty range = new IntegerProperty(1, 50, 1, 25)
            .setVisibilitySupplier(() -> !type.is(EnumColorType.STATIC));

    public final FloatProperty rainbowSaturation = new FloatProperty(0, 1, 0.01f, 0.8f)
            .setVisibilitySupplier(() -> type.is(EnumColorType.RAINBOW));
    public final FloatProperty rainbowBrightness = new FloatProperty(0, 1, 0.01f, 1)
            .setVisibilitySupplier(() -> type.is(EnumColorType.RAINBOW));

    public final IntegerProperty gradientColorCount = new IntegerProperty(2, 6, 1, 2)
            .setVisibilitySupplier(() -> type.is(EnumColorType.GRADIENT));

    private final ColorProperty[] colors = new ColorProperty[7];

    @RequiredArgsConstructor
    @TranslationKey("strings.enum.color.type.~")
    public enum EnumColorType implements Named {
        STATIC("Static"),
        RAINBOW("Rainbow"),
        GRADIENT("Gradient");
        final String key;
        @Override public String toString() {
            return key;
        }
    }

    public ColorSetting(int defaultColor, boolean allowAlpha) {
        this(defaultColor, allowAlpha, () -> true);
    }
    public ColorSetting(boolean allowAlpha, Supplier<Boolean> visibility) {
        this(0xFFFFFFFF, allowAlpha, visibility);
    }
    public ColorSetting(int defaultColor, boolean allowAlpha, Supplier<Boolean> visibility) {
        this.visibility = visibility;

        colors[0] = new ColorProperty(defaultColor, allowAlpha).setVisibilitySupplier(() -> type.is(EnumColorType.STATIC));
        for (int i = 1; i <= 6; i++) {
            final int colorIndex = i;
            colors[i] = new ColorProperty(0xFFFFFFFF, allowAlpha).setVisibilitySupplier(
                    () -> type.is(EnumColorType.GRADIENT) && gradientColorCount.get() >= colorIndex
            );
        }
    }

    @Override
    public List<PropertyDescriptor> provideDescriptors(
            String keyPrefix,
            Consumer<AbstractProperty<?, ?>> mainPropConsumer
    ) {
        mainPropConsumer.accept(type);
        ArrayList<PropertyDescriptor> descriptors = new ArrayList<>(Arrays.asList(
                // type
                getPropDescriptor(keyPrefix, "Type", type),

                // dynamic
                getPropDescriptor(keyPrefix, "Speed", speed),
                getPropDescriptor(keyPrefix, "Range", range),

                // rainbow
                getPropDescriptor(keyPrefix, "RainbowSaturation", rainbowSaturation),
                getPropDescriptor(keyPrefix, "RainbowBrightness", rainbowBrightness),

                // gradient
                getPropDescriptor(keyPrefix, "GradientColorCount", gradientColorCount)
        ));

        // add colors
        String propKey = "StaticColor";
        for (int i = 0; i < 7; i++) {
            ColorProperty colorProp = colors[i];
            descriptors.add(getPropDescriptor(keyPrefix, propKey, colorProp));
            propKey = "GradientColor"  + (i + 1);
        }

        return Collections.unmodifiableList(descriptors);
    }

    public int getColorABGR() {
        return getColorABGR(0);
    }
    public int getColorABGR(int index) {
        if (!type.is(EnumColorType.STATIC)) {
            final int range = this.range.get();
            final int speed = this.speed.getMaxValue().intValue() + this.speed.getMinValue().intValue() - this.speed.get();
            int[] colors = null;
            if (type.is(EnumColorType.GRADIENT)) {
                final int count = gradientColorCount.get();
                colors = new int[count];
                for (int i = 0; i < count; i++) {
                    colors[i] = this.colors[i + 1].getValueABGR();
                }
            }
            if (type.is(EnumColorType.RAINBOW)) {
                colors = new int[6];
                final float hueStep = 1 / 7f;
                final float s = rainbowSaturation.get(), b = rainbowBrightness.get();
                for (int i = 0; i < 6; i++) {
                    colors[i] = ColorUtils.HSBtoBGR(hueStep * i, s, b);
                }
            }
            if (colors != null) return ColorUtils.getMultiGradient(index, speed, range, colors);
        }
        return colors[0].getValueABGR();
    }

    @Override
    public boolean isVisible() {
        return visibility.get();
    }

    private PropertyDescriptor getPropDescriptor(String keyPrefix, String key, AbstractProperty<?, ?> prop) {
        return new PropertyDescriptor(keyPrefix + key, prop, "strings.settings.color.props." + key);
    }
}
