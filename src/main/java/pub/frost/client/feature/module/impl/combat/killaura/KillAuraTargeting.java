package pub.frost.client.feature.module.impl.combat.killaura;

import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.preset.TargetSetting;
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

    private Object lastTarget = null;

    public Object selectBestTarget(List<Object> validTargets) {
        Object targetRet = lastTarget;
        if (mode.is(KillAura.Mode.SINGLE)) {
            if (lastTarget == null || !validTargets.stream().collect(ArrayList::new,
                    ArrayList::add,
                    ArrayList::addAll
            ).contains(lastTarget)) {
                targetRet = validTargets.get(0);
            }
        } else targetRet = validTargets.get(0);

        if (targetRet != null && raytraceBeforeTarget.get()) {
            boolean hit = preTargetRaytrace(targetRet);

            if (!hit) {
                targetRet = null;
                for (Object validTarget : validTargets) {
                    if (preTargetRaytrace(validTarget)) targetRet = validTarget;
                    break;
                }
            }
        }

        lastTarget = targetRet;
        return targetRet;
    }

    private boolean preTargetRaytrace(Object target) {
        Rotation rotationAimingTarget = getParent().getRotation(target);
        return rotationAimingTarget != null;
    }
}
