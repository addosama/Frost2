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
        WEntityLivingBase wrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WEntityLivingBase.class);
        Vector3d eyePos = wrapper.getPositionEyes(player, 1);
        float playerYaw = RotationUtils.wrapYawTo180(wrapper.getYaw(player));
        return Comparator.comparingDouble(target -> Math.abs(
                RotationUtils.wrapYawTo180(RotationUtils.getRotationAimingPoint(eyePos, wrapper.getPositionVector(target)).getYaw())
                - playerYaw
        ));
    }),
    DISTANCE("distance", player -> Comparator.comparingDouble(entity -> FrostCore.getInstance().getWrapperManager().getWrapper(WEntityLivingBase.class).distanceTo(player, FrostCore.getInstance().getWrapperManager().getWrapper(WEntityLivingBase.class).getPositionVector(entity)))),
    HEALTH("health", p -> Comparator.comparingDouble(FrostCore.getInstance().getWrapperManager().getWrapper(WEntityLivingBase.class)::getHealth)),
    HURTTIME("hurttime", p -> Comparator.comparingInt(FrostCore.getInstance().getWrapperManager().getWrapper(WEntityLivingBase.class)::getHurtTime)),;

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
