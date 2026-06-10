package pub.frost.client.feature.helper.player.rotation;

import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.event.impl.types.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.helper.player.rotation.processors.post.EnumRotationPostProcessor;
import pub.frost.utils.MathUtils;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.Rotation;

import java.util.function.BiConsumer;

@Getter
public class RotationManager {
    protected final Minecraft mc = Minecraft.getMinecraft();

    @Setter
    private float playerYaw, playerPitch, prevPlayerYaw, prevPlayerPitch;
    private float silentYaw, silentPitch, prevSilentYaw, prevSilentPitch;

    private boolean applied;
    private float targetYaw, targetPitch;
    private float speed;
    private boolean lockView;
    private boolean disableYawWrapping;
    private int processors;

    private void processSilentRotation() {
        float nextSilentYaw = getSilentYaw(), nextSilentPitch = getSilentPitch();

        if (speed > 0) {
            float deltaYaw = targetYaw - nextSilentYaw;
            if (!disableYawWrapping) deltaYaw = RotationUtils.wrapYawTo180(deltaYaw);
            float deltaPitch = targetPitch - nextSilentPitch;
            nextSilentYaw += MathUtils.clamp(deltaYaw, -speed, speed);
            nextSilentPitch += MathUtils.clamp(deltaPitch, -speed, speed);
        } else {
            nextSilentYaw = targetYaw;
            nextSilentPitch = targetPitch;
        }

        final float[] rotation = new float[] { nextSilentYaw, nextSilentPitch };
        final BiConsumer<Float, Float> rotationAcceptor = (yaw, pitch) -> {
            if (yaw != null) rotation[0] = yaw;
            if (pitch != null) rotation[1] = pitch;
        };

        for (EnumRotationPostProcessor processor : EnumRotationPostProcessor.values()) {
            if (processor.isEnabled(processors)) {
                processor.process(
                        silentYaw, silentPitch,
                        rotation[0], rotation[1],
                        rotationAcceptor
                );
            }
        }

        nextSilentYaw = rotation[0];
        nextSilentPitch = MathUtils.clamp(rotation[1], -90f, 90f);

        setSilentYaw(nextSilentYaw);
        setSilentPitch(nextSilentPitch);
    }

    private boolean postRotationEvent() {
        EntityPlayerSP player = mc.thePlayer;
        EventRotation event = new EventRotation(player.rotationYaw, player.rotationPitch, 180, false, false, 0);
        FrostCore.getInstance().getEventBus().call(event);
        targetYaw = event.getYaw();
        targetPitch = event.getPitch();
        speed = event.getSpeed();
        lockView = event.isLockView();
        processors = event.getProcessors();

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

    public Rotation getCurrentSilentRotation() {
        return new Rotation(silentYaw, silentPitch);
    }
    public Rotation getCurrentPlayerRotation() {
        return new Rotation(playerYaw, playerPitch);
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
        if (e.getType() == TickType.PRE) {
            EntityPlayerSP player = mc.thePlayer;
            if (player == null) return;

            setPrevPlayerYaw(player.prevRotationYaw);
            setPrevPlayerPitch(player.prevRotationPitch);
            setPlayerYaw(player.rotationYaw);
            setPlayerPitch(player.rotationPitch);

            if (!postRotationEvent()) {
                applied = false;
                return;
            }

            processSilentRotation();

            player.prevRotationYaw = getPrevSilentYaw();
            player.prevRotationPitch = getPrevSilentPitch();
            player.rotationYaw = getSilentYaw();
            player.rotationPitch = getSilentPitch();

            if (lockView) {
                setPrevPlayerYaw(getPrevSilentYaw());
                setPrevPlayerPitch(getPrevSilentPitch());
                setPlayerYaw(getSilentYaw());
                setPlayerPitch(getSilentPitch());
            }

            applied = true;
        }
    }

    @EventHandler(priority = 100)
    private void onPostGameTick(EventGameTick e) {
        if (e.getType() == TickType.POST) {
            if (!applied) return;
            EntityPlayerSP player = mc.thePlayer;
            if (player == null) return;

            player.prevRotationYaw = getPrevPlayerYaw();
            player.prevRotationPitch = getPrevPlayerPitch();
            player.rotationYaw = getPlayerYaw();
            player.rotationPitch = getPlayerPitch();

            applied = false;
        }
    }
}