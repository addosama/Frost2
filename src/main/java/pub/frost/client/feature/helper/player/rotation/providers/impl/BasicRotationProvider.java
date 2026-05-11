package pub.frost.client.feature.helper.player.rotation.providers.impl;

import org.joml.Vector3d;
import pub.frost.client.feature.helper.player.rotation.providers.AbstractRotationProvider;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.Rotation;

import java.util.function.Predicate;

public class BasicRotationProvider extends AbstractRotationProvider {
    @Override
    protected Rotation getRotationInternal(Vector3d eyePos, BoundingBox target, Vector3d preferred, Predicate<Rotation> rayCast, BoundingBox lastTarget, Rotation lastProvidedRotation, boolean tick) {
        return getBestRotationAimingBoundingBox(
                eyePos, target,
                preferred,
                rayCast
        );
    }
}
