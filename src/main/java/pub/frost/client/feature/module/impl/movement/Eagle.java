package pub.frost.client.feature.module.impl.movement;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.EnumDirection;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityPlayer;
import pub.frost.wrappers.shared.item.WItemBlock;
import pub.frost.wrappers.shared.item.WItemStack;
import pub.frost.wrappers.shared.world.WWorld;

@Module(
        key = "eagle",
        category = ModuleCategory.MOVEMENT
)
public class Eagle extends AbstractModule {
    @Property("PitchCheck")
    public final BooleanProperty pitchCheck = new BooleanProperty(true);
    @Property("BlockCheck")
    public final BooleanProperty blockCheck = new BooleanProperty(true);
    @Property("ModifyInput")
    public final BooleanProperty modifyInput = new BooleanProperty(true);

    private final WEntity entityWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WEntity.class);
    private final WWorld worldWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WWorld.class);

    private boolean onEdge;

    @EventHandler
    private void onPlayerUpdate(EventPlayerUpdateTick event) {
        if (event.getType() == TickType.PRE) {
            onEdge = worldWrapper.isAirBlock(
                    mcWrapper.getWorld(mc),
                    new BlockPosition(entityWrapper.getPositionVector(mcWrapper.getPlayer(mc))).offset(EnumDirection.DOWN)
            );
        }
    }

    @EventHandler
    private void onMoveInput(EventUpdateMovementInput event) {
        boolean sneak = false;
        if (onEdge) {
            sneak = true;
            if (pitchCheck.get() && entityWrapper.getPitch(mcWrapper.getPlayer(mc)) < 70) sneak = false;
            if (blockCheck.get() && !isHoldingBlock()) sneak = false;
        }
        event.setSneak(modifyInput.get()? sneak : onEdge);
    }

    private boolean isHoldingBlock() {
        Object itemHeld = FrostCore.getWrapper(WEntityPlayer.class).getHeldItem(mcWrapper.getPlayer(mc));
        if (itemHeld != null) {
            return FrostCore.getWrapper(WItemBlock.class).isTarget(FrostCore.getWrapper(WItemStack.class).getItem(itemHeld));
        }
        return false;
    }
}
