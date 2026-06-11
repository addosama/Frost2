package pub.frost.client.feature.module.impl.combat.killaura;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.property.annotations.Property;
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
public class KillAuraSearching extends AbstractSubModule<KillAura> {
    @Property("targetRange")
    public final FloatProperty targetRange = new FloatProperty(0, 8, 0.01f, 3.2f);
    @Property("fov")
    public final IntegerProperty fov = new IntegerProperty(1, 180, 1, 180);

    private List<Entity> provideValidTargetList(double targetRange) {
        final TargetSetting targetSetting = getParent().targeting.targets;
        List<Entity> list = new ArrayList<>();
        int fovValue = fov.get();
        Vec3 eyePos = EntityUtils.getPositionEyes(mc.thePlayer, 1);
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity.isDead) continue;
            if (!(entity instanceof EntityLivingBase)) continue;
            if (entity == mc.thePlayer) continue;

            EntityLivingBase living = (EntityLivingBase) entity;
            if (living.getHealth() <= 0) continue;
            if (EntityUtils.getDistanceToPoint(living, EntityUtils.getPositionEyes(mc.thePlayer, 1)) > targetRange) continue;

            if (targetSetting.teamCheck.get() && Teams.isTeammate(entity)) continue;

            for (EnumEntityTarget targetEnum : targetSetting.targets.getEnabled()) {
                if (!targetEnum.isTarget(entity.getClass())) continue;
                if (targetSetting.invisibleCheck.get() && entity.isInvisible()) continue;
                if (fovValue != 180) {
                    Rotation rotDelta = RotationUtils.getRotationDeltaAimingPoint(
                            eyePos,
                            FrostCore.getHelpers().getRotationManager().getCurrentPlayerRotation(),
                            BoundingBoxUtils.getCenter(entity.getEntityBoundingBox())
                    );
                    if (Math.abs(rotDelta.getYaw()) > fovValue || Math.abs(rotDelta.getPitch()) > fovValue / 2f) {
                        break;
                    }
                }

                list.add(entity);
                break;
            }
        }
        return list;
    }
    private void sortEntityListByPriority(List<Entity> list) {
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
        list.sort(comparator);
    }

    public List<Entity> searchTargets(double targetRange) {
        List<Entity> list = provideValidTargetList(targetRange);
        sortEntityListByPriority(list);
        return list;
    }
    public List<Entity> searchTargets() {
        return searchTargets(targetRange.get());
    }
}
