package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3dc;
import pub.frost.base.event.api.interfaces.Event;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.raytrace.HitResult;

@Getter @Setter
public class EventTestPlayerLookingEntity implements Event {
    private final float tickDelta;
    private final Object sourceEntity;
    private final Object entity;
    private final Vector3dc eyePos;
    private final Vector3dc lookingVec;
    private final double reachDistance;

    private BoundingBox hitbox;

    private boolean useDefaultHitResult = true;
    private HitResult hitResult = null;

    public EventTestPlayerLookingEntity(
            float tickDelta, Object sourceEntity, Object entity, Vector3dc eyePos,
            Vector3dc lookingVec, double reachDistance,
            BoundingBox hitbox
    ) {
        this.tickDelta = tickDelta;
        this.sourceEntity = sourceEntity;
        this.entity = entity;
        this.eyePos = eyePos;
        this.lookingVec = lookingVec;
        this.reachDistance = reachDistance;

        this.hitbox = hitbox;
    }

    public EventTestPlayerLookingEntity(
            float tickDelta, Object entity, Vector3dc eyePos,
            Vector3dc lookingVec, double reachDistance,
            BoundingBox hitbox
    ) {
        this(tickDelta, null, entity, eyePos, lookingVec, reachDistance, hitbox);
    }

    public EventTestPlayerLookingEntity(
            float tickDelta, Object entity, Vector3dc eyePos,
            BoundingBox hitbox
    ) {
        this(tickDelta, null, entity, eyePos, null, Double.NaN, hitbox);
    }

    public void setHitResult(HitResult hitResult) {
        useDefaultHitResult = false;
        this.hitResult = hitResult;
    }
}
