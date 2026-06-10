package pub.frost.client.feature.module.impl.utility;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemSword;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.InputUtils;
import pub.frost.utils.ItemUtils;
import net.minecraft.util.MovingObjectPosition;

@Module(
        key = "AutoTool",
        category = ModuleCategory.UTILITY
)
public class AutoTool extends AbstractModule {
    @Property("RequireMouseDown")
    public BooleanProperty requireMouseDown = new BooleanProperty(true);
    @Property("RequireSneak")
    public BooleanProperty requireSneak = new BooleanProperty(false);
    @Property("NotWhileSword")
    public BooleanProperty notWhileSword = new BooleanProperty(false);
    @Property("SwitchDelay")
    public IntegerProperty delay = new IntegerProperty(0, 20, 1, 1);

    @Property("AutoSwitchback")
    public BooleanProperty autoSwitchBack = new BooleanProperty(false);
    @Property("SwitchbackDelay")
    public IntegerProperty switchBackDelay = new IntegerProperty(0, 20, 1, 1).setVisibilitySupplier(autoSwitchBack::get);

    private int ticksSinceStartBreaking = -1;
    private int ticksSinceStopBreaking = -1;

    private int switchedFromSlot = -1;

    @EventHandler
    private void onUpdate(EventPlayerUpdateTick e) {
        EntityPlayerSP player = mc.thePlayer;

        if (FrostCore.getHelpers().getPlayerListener().isStopDiggingTick()) {
            ticksSinceStopBreaking = 0;
        }
        if (!FrostCore.getHelpers().getPlayerListener().isDigging()) {
            if (autoSwitchBack.get()) {
                if (ticksSinceStopBreaking > switchBackDelay.get()) {
                    switchBack();
                }
            }
        }

        if (requireMouseDown.get() && !InputUtils.isMouseDown(0)) return;
        if (requireSneak.get() && !player.isSneaking()) return;
        if (notWhileSword.get() && EntityUtils.isHoldingItem(player, ItemSword.class)) return;

        if (FrostCore.getHelpers().getPlayerListener().isStartDiggingTick()) {
            ticksSinceStartBreaking = 0;
        }
        if (ticksSinceStartBreaking >= delay.get()) {
            switchBestTool();
        }
    }

    @EventHandler
    private void onPostGameTick(EventGameTick e) {
        if (e.getType() == TickType.POST) {
            if (FrostCore.getHelpers().getPlayerListener().isDigging()) {
                ticksSinceStopBreaking = -1;
                ticksSinceStartBreaking++;
            }
            else {
                ticksSinceStartBreaking = -1;
                ticksSinceStopBreaking++;
            }
        }
    }

    private void switchBestTool() {
        EntityPlayerSP player = mc.thePlayer;
        MovingObjectPosition hitResult = EntityUtils.getLookingObject(
                player,
                player.getLook(1),
                3, 1
        );
        if (hitResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        Block block = player.worldObj.getBlockState(hitResult.getBlockPos()).getBlock();

        InventoryPlayer inventory = player.inventory;
        int current = inventory.currentItem;
        int best = current;
        float vl = ItemUtils.getToolEfficiency(inventory.getStackInSlot(best), block);
        for (int i = 0; i <= 8; i++) {
            Object item = inventory.getStackInSlot(i);
            float nextVL = ItemUtils.getToolEfficiency(item, block);
            if (nextVL > vl) {
                best = i;
                vl = nextVL;
            }
        }
        inventory.currentItem = best;
        if (switchedFromSlot == -1 && current != best) switchedFromSlot = current;
    }
    private void switchBack() {
        if (switchedFromSlot != -1) {
            InventoryPlayer inventory = mc.thePlayer.inventory;
            inventory.currentItem = switchedFromSlot;
            switchedFromSlot = -1;
        }
    }

    @Override
    protected void onEnabled() {
        ticksSinceStartBreaking = -1;
        ticksSinceStopBreaking = -1;
    }
}
