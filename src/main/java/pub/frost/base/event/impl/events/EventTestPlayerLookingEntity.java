package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import pub.frost.base.event.api.interfaces.Event;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;

@Getter @Setter
public class EventTestPlayerLookingEntity implements Event {
    private final float tickDelta;
    private final Entity sourceEntity;
    private final Entity entity;
    private final Vec3 eyePos;
    private final Vec3 lookingVec;
    private final double reachDistance;

    private AxisAlignedBB hitbox;

    private boolean useDefualtResult = true;
    private MovingObjectPosition hitResult = null;

    public EventTestPlayerLookingEntity(
            float tickDelta, Entity sourceEntity, Entity entity, Vec3 eyePos,
            Vec3 lookingVec, double reachDistance,
            AxisAlignedBB hitbox
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
            float tickDelta, Entity entity, Vec3 eyePos,
            Vec3 lookingVec, double reachDistance,
            AxisAlignedBB hitbox
    ) {
        this(tickDelta, null, entity, eyePos, lookingVec, reachDistance, hitbox);
    }

    public EventTestPlayerLookingEntity(
            float tickDelta, Entity entity, Vec3 eyePos,
            AxisAlignedBB hitbox
    ) {
        this(tickDelta, null, entity, eyePos, null, Double.NaN, hitbox);
    }

    public void setResult(MovingObjectPosition hitResult) {
        useDefualtResult = false;
        this.hitResult = hitResult;
    }
}
