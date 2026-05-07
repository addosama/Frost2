package pub.frost.client.feature.module.impl.movement;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomUtils;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.ItemUtils;
import pub.frost.utils.MoveUtils;
import pub.frost.utils.PlayerUtils;

@Module(
        key = "eagle",
        category = ModuleCategory.MOVEMENT
)
public class Eagle extends AbstractModule {
    @Property("MinDelay")
    public final IntegerProperty minDelay = new IntegerProperty(0, 10, 1, 2);
    @Property("MaxDelay")
    public final IntegerProperty maxDelay = new IntegerProperty(0, 10, 1, 3);
    @Property("DirectionCheck")
    public final BooleanProperty directionCheck = new BooleanProperty(true);
    @Property("PitchCheck")
    public final BooleanProperty pitchCheck = new BooleanProperty(true);
    @Property("BlocksOnly")
    public final BooleanProperty blocksOnly = new BooleanProperty(true);

    private int sneakDelay = 0;

    @Override
    protected void onInitialized() {
        minDelay.setValueChangeListener((old, current) -> {
            if (current > maxDelay.get()) {
                maxDelay.set(current);
            }
        });
        maxDelay.setValueChangeListener((old, current) -> {
            if (current < minDelay.get()) {
                minDelay.set(current);
            }
        });
    }

    private boolean canMoveSafely() {
        Minecraft minecraft = (Minecraft) mc;
        double[] offset = MoveUtils.predictMovement();
        return PlayerUtils.canMove(
                minecraft.thePlayer.motionX + offset[0],
                minecraft.thePlayer.motionZ + offset[1]
        );
    }

    private boolean shouldSneak() {
        Minecraft minecraft = (Minecraft) mc;
        if (directionCheck.get() && minecraft.gameSettings.keyBindForward.isKeyDown()) {
            return false;
        }
        if (pitchCheck.get() && minecraft.thePlayer.rotationPitch < 69.0F) {
            return false;
        }
        return (!blocksOnly.get() || isHoldingBlock()) && minecraft.thePlayer.onGround;
    }

    private boolean isHoldingBlock() {
        Minecraft minecraft = (Minecraft) mc;
        return ItemUtils.isBlock(minecraft.thePlayer.getHeldItem());
    }

    @EventHandler
    private void onTick(EventPlayerUpdateTick event) {
        if (event.getType() != TickType.PRE) return;
        if (!isEnabled()) return;

        if (sneakDelay > 0) {
            sneakDelay--;
        }
        if (sneakDelay == 0 && canMoveSafely()) {
            sneakDelay = RandomUtils.nextInt(minDelay.get(), maxDelay.get() + 1);
        }
    }

    @EventHandler
    private void onMoveInput(EventUpdateMovementInput event) {
        Minecraft minecraft = (Minecraft) mc;
        if (!isEnabled()) return;
        if (minecraft.currentScreen != null) return;
        if (minecraft.thePlayer.movementInput.sneak) return;

        if (shouldSneak() && (sneakDelay > 0 || canMoveSafely())) {
            minecraft.thePlayer.movementInput.sneak = true;
            minecraft.thePlayer.movementInput.moveStrafe *= 0.3F;
            minecraft.thePlayer.movementInput.moveForward *= 0.3F;
        }
    }

    @Override
    protected void onDisabled() {
        sneakDelay = 0;
    }
}
