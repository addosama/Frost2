package pub.frost.client.feature.module.impl.combat.killaura;

import net.minecraft.entity.Entity;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.preset.legacy.TargetSetting;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.targeting.EnumEntityTargetPriority;

import java.util.ArrayList;
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

    @Property("RaytraceBeforeTarget")
    public final BooleanProperty raytraceBeforeTarget = new BooleanProperty(true);

    private Entity lastTarget = null;

    public Entity selectBestTarget(List<Entity> validTargets, float tickDelta) {
        Entity targetRet = lastTarget;
        if (mode.is(KillAura.Mode.SINGLE)) {
            if (lastTarget == null || !validTargets.stream().collect(ArrayList::new,
                    ArrayList::add,
                    ArrayList::addAll
            ).contains(lastTarget)) {
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

        lastTarget = targetRet;
        return targetRet;
    }

    private boolean preTargetRaytrace(Entity target, float tickDelta) {
        Rotation rotationAimingTarget = getParent().getRotation(target, tickDelta);
        return rotationAimingTarget != null;
    }
}
