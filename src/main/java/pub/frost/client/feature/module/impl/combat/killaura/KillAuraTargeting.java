package pub.frost.client.feature.module.impl.combat.killaura;

import pub.frost.client.feature.module.api.SubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.preset.TargetSetting;
import pub.frost.utils.targeting.EnumEntityTargetPriority;

import java.util.ArrayList;
import java.util.List;

public class KillAuraTargeting extends SubModule<KillAura> {
    public KillAuraTargeting(KillAura killAura) {
        super(killAura);
    }

    @PropertyGroupMain
    @Property("mode")
    public final ModeProperty<KillAura.Mode> mode = new ModeProperty<>(KillAura.Mode.SINGLE);

    @InsertProperty
    public final TargetSetting targets = new TargetSetting();
    @Property("priority")
    public final ModeProperty<EnumEntityTargetPriority> priority = new ModeProperty<>(EnumEntityTargetPriority.ANGLE);

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

        lastTarget = targetRet;
        return targetRet;
    }
}
