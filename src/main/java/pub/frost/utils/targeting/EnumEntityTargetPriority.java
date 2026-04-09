package pub.frost.utils.targeting;

import lombok.Getter;
import org.joml.Vector3d;
import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.utils.RotationUtils;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;

import java.util.Comparator;
import java.util.function.Function;

@Getter
public enum EnumEntityTargetPriority implements Named {
    ANGLE("angle", player -> {
        Vector3d eyePos = player.getPositionEyes(1);
        float playerYaw = RotationUtils.wrapYawTo180(player.getYaw());
        return Comparator.comparingDouble(target -> Math.abs(
                RotationUtils.wrapYawTo180(RotationUtils.getRotationAimingPoint(eyePos, target.getPositionVector()).getYaw())
                - playerYaw
        ));
    }),
    DISTANCE("distance", player -> Comparator.comparingDouble(entity -> player.distanceTo(entity.getPositionVector()))),
    HEALTH("health", p -> Comparator.comparingDouble(WEntityLivingBase::getHealth)),
    HURTTIME("hurttime", p -> Comparator.comparingInt(WEntityLivingBase::getHurtTime)),;

    final String key;
    final Function<WEntityLivingBase, Comparator<WEntityLivingBase>> comparator;

    EnumEntityTargetPriority(String key, Function<WEntityLivingBase, Comparator<WEntityLivingBase>> comparator) {
        this.key = "targeting.priority.entity." + key;
        this.comparator = comparator;
    }

    @Override
    public String toString() {
        return key;
    }
    @Override
    public String getName() {
        return FrostCore.getLocalizer().get("strings." + this + ".name");
    }
}
