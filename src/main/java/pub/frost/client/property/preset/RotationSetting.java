package pub.frost.client.property.preset;

import lombok.RequiredArgsConstructor;
import pub.frost.client.feature.helper.player.rotation.processors.post.EnumRotationPostProcessor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.EnumRotationMode;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class RotationSetting {
    @TranslationKey("strings.rotation.rotation")
    @PropertyGroupHead("Rotation")
    private final Supplier<Boolean> supplier;

    @TranslationKey("strings.rotation.instant")
    @Property("Instant")
    public final BooleanProperty instant = new BooleanProperty(false);
    @TranslationKey("strings.mode")
    @Property("mode")
    public final ModeProperty<EnumRotationMode> mode = new ModeProperty<>(EnumRotationMode.BASIC);

    @TranslationKey("strings.speed")
    @Property("Speed")
    public final IntegerProperty speed = new IntegerProperty(1, 180, 1, 120).setVisibilitySupplier(() -> !instant.get());

    @TranslationKey("strings.rotation.lockview")
    @Property("LockView")
    public final BooleanProperty lockView = new BooleanProperty(false);
    @TranslationKey("strings.rotation.extraprocessors")
    @Property(value = "ExtraProcessors", endGroup = true)
    public final MultipleBooleanProperty<EnumRotationPostProcessor> extraProcessors = new MultipleBooleanProperty<>(EnumRotationPostProcessor.class);

    public int getSpeed() {
        return instant.get()? 0 : speed.get();
    }
    public boolean isLockViewEnabled() {
        return lockView.get();
    }
    public int getEnabledProcessors() {
        int processorFlag = 0;
        for (EnumRotationPostProcessor processor : extraProcessors.getEnabled()) {
            processorFlag |= processor.getFlag();
        }
        return processorFlag;
    }

    public RotationSetting() {
        this(() -> true);
    }
}
