package pub.frost.client.feature.helper.player.rotation.providers;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Vector3d;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.Rotation;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Accessors(fluent = true)
public abstract class AbstractRotationProvider {
    @Setter
    private Supplier<Boolean> search = () -> true;
    @Setter
    private Supplier<Integer> maxSearchSteps = () -> 2;

    private Rotation lastProvidedRotation = null;
    private BoundingBox lastTarget = null;

    public Rotation getRotation(
            Vector3d eyePos, BoundingBox target,
            Vector3d preferred,
            Predicate<Rotation> rayCast,
            boolean tick
    ) {
        return getRotation(eyePos, target, preferred, rayCast, null, tick);
    }

    public Rotation getRotation(
            Vector3d eyePos, BoundingBox target,
            Vector3d preferred,
            Predicate<Rotation> rayCast,
            Rotation fallbackRotation,
            boolean tick
    ) {
        Rotation rotation = Optional.ofNullable(getRotationInternal(
                eyePos,
                target, preferred,
                rayCast,
                lastTarget, lastProvidedRotation,
                tick
        )).orElse(fallbackRotation);
        lastProvidedRotation = rotation;
        return rotation;
    }

    protected abstract Rotation getRotationInternal(
            Vector3d eyePos,
            BoundingBox target,
            Vector3d preferred,
            Predicate<Rotation> rayCast,
            BoundingBox lastTarget, Rotation lastProvidedRotation,
            boolean tick
    );

    protected Rotation getBestRotationAimingBoundingBox(
            Vector3d eyePos, BoundingBox target,
            Vector3d preferredAimingPoint,
            Predicate<Rotation> rayCast
    ) {
        {
            Rotation rotationAimingPreferredPoint = preferredAimingPoint != null ?
                    RotationUtils.getRotationAimingPoint(eyePos, preferredAimingPoint)
                    : null;
            if (rotationAimingPreferredPoint != null) {
                if (rayCast.test(rotationAimingPreferredPoint)) return rotationAimingPreferredPoint;
            }
        }

        if (search.get()) {
            return RotationUtils.searchRotationHittingBoundingBox(
                    eyePos, target,
                    rayCast,
                    maxSearchSteps.get()
            );
        }
        return null;
    }

    public void reset() {
        lastProvidedRotation = null;
        lastTarget = null;
    }
}
