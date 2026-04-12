package pub.frost.client.feature.helper;

import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.utils.MathUtils;
import pub.frost.utils.RotationUtils;
import pub.frost.wrappers.shared.client.WMinecraft;
import pub.frost.wrappers.shared.entity.WEntityClientPlayer;

@Getter
public class RotationManager {
    @Setter
    private float playerYaw, playerPitch, prevPlayerYaw, prevPlayerPitch;
    private float silentYaw, silentPitch, prevSilentYaw, prevSilentPitch;

    private boolean applied;
    private float targetYaw, targetPitch;
    private float speed;
    private boolean lockView;

    private void processSilentRotation() {
        float nextSilentYaw = getSilentYaw(), nextSilentPitch = getSilentPitch();

        if (speed > 0) {
            float deltaYaw = RotationUtils.wrapYawTo180(targetYaw - nextSilentYaw);
            float deltaPitch = targetPitch - nextSilentPitch;
            nextSilentYaw += MathUtils.clamp(deltaYaw, -speed, speed);
            nextSilentPitch += MathUtils.clamp(deltaPitch, -speed, speed);
        } else {
            nextSilentYaw = targetYaw;
            nextSilentPitch = targetPitch;
        }
        nextSilentPitch = MathUtils.clamp(nextSilentPitch, -90f, 90f);

        setSilentYaw(nextSilentYaw);
        setSilentPitch(nextSilentPitch);
    }

    private boolean postRotationEvent() {
        WEntityClientPlayer player = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance().getPlayer();
        EventRotation event = new EventRotation(player.getYaw(), player.getPitch(), 180, false);
        FrostCore.getInstance().getEventBus().call(event);
        targetYaw = event.getYaw();
        targetPitch = event.getPitch();
        speed = MathUtils.clamp(event.getSpeed(), 0f, 180f);
        lockView = event.isLockView();

        return targetYaw != silentYaw || targetPitch != silentPitch;
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
        double angleDifference = RotationUtils.wrapYawTo180(RotationUtils.getDirection(getPlayerYaw(), forward, strafe) - yaw);
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
        WEntityClientPlayer player = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance().getPlayer();
        if (player == null) return;
        if (e.getType() == TickType.PRE) {
            setPrevPlayerYaw(player.getPrevYaw());
            setPrevPlayerPitch(player.getPrevPitch());
            setPlayerYaw(player.getYaw());
            setPlayerPitch(player.getPitch());

            if (!postRotationEvent()) {
                applied = false;
                return;
            }

            processSilentRotation();

            player.setPrevYaw(getPrevSilentYaw());
            player.setPrevPitch(getPrevSilentPitch());
            player.setYaw(getSilentYaw());
            player.setPitch(getSilentPitch());

            if (lockView) {
                setPrevPlayerYaw(getPrevSilentYaw());
                setPrevPlayerPitch(getPrevSilentPitch());
                setPlayerYaw(getSilentYaw());
                setPlayerPitch(getSilentPitch());
            }

            applied = true;
        } else {
            if (!applied) return;

            player.setPrevYaw(getPrevPlayerYaw());
            player.setPrevPitch(getPrevPlayerPitch());
            player.setYaw(getPlayerYaw());
            player.setPitch(getPlayerPitch());

            applied = false;
        }
    }
}