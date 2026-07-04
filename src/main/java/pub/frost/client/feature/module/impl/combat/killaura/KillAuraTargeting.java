package pub.frost.client.feature.module.impl.combat.killaura;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.preset.legacy.TargetSetting;
import pub.frost.utils.BoundingBoxUtils;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.targeting.EnumEntityTarget;
import pub.frost.utils.targeting.EnumEntityTargetPriority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SubModule(KillAura.class)
public class KillAuraTargeting extends AbstractSubModule<KillAura> {
    @PropertyGroupMain
    @Property("mode")
    public final ModeProperty<KillAura.Mode> mode = new ModeProperty<>(KillAura.Mode.SINGLE);

    @InsertProperty
    public final TargetSetting targets = new TargetSetting();
    @Property("priority")
    public final ModeProperty<EnumEntityTargetPriority> priority = new ModeProperty<>(EnumEntityTargetPriority.ANGLE);

    @Property("targetRange")
    public final FloatProperty targetRange = new FloatProperty(0, 8, 0.01f, 3.2f);
    @Property("fov")
    public final IntegerProperty fov = new IntegerProperty(1, 180, 1, 180);

    @Property("RaytraceBeforeTarget")
    public final BooleanProperty raytraceBeforeTarget = new BooleanProperty(true);

    private Entity lastTarget = null;
    public boolean isTarget(Entity entity, Vec3 eyePos) {
        final TargetSetting targetSetting = getParent().targeting.targets;
        if (entity == mc.thePlayer || entity.isDead) return false;
        if (!(entity instanceof EntityLivingBase)) return false;
        final EntityLivingBase living = (EntityLivingBase) entity;
        if (living.getHealth() <= 0) return false;

        if (EntityUtils.getDistanceToPoint(living, EntityUtils.getPositionEyes(mc.thePlayer, 1)) > 10) return false;
        if (targetSetting.teamCheck.get() && Teams.isTeammate(living)) return false;

        final int fovLimit = fov.get();
        for (EnumEntityTarget targetEnum : targetSetting.targets.getEnabled()) {
            if (!targetEnum.isTarget(entity.getClass())) continue;
            if (targetSetting.invisibleCheck.get() && entity.isInvisible()) continue;
            if (fovLimit != 180) {
                Rotation rotDelta = RotationUtils.getRotationDeltaAimingPoint(
                        eyePos,
                        FrostCore.getHelpers().getRotationManager().getCurrentPlayerRotation(),
                        BoundingBoxUtils.getCenter(entity.getEntityBoundingBox())
                );
                if (Math.abs(rotDelta.getYaw()) > fovLimit || Math.abs(rotDelta.getPitch()) > fovLimit / 2f) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public Entity selectBestTarget(List<Entity> validTargets, float tickDelta) {
        Entity targetRet = lastTarget;
        if (!validTargets.isEmpty()) {
            if (mode.is(KillAura.Mode.SINGLE)) {
                if (lastTarget == null || !validTargets.contains(lastTarget)) {
                    targetRet = validTargets.get(0);
                }
            } else targetRet = validTargets.get(0);

            if (targetRet != null && raytraceBeforeTarget.get()) {
                boolean hit = preTargetRaytrace(targetRet, tickDelta);

                if (!hit) {
                    Entity tempTarget = null;
                    for (Entity validTarget : validTargets) {
                        if (validTarget == targetRet) continue;
                        if (preTargetRaytrace(validTarget, tickDelta)) tempTarget = validTarget;
                        break;
                    }
                    targetRet = tempTarget;
                }
            }
        }
        lastTarget = targetRet;
        return targetRet;
    }
    public Comparator<Entity> getComparator() {
        EnumEntityTargetPriority value = getParent().targeting.priority.getValue();
        Comparator<Entity> comparator = Comparator.comparingInt(
                en -> mc.thePlayer.getDistanceToEntity(
                        en
                ) > getParent().attacking.getRealAttackRange()? 1 : -1
        );
        comparator = comparator.thenComparing(value.getComparator().apply(mc.thePlayer));
        for (EnumEntityTargetPriority p : EnumEntityTargetPriority.values()) {
            if (p == value) continue;
            comparator = comparator.thenComparing(p.getComparator().apply(mc.thePlayer));
        }
        return comparator;
    }

    private boolean preTargetRaytrace(Entity target, float tickDelta) {
        Rotation rotationAimingTarget = getParent().getRotation(target, tickDelta);
        return rotationAimingTarget != null;
    }
}
