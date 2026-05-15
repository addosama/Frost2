package pub.frost.client.feature.module.impl.utility;

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
import pub.frost.utils.data.raytrace.HitResult;

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
        Object player = Minecraft.getPlayer(mc);

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
        if (requireSneak.get() && !Entity.isSneaking(player)) return;
        if (notWhileSword.get() && EntityUtils.isHoldingItem(player, ItemSword)) return;

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
        Object player = Minecraft.getPlayer(mc);
        HitResult hitResult = Entity.rayTrace(
                player,
                Entity.getLook(player, 1),
                3, 1
        );
        if (hitResult.getType() != HitResult.EnumHitType.BLOCK) return;

        Object block = IBlockState.getBlock(World.getBlockState(Minecraft.getWorld(mc), hitResult.getBlockPos()));

        Object inventory = EntityPlayer.getInventory(player);
        int current = InventoryPlayer.getCurrentItem(inventory);
        int best = current;
        float vl = ItemUtils.getToolEfficiency(InventoryPlayer.getStackInSlot(inventory, best), block);
        for (int i = 0; i <= 8; i++) {
            Object item = InventoryPlayer.getStackInSlot(inventory, i);
            float nextVL = ItemUtils.getToolEfficiency(item, block);
            if (nextVL > vl) {
                best = i;
                vl = nextVL;
            }
        }
        InventoryPlayer.setCurrentItem(inventory, best);
        if (switchedFromSlot == -1 && current != best) switchedFromSlot = current;
    }
    private void switchBack() {
        if (switchedFromSlot != -1) {
            Object inventory = EntityPlayer.getInventory(Minecraft.getPlayer(mc));
            InventoryPlayer.setCurrentItem(inventory, switchedFromSlot);
            switchedFromSlot = -1;
        }
    }

    @Override
    protected void onEnabled() {
        ticksSinceStartBreaking = -1;
        ticksSinceStopBreaking = -1;
    }
}
