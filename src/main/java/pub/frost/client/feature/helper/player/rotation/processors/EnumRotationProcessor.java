package pub.frost.client.feature.helper.player.rotation.processors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.utils.RotationUtils;

import java.util.function.BiConsumer;

@TranslationKey("strings.enum.rotation.processors.~")
@RequiredArgsConstructor
public enum EnumRotationProcessor implements Named, RotationProcessor, Wrappers {
    GCD_FIX(
            "GCDFix", 0x01,
            (currentYaw, currentPitch, nextYaw, nextPitch, rotationAcceptor) -> {
                final float mouseSensitivity = (float) (GameSettings.getMouseSensitivity(Minecraft.getGameSettings(Minecraft.getInstance())) * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
                final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
                final float yaw = currentYaw + (float) (Math.round((nextYaw - currentYaw) / multiplier) * multiplier);
                final float pitch = currentPitch + (float) (Math.round((nextPitch - currentPitch) / multiplier) * multiplier);

                rotationAcceptor.accept(yaw, pitch);
            }
    ),

    PITCH_DELTA_FIX(
            "PitchDeltaFix", 0x10,
            (currentYaw, currentPitch, nextYaw, nextPitch, rotationAcceptor) -> {
                final float deltaYaw = RotationUtils.wrapYawTo180(Math.abs(nextYaw - currentYaw));
                final float deltaPitch = Math.abs(nextPitch - currentPitch);

                if (deltaPitch < deltaYaw / 100) {
                    nextPitch += (float) (Math.random());
                }

                rotationAcceptor.accept(null, nextPitch);
            }
    );


    final String key;
    @Getter
    final int flag;
    final RotationProcessor impl;

    public boolean isEnabled(int processors) {
        return (processors & flag) != 0;
    }

    @Override
    public void process(float currentYaw, float currentPitch, float nextYaw, float nextPitch, BiConsumer<Float, Float> rotationAcceptor) {
        impl.process(currentYaw, currentPitch, nextYaw, nextPitch, rotationAcceptor);
    }

    @Override
    public String toString() {
        return key.toLowerCase();
    }
}
