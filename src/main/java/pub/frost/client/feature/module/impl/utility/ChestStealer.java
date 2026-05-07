package pub.frost.client.feature.module.impl.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.RandomUtils;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.BlockUtils;
import pub.frost.utils.ItemUtils;

@Module(
        key = "ChestStealer",
        category = ModuleCategory.UTILITY
)
public class ChestStealer extends AbstractModule {
    @Property("MinDelay")
    public final IntegerProperty minDelay = new IntegerProperty(0, 20, 1, 1);
    @Property("MaxDelay")
    public final IntegerProperty maxDelay = new IntegerProperty(0, 20, 1, 2);
    @Property("OpenDelay")
    public final IntegerProperty openDelay = new IntegerProperty(0, 20, 1, 1);
    @Property("AutoClose")
    public final BooleanProperty autoClose = new BooleanProperty(false);
    @Property("NameCheck")
    public final BooleanProperty nameCheck = new BooleanProperty(true);
    @Property("SkipTrash")
    public final BooleanProperty skipTrash = new BooleanProperty(true);

    private int clickDelay = 0;
    private int oDelay = 0;
    private boolean inChest = false;
    private boolean warnedFull = false;

    private boolean isValidGameMode() {
        Minecraft minecraft = (Minecraft) mc;
        WorldSettings.GameType gameType = minecraft.playerController.getCurrentGameType();
        return gameType == WorldSettings.GameType.SURVIVAL || gameType == WorldSettings.GameType.ADVENTURE;
    }

