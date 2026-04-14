package pub.frost.utils.targeting;

import lombok.Getter;
import org.joml.Vector3d;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.utils.RotationUtils;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;

import java.util.Comparator;
import java.util.function.Function;

@Getter
public enum EnumEntityTargetPriority implements Named {
    ANGLE("angle", player -> {
        WEntityLivingBase wrapper = Wrappers.EntityLivingBase;
        Vector3d eyePos = wrapper.getPositionEyes(player, 1);
        float playerYaw = RotationUtils.wrapYawTo180(FrostCore.getInstance().getRotationManager().getPlayerYaw());
        return Comparator.comparingDouble(target -> Math.abs(
                RotationUtils.wrapYawTo180(RotationUtils.getRotationAimingPoint(eyePos, wrapper.getPositionVector(target)).getYaw())
                - playerYaw
        ));
    }),
    DISTANCE("distance", player -> Comparator.comparingDouble(entity -> Wrappers.EntityLivingBase.distanceTo(player, Wrappers.EntityLivingBase.getPositionVector(entity)))),
    HEALTH("health", p -> Comparator.comparingDouble(Wrappers.EntityLivingBase::getHealth)),
    HURTTIME("hurttime", p -> Comparator.comparingInt(Wrappers.EntityLivingBase::getHurtTime)),;

    final String key;
    final Function<Object, Comparator<Object>> comparator;

    EnumEntityTargetPriority(String key, Function<Object, Comparator<Object>> comparator) {
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
