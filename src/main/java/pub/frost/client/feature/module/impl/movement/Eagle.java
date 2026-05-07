package pub.frost.client.feature.module.impl.movement;

import imgui.ImGui;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
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

    private final WEntity entityWrapper = Entity;
    private final WWorld worldWrapper = World;

    private BlockPosition lastPos;
    private boolean onEdge;

    @EventHandler
    private void onPlayerUpdate(EventPlayerUpdateTick event) {
        BlockPosition pos = new BlockPosition(entityWrapper.getPositionVector(mcWrapper.getPlayer(mc))).offset(EnumDirection.DOWN);
        if (event.getType() == TickType.PRE) {
            onEdge = worldWrapper.isAirBlock(
                    mcWrapper.getWorld(mc),
                    pos
            );
        }
        lastPos = pos;
    }

    @EventHandler
    private void onRender2D(EventRender2D e) {
        if (lastPos == null) return;
        Object w = Minecraft.getWorld(mc);
        if (w == null) return;
        ImGui.getForegroundDrawList().addText(
                100f, 100f,
                0xFF0000FF,
                IBlockState.getBlock(World.getBlockState(w, lastPos)).toString()
        );
    }

    @EventHandler
    private void onMoveInput(EventUpdateMovementInput event) {
        if (pitchCheck.get() && entityWrapper.getPitch(mcWrapper.getPlayer(mc)) < 70) return;
        if (blockCheck.get() && !isHoldingBlock()) return;
        event.setSneak(onEdge || !modifyInput.get() && event.isSneak());
    }

    private boolean isHoldingBlock() {
        Object itemHeld = EntityPlayer.getHeldItem(mcWrapper.getPlayer(mc));
        if (itemHeld != null) {
            return ItemBlock.isTarget(ItemStack.getItem(itemHeld));
        }
        return false;
    }
}
