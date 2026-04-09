package pub.frost.client.feature.helper;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MathHelper;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.utils.RotationUtils;
import pub.frost.wrappers.shared.client.WMinecraft;
import pub.frost.wrappers.shared.entity.WEntity;

@Getter
public class RotationManager {
    @Setter
    private float playerYaw, playerPitch;
    private float silentYaw, silentPitch, prevSilentYaw, prevSilentPitch;

    @Setter
    private static float targetYaw, targetPitch;
    private static float speed;

    private void processSilentRotation() {
        float nextSilentYaw = getSilentYaw(), nextSilentPitch = getSilentPitch();

        postRotationEvent();

        if (speed > 0) {
            float deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - nextSilentYaw);
            float deltaPitch = targetPitch - nextSilentPitch;
            nextSilentYaw += MathHelper.clamp_float(deltaYaw, -speed, speed);
            nextSilentPitch += MathHelper.clamp_float(deltaPitch, -speed, speed);
        } else {
            nextSilentYaw = targetYaw;
            nextSilentPitch = targetPitch;
        }
        nextSilentPitch = MathHelper.clamp_float(nextSilentPitch, -90, 90);

        setSilentYaw(nextSilentYaw);
        setSilentPitch(nextSilentPitch);
    }

    private void postRotationEvent() {
        EventRotation event = new EventRotation(getPlayerYaw(), getPlayerPitch(), 0);
        FrostCore.getInstance().getEventBus().call(event);
        setTargetYaw(event.getYaw());
        setTargetPitch(event.getPitch());
        speed = MathHelper.clamp_float(event.getSpeed(), 0, 180);
    }

    private void setSilentYaw(float silentYaw) {
        prevSilentYaw = this.silentYaw;
        this.silentYaw = silentYaw;
    }
    private void setSilentPitch(float silentPitch) {
        prevSilentPitch = this.silentPitch;
        this.silentPitch = silentPitch;
    }


    @EventHandler(priority = 100)
    private void onTickMoveInput(EventUpdateMovementInput e) {
        float forward = e.getMoveForward(), strafe = e.getMoveStrafe();

        float yaw = getSilentYaw();

        int angleUnit = 45;
        float angleTolerance = 22.5F;
        float directionFactor = Math.max(Math.abs(forward), Math.abs(strafe));
        double angleDifference = MathHelper.wrapAngleTo180_float(RotationUtils.getDirection(getPlayerYaw(), forward, strafe) - yaw);
        double angleDistance = Math.abs(angleDifference);
        forward = 0.0F;
        strafe = 0.0F;
        if (angleDistance <= (double) ((float) angleUnit + angleTolerance)) {
            forward++;
        } else if (angleDistance >= (double) (180.0F - (float) angleUnit - angleTolerance)) {
            forward--;
        }

        if (angleDifference >= (double) ((float) angleUnit - angleTolerance) && angleDifference <= (double) (180.0F - (float) angleUnit + angleTolerance)) {
            strafe--;
        } else if (angleDifference <= (double) ((float) (-angleUnit) + angleTolerance) && angleDifference >= (double) (-180.0F + (float) angleUnit - angleTolerance)) {
            strafe++;
        }

        forward *= directionFactor;
        strafe *= directionFactor;

        e.setMoveForward(forward);
        e.setMoveStrafe(strafe);
    }

    @EventHandler(priority = 100)
    private void onPreGameTick(EventGameTick e) {
        WEntity player = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance().getPlayer();
        if (player == null) return;
        if (e.getType() == TickType.PRE) {
            setPlayerYaw(player.getYaw());
            setPlayerPitch(player.getPitch());

            processSilentRotation();

            player.setYaw(getSilentYaw());
            player.setPitch(getSilentPitch());
        } else {
//            player.setYaw(getPlayerYaw());
//            player.setPitch(getPlayerPitch());
        }
    }
}