    private void shiftClick(int windowId, int slotId) {
        Minecraft minecraft = (Minecraft) mc;
        minecraft.playerController.windowClick(windowId, slotId, 0, 1, minecraft.thePlayer);
        clickDelay = RandomUtils.nextInt(minDelay.get() + 1, maxDelay.get() + 2);
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    private void onUpdate(EventPlayerUpdateTick event) {
        if (event.getType() != TickType.PRE) return;

        Minecraft minecraft = (Minecraft) mc;
        if (clickDelay > 0) clickDelay--;
        if (oDelay > 0) oDelay--;

        if (!(minecraft.currentScreen instanceof GuiChest)) {
            inChest = false;
            return;
        }

        Container container = ((GuiChest) minecraft.currentScreen).inventorySlots;
        if (!(container instanceof ContainerChest)) {
            inChest = false;
            return;
        }

        if (!inChest) {
            inChest = true;
            warnedFull = false;
            oDelay = openDelay.get() + 1;
        }

        if (oDelay > 0 || clickDelay > 0) return;
        if (!isEnabled() || !isValidGameMode()) return;

        IInventory inventory = ((ContainerChest) container).getLowerChestInventory();

        if (nameCheck.get()) {
            String inventoryName = inventory.getName();
            if (!inventoryName.equals(I18n.format("container.chest"))
                    && !inventoryName.equals(I18n.format("container.chestDouble"))) {
                return;
            }
        }

        if (minecraft.thePlayer.inventory.getFirstEmptyStack() == -1) {
            if (!warnedFull) {
                warnedFull = true;
            }
            if (autoClose.get()) {
                minecraft.thePlayer.closeScreen();
            }
            return;
        }

        if (skipTrash.get()) {
            int bestSword = -1;
            double bestDamage = 0.0;
            int[] bestArmorSlots = new int[]{-1, -1, -1, -1};
            double[] bestArmorProtection = new double[]{0.0, 0.0, 0.0, 0.0};
            int bestPickaxeSlot = -1;
            float bestPickaxeEfficiency = 1.0F;
            int bestShovelSlot = -1;
            float bestShovelEfficiency = 1.0F;
            int bestAxeSlot = -1;
            float bestAxeEfficiency = 1.0F;

            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                if (!container.getSlot(i).getHasStack()) continue;
                ItemStack stack = container.getSlot(i).getStack();
                Item item = stack.getItem();

                if (item instanceof ItemSword) {
                    double damage = ItemUtils.getAttackBonus(stack);
                    if (bestSword == -1 || damage > bestDamage) {
                        bestSword = i;
                        bestDamage = damage;
                    }
                } else if (item instanceof ItemArmor) {
                    int armorType = ((ItemArmor) item).armorType;
                    double protectionLevel = ItemUtils.getArmorProtection(stack);
                    if (bestArmorSlots[armorType] == -1 || protectionLevel > bestArmorProtection[armorType]) {
                        bestArmorSlots[armorType] = i;
                        bestArmorProtection[armorType] = protectionLevel;
                    }
                } else if (item instanceof ItemPickaxe) {
                    float efficiency = ItemUtils.getToolMaterialEfficiency(stack);
                    if (bestPickaxeSlot == -1 || efficiency > bestPickaxeEfficiency) {
                        bestPickaxeSlot = i;
                        bestPickaxeEfficiency = efficiency;
                    }
                } else if (item instanceof ItemSpade) {
                    float efficiency = ItemUtils.getToolMaterialEfficiency(stack);
                    if (bestShovelSlot == -1 || efficiency > bestShovelEfficiency) {
                        bestShovelSlot = i;
                        bestShovelEfficiency = efficiency;
                    }
                } else if (item instanceof ItemAxe) {
                    float efficiency = ItemUtils.getToolMaterialEfficiency(stack);
                    if (bestAxeSlot == -1 || efficiency > bestAxeEfficiency) {
                        bestAxeSlot = i;
                        bestAxeEfficiency = efficiency;
                    }
                }
            }

            int swordInInventorySlot = ItemUtils.findSwordInInventorySlot(minecraft.thePlayer.inventory, 0, true);
            double damage = swordInInventorySlot != -1
                    ? ItemUtils.getAttackBonus(minecraft.thePlayer.inventory.getStackInSlot(swordInInventorySlot)) : 0.0;
            if (bestDamage > damage) {
                shiftClick(container.windowId, bestSword);
                return;
            }

            for (int i = 0; i < 4; i++) {
                int slot = ItemUtils.findArmorInventorySlot(minecraft.thePlayer.inventory, i, true);
                double protectionLevel = slot != -1
                        ? ItemUtils.getArmorProtection(minecraft.thePlayer.inventory.getStackInSlot(slot)) : 0.0;
                if (bestArmorProtection[i] > protectionLevel) {
                    shiftClick(container.windowId, bestArmorSlots[i]);
                    return;
                }
            }

            int pickaxeSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "pickaxe", 0, true);
            float pickaxeEfficiency = pickaxeSlot != -1
                    ? ItemUtils.getToolMaterialEfficiency(minecraft.thePlayer.inventory.getStackInSlot(pickaxeSlot)) : 1.0F;
            if (bestPickaxeEfficiency > pickaxeEfficiency) {
                shiftClick(container.windowId, bestPickaxeSlot);
                return;
            }

            int shovelSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "shovel", 0, true);
            float shovelEfficiency = shovelSlot != -1
                    ? ItemUtils.getToolMaterialEfficiency(minecraft.thePlayer.inventory.getStackInSlot(shovelSlot)) : 1.0F;
            if (bestShovelEfficiency > shovelEfficiency) {
                shiftClick(container.windowId, bestShovelSlot);
                return;
            }

            int axeSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "axe", 0, true);
            float efficiency = axeSlot != -1
                    ? ItemUtils.getToolMaterialEfficiency(minecraft.thePlayer.inventory.getStackInSlot(axeSlot)) : 1.0F;
            if (bestAxeEfficiency > efficiency) {
                shiftClick(container.windowId, bestAxeSlot);
                return;
            }
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (!container.getSlot(i).getHasStack()) continue;
            ItemStack stack = container.getSlot(i).getStack();
            if (!skipTrash.get() || !ItemUtils.isNotSpecialItem(stack)) {
                shiftClick(container.windowId, i);
                return;
            }
        }

        if (autoClose.get()) {
            minecraft.thePlayer.closeScreen();
        }
    }
}
