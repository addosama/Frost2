package pub.frost.client.feature.module.impl.combat.killaura;

import org.joml.Vector3d;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.preset.legacy.TargetSetting;
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

    private List<Object> provideValidTargetList() {
        final TargetSetting targetSetting = getParent().targeting.targets;
        List<Object> list = new ArrayList<>();
        int fovValue = fov.get();
        Vector3d eyePos = Entity.getPositionEyes(Minecraft.getPlayer(mc), 1);
        for (Object entity : World.getLoadedEntityList(Minecraft.getWorld(mc))) {
            if (Entity.isDead(entity)) continue;
            if (!EntityLivingBase.isTarget(entity.getClass())) continue;
            if (entity == Minecraft.getPlayer(mc)) continue;

            if (EntityLivingBase.getHealth(entity) <= 0) continue;
            if (EntityLivingBase.distanceTo(entity, Entity.getPositionVector(Minecraft.getPlayer(mc))) > targetRange.getValue()) continue;

            if (targetSetting.teamCheck.get() && Teams.isTeammate(entity)) continue;

            for (EnumEntityTarget targetEnum : targetSetting.targets.getEnabled()) {
                if (!targetEnum.isTarget(entity.getClass())) continue;
                if (targetSetting.invisibleCheck.get() && Entity.isInvisible(entity)) continue;
                if (fovValue != 180) {
                    Rotation rotDelta = RotationUtils.getRotationDeltaAimingPoint(
                            eyePos,
                            FrostCore.getHelpers().getRotationManager().getCurrentPlayerRotation(),
                            Entity.getBoundingBox(entity).getCenter()
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
    private void sortEntityListByPriority(List<Object> list) {
        EnumEntityTargetPriority value = getParent().targeting.priority.getValue();
        Comparator<Object> comparator = Comparator.comparingInt(
                en -> Entity.distanceTo(
                        Minecraft.getPlayer(mc), Entity.getPositionVector(en)
                ) > getParent().attacking.getRealAttackRange()? 1 : -1
        );
        comparator = comparator.thenComparing(value.getComparator().apply(Minecraft.getPlayer(mc)));
        for (EnumEntityTargetPriority p : EnumEntityTargetPriority.values()) {
            if (p == value) continue;
            comparator = comparator.thenComparing(p.getComparator().apply(Minecraft.getPlayer(mc)));
        }
        list.sort(comparator);
    }

    public List<Object> searchTargets() {
        List<Object> list = provideValidTargetList();
        sortEntityListByPriority(list);
        return list;
    }
}
