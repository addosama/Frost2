package pub.frost.client.feature.helper.player.rotation.providers;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import pub.frost.utils.RotationUtils;
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
    private AxisAlignedBB lastTarget = null;

    public Rotation getRotation(
            Vec3 eyePos, AxisAlignedBB target,
            Vec3 preferred,
            Predicate<Rotation> rayCast,
            boolean tick
    ) {
        return getRotation(eyePos, target, preferred, rayCast, null, tick);
    }

    public Rotation getRotation(
            Vec3 eyePos, AxisAlignedBB target,
            Vec3 preferred,
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
            Vec3 eyePos,
            AxisAlignedBB target,
            Vec3 preferred,
            Predicate<Rotation> rayCast,
            AxisAlignedBB lastTarget, Rotation lastProvidedRotation,
            boolean tick
    );

    protected Rotation getBestRotationAimingAxisAlignedBB(
            Vec3 eyePos, AxisAlignedBB target,
            Vec3 preferredAimingPoint,
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
