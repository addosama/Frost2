package pub.frost.client.property.preset.legacy;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class TickDeltaFixSetting {
    @TranslationKey("strings.settings.tickdeltafix.name")
    @PropertyGroupHead("TickDeltaFix")
    public final Supplier<Boolean> visibility;
    public TickDeltaFixSetting() {
        this(() -> true);
    }

    @PropertyGroupMain
    @TranslationKey("strings.mode")
    @Property("Mode")
    public ModeProperty<EnumTickDeltaFix> mode = new ModeProperty<>(EnumTickDeltaFix.FULL);

    @TranslationKey("strings.settings.tickdeltafix.customvalue")
    @Property(value = "CustomValue", endGroup = true)
    public FloatProperty customValue = new FloatProperty(0, 1, 0.01f, 1f).setVisibilitySupplier(() -> mode.is(EnumTickDeltaFix.CUSTOM));

    public float get() {
        return get(0);
    }
    public float get(float fallback) {
        switch (mode.get()) {
            case FULL: return 1;
            case ZERO: return 0;
            case CUSTOM: return customValue.get();
            case RANDOM: return (float) Math.random();
        }
        return fallback;
    }

    @TranslationKey("strings.enum.tickdeltafix.~")
    @RequiredArgsConstructor
    public enum EnumTickDeltaFix {
        FULL("Full"),
        ZERO("Zero"),
        CUSTOM("Custom"),
        RANDOM("Random");

        final String key;
        @Override public String toString() {
            return key;
        }
    }
}
