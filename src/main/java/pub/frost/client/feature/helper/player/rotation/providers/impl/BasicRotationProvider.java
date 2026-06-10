package pub.frost.client.feature.helper.player.rotation.providers.impl;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import pub.frost.client.feature.helper.player.rotation.providers.AbstractRotationProvider;
import pub.frost.utils.data.Rotation;

import java.util.function.Predicate;

public class BasicRotationProvider extends AbstractRotationProvider {
    @Override
    protected Rotation getRotationInternal(Vec3 eyePos, AxisAlignedBB target, Vec3 preferred, Predicate<Rotation> rayCast, AxisAlignedBB lastTarget, Rotation lastProvidedRotation, boolean tick) {
        return getBestRotationAimingAxisAlignedBB(
                eyePos, target,
                preferred,
                rayCast
        );
    }
}
