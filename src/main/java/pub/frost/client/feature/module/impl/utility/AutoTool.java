package pub.frost.client.feature.module.impl.utility;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.utils.InputUtils;
import pub.frost.utils.ItemUtils;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.wrappers.shared.block.WIBlockState;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityClientPlayer;
import pub.frost.wrappers.shared.player.WInventoryPlayer;
import pub.frost.wrappers.shared.world.WWorld;

@Module(
        key = "AutoTool",
        category = ModuleCategory.UTILITY
)
public class AutoTool extends AbstractModule {
    @Property("RequireMouseDown")
    public BooleanProperty requireMouseDown = new BooleanProperty(true);

    private final WEntity entityWrapper = Wrappers.Entity;
    private final WEntityClientPlayer playerWrapper = Wrappers.EntityClientPlayer;
    private final WWorld worldWrapper = Wrappers.World;
    private final WInventoryPlayer inventoryWrapper = Wrappers.InventoryPlayer;
    private final WIBlockState blockStateWrapper = Wrappers.IBlockState;

    @EventHandler
    public void onUpdate(EventPlayerUpdateTick e) {
        if (requireMouseDown.get() && !InputUtils.isMouseDown(0)) return;
        if (FrostCore.getInstance().getPlayerListener().isStartDiggingTick()) {
            Object player = mcWrapper.getPlayer(mc);
            HitResult hitResult = entityWrapper.rayTrace(
                    player,
                    entityWrapper.getLook(player, 1),
                    3, 1
            );
            if (hitResult.getType() != HitResult.EnumHitType.BLOCK) return;

            Object block = blockStateWrapper.getBlock(worldWrapper.getBlockState(mcWrapper.getWorld(mc), hitResult.getBlockPos()));

            Object inventory = playerWrapper.getInventory(player);
            int best = inventoryWrapper.getCurrentItem(inventory);
            float vl = ItemUtils.getToolEfficiency(inventoryWrapper.getStackInSlot(inventory, best), block);
            for (int i = 0; i <= 8; i++) {
                Object item = inventoryWrapper.getStackInSlot(inventory, i);
                float nextVL = ItemUtils.getToolEfficiency(item, block);
                if (nextVL > vl) {
                    best = i;
                    vl = nextVL;
                }
            }
            inventoryWrapper.setCurrentItem(inventory, best);
        }
    }
}
