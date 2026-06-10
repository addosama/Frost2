package pub.frost.utils.targeting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.utils.RotationUtils;

import java.util.Comparator;
import java.util.function.Function;

@Getter
@TranslationKey("strings.enum.targeting.priority.entity.~")
@RequiredArgsConstructor
public enum EnumEntityTargetPriority implements Named {
    ANGLE("angle", player -> {
        Vec3 eyePos = new Vec3(
                ((Entity) player).posX,
                ((Entity) player).posY + ((Entity) player).getEyeHeight(),
                ((Entity) player).posZ
        );
        float playerYaw = RotationUtils.wrapYawTo180(FrostCore.getInstance().getRotationManager().getPlayerYaw());
        return Comparator.comparingDouble(target -> Math.abs(
                RotationUtils.wrapYawTo180(RotationUtils.getRotationAimingPoint(eyePos, ((Entity) target).getPositionVector()).getYaw())
                - playerYaw
        ));
    }),
    DISTANCE("distance", player -> Comparator.comparingDouble(entity ->
            ((Entity) player).getDistanceToEntity((Entity) entity)
    )),
    HEALTH("health", p -> Comparator.comparingDouble(e -> ((EntityLivingBase) e).getHealth())),
    HURTTIME("hurttime", p -> Comparator.comparingInt(e -> ((EntityLivingBase) e).hurtTime)),;

    final String key;
    final Function<Object, Comparator<Object>> comparator;

    @Override
    public String toString() {
        return key;
    }
}
