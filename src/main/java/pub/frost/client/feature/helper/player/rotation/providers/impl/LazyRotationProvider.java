package pub.frost.client.feature.helper.player.rotation.providers.impl;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Vector3d;
import pub.frost.client.feature.helper.player.rotation.providers.AbstractRotationProvider;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.Rotation;

import java.util.function.Predicate;
import java.util.function.Supplier;

// todo
@Accessors(fluent = true)
public class LazyRotationProvider extends AbstractRotationProvider {
    @Setter
    private Supplier<Integer> maxWaitTicks = () -> 5;

    private int ticksRayCastFailed = 0;

    @Override
    protected Rotation getRotationInternal(Vector3d eyePos, BoundingBox target, Vector3d preferred, Predicate<Rotation> rayCast, BoundingBox lastTarget, Rotation lastProvidedRotation, boolean tick) {
        if (rayCast.test(lastProvidedRotation)) {
            if (tick) ticksRayCastFailed = 0;
            return lastProvidedRotation;
        } else {
            if (tick) ticksRayCastFailed++;
        }

        if (ticksRayCastFailed > maxWaitTicks.get()) {

        }

        return null;
    }
}
