package pub.frost.client.feature.module.impl.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
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
import pub.frost.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

@Module(
        key = "InvManager",
        category = ModuleCategory.UTILITY
)
public class InvManager extends AbstractModule {
    @Property("MinDelay")
    public final IntegerProperty minDelay = new IntegerProperty(0, 20, 1, 1);
    @Property("MaxDelay")
    public final IntegerProperty maxDelay = new IntegerProperty(0, 20, 1, 2);
    @Property("OpenDelay")
    public final IntegerProperty openDelay = new IntegerProperty(0, 20, 1, 1);
    @Property("AutoArmor")
    public final BooleanProperty autoArmor = new BooleanProperty(true);
    @Property("DropTrash")
    public final BooleanProperty dropTrash = new BooleanProperty(false);
    @Property("SwordSlot")
    public final IntegerProperty swordSlot = new IntegerProperty(0, 9, 1, 1);
    @Property("PickaxeSlot")
    public final IntegerProperty pickaxeSlot = new IntegerProperty(0, 9, 1, 3);
    @Property("ShovelSlot")
    public final IntegerProperty shovelSlot = new IntegerProperty(0, 9, 1, 4);
    @Property("AxeSlot")
    public final IntegerProperty axeSlot = new IntegerProperty(0, 9, 1, 5);
    @Property("BlocksSlot")
    public final IntegerProperty blocksSlot = new IntegerProperty(0, 9, 1, 2);
    @Property("MaxBlocks")
    public final IntegerProperty blocks = new IntegerProperty(64, 2304, 64, 128);
    @Property("GappleSlot")
    public final IntegerProperty gappleSlot = new IntegerProperty(0, 9, 1, 0);
    @Property("KeepThrowables")
    public final BooleanProperty keepThrowables = new BooleanProperty(true);
    @Property("ThrowableSlot")
    public final IntegerProperty throwableSlot = new IntegerProperty(0, 9, 1, 0);

    private int actionDelay = 0;
    private int oDelay = 0;
    private boolean inventoryOpen = false;

    private boolean isValidGameMode() {
        Minecraft minecraft = (Minecraft) mc;
        WorldSettings.GameType gameType = minecraft.playerController.getCurrentGameType();
        return gameType == WorldSettings.GameType.SURVIVAL || gameType == WorldSettings.GameType.ADVENTURE;
    }

    private int convertSlotIndex(int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        } else {
            return slot <= 8 ? slot + 36 : slot;
        }
    }

    private void clickSlot(int windowId, int slotId, int mouseButtonClicked, int mode) {
        Minecraft minecraft = (Minecraft) mc;
        minecraft.playerController.windowClick(windowId, slotId, mouseButtonClicked, mode, minecraft.thePlayer);
        actionDelay = RandomUtils.nextInt(minDelay.get() + 1, maxDelay.get() + 2);
    }

    private int getStackSize(int slot) {
        Minecraft minecraft = (Minecraft) mc;
        if (slot == -1) {
            return 0;
        }
        ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
        return stack != null ? stack.stackSize : 0;
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    private void onUpdate(EventPlayerUpdateTick event) {
        if (event.getType() != TickType.PRE) return;

        Minecraft minecraft = (Minecraft) mc;
        if (actionDelay > 0) actionDelay--;
        if (oDelay > 0) oDelay--;

        if (!(minecraft.currentScreen instanceof GuiInventory)) {
            inventoryOpen = false;
            return;
        }
        if (!(((GuiInventory) minecraft.currentScreen).inventorySlots instanceof ContainerPlayer)) {
            inventoryOpen = false;
            return;
        }

        if (!inventoryOpen) {
            inventoryOpen = true;
            oDelay = openDelay.get() + 1;
        }

        if (oDelay > 0 || actionDelay > 0) return;
        if (!isEnabled() || !isValidGameMode()) return;

        ArrayList<Integer> equippedArmorSlots = new ArrayList<>(Arrays.asList(-1, -1, -1, -1));
        ArrayList<Integer> inventoryArmorSlots = new ArrayList<>(Arrays.asList(-1, -1, -1, -1));
        for (int i = 0; i < 4; i++) {
            equippedArmorSlots.set(i, ItemUtils.findArmorInventorySlot(minecraft.thePlayer.inventory, i, true));
            inventoryArmorSlots.set(i, ItemUtils.findArmorInventorySlot(minecraft.thePlayer.inventory, i, false));
        }

        int preferredSwordHotbarSlot = swordSlot.get() - 1;
        int equippedSwordSlot = ItemUtils.findSwordInInventorySlot(minecraft.thePlayer.inventory, preferredSwordHotbarSlot, true);
        int inventorySwordSlot = ItemUtils.findSwordInInventorySlot(minecraft.thePlayer.inventory, preferredSwordHotbarSlot, false);

        int preferredPickaxeHotbarSlot = pickaxeSlot.get() - 1;
        int equippedPickaxeSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "pickaxe", preferredPickaxeHotbarSlot, true);
        int inventoryPickaxeSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "pickaxe", preferredPickaxeHotbarSlot, false);

        int preferredShovelHotbarSlot = shovelSlot.get() - 1;
        int equippedShovelSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "shovel", preferredShovelHotbarSlot, true);
        int inventoryShovelSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "shovel", preferredShovelHotbarSlot, false);

        int preferredAxeHotbarSlot = axeSlot.get() - 1;
        int equippedAxeSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "axe", preferredAxeHotbarSlot, true);
        int inventoryAxeSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, "axe", preferredAxeHotbarSlot, false);

        int preferredBlocksHotbarSlot = blocksSlot.get() - 1;
        int inventoryBlocksSlot = ItemUtils.findInventorySlot(minecraft.thePlayer.inventory, preferredBlocksHotbarSlot);

        if (autoArmor.get()) {
            for (int i = 0; i < 4; i++) {
                int equippedSlot = equippedArmorSlots.get(i);
                int inventorySlot = inventoryArmorSlots.get(i);
                if (equippedSlot != -1 || inventorySlot != -1) {
                    int playerArmorSlot = 39 - i;
                    if (equippedSlot != playerArmorSlot && inventorySlot != playerArmorSlot) {
                        if (minecraft.thePlayer.inventory.getStackInSlot(playerArmorSlot) != null) {
                            if (minecraft.thePlayer.inventory.getFirstEmptyStack() != -1) {
                                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(playerArmorSlot), 0, 1);
                            } else {
                                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(playerArmorSlot), 1, 4);
                            }
                        } else {
                            int armorToEquipSlot = equippedSlot != -1 ? equippedSlot : inventorySlot;
                            clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(armorToEquipSlot), 0, 1);
                        }
                        return;
                    }
                }
            }
        }

        LinkedHashSet<Integer> usedHotbarSlots = new LinkedHashSet<>();
        if (preferredSwordHotbarSlot >= 0 && preferredSwordHotbarSlot <= 8
                && (equippedSwordSlot != -1 || inventorySwordSlot != -1)) {
            usedHotbarSlots.add(preferredSwordHotbarSlot);
            if (equippedSwordSlot != preferredSwordHotbarSlot && inventorySwordSlot != preferredSwordHotbarSlot) {
                int slot = equippedSwordSlot != -1 ? equippedSwordSlot : inventorySwordSlot;
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(slot), preferredSwordHotbarSlot, 2);
                return;
            }
        }

        if (preferredPickaxeHotbarSlot >= 0 && preferredPickaxeHotbarSlot <= 8
                && !usedHotbarSlots.contains(preferredPickaxeHotbarSlot)
                && (equippedPickaxeSlot != -1 || inventoryPickaxeSlot != -1)) {
            usedHotbarSlots.add(preferredPickaxeHotbarSlot);
            if (equippedPickaxeSlot != preferredPickaxeHotbarSlot && inventoryPickaxeSlot != preferredPickaxeHotbarSlot) {
                int slot = equippedPickaxeSlot != -1 ? equippedPickaxeSlot : inventoryPickaxeSlot;
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(slot), preferredPickaxeHotbarSlot, 2);
                return;
            }
        }

        if (preferredShovelHotbarSlot >= 0 && preferredShovelHotbarSlot <= 8
                && !usedHotbarSlots.contains(preferredShovelHotbarSlot)
                && (equippedShovelSlot != -1 || inventoryShovelSlot != -1)) {
            usedHotbarSlots.add(preferredShovelHotbarSlot);
            if (equippedShovelSlot != preferredShovelHotbarSlot && inventoryShovelSlot != preferredShovelHotbarSlot) {
                int slot = equippedShovelSlot != -1 ? equippedShovelSlot : inventoryShovelSlot;
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(slot), preferredShovelHotbarSlot, 2);
                return;
            }
        }

        if (preferredAxeHotbarSlot >= 0 && preferredAxeHotbarSlot <= 8
                && !usedHotbarSlots.contains(preferredAxeHotbarSlot)
                && (equippedAxeSlot != -1 || inventoryAxeSlot != -1)) {
            usedHotbarSlots.add(preferredAxeHotbarSlot);
            if (equippedAxeSlot != preferredAxeHotbarSlot && inventoryAxeSlot != preferredAxeHotbarSlot) {
                int slot = equippedAxeSlot != -1 ? equippedAxeSlot : inventoryAxeSlot;
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(slot), preferredAxeHotbarSlot, 2);
                return;
            }
        }

        if (preferredBlocksHotbarSlot >= 0 && preferredBlocksHotbarSlot <= 8
                && !usedHotbarSlots.contains(preferredBlocksHotbarSlot)
                && inventoryBlocksSlot != -1) {
            usedHotbarSlots.add(preferredBlocksHotbarSlot);
            if (inventoryBlocksSlot != preferredBlocksHotbarSlot) {
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(inventoryBlocksSlot), preferredBlocksHotbarSlot, 2);
                return;
            }
        }

        int preferredGappleHotbarSlot = gappleSlot.get() - 1;
        if (preferredGappleHotbarSlot >= 0 && preferredGappleHotbarSlot <= 8
                && !usedHotbarSlots.contains(preferredGappleHotbarSlot)) {
            int inventoryGappleSlot = ItemUtils.findAppleGoldInInventorySlot(minecraft.thePlayer.inventory);
            if (inventoryGappleSlot != -1 && inventoryGappleSlot != preferredGappleHotbarSlot) {
                usedHotbarSlots.add(preferredGappleHotbarSlot);
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(inventoryGappleSlot), preferredGappleHotbarSlot, 2);
                return;
            }
        }

        int preferredThrowableHotbarSlot = throwableSlot.get() - 1;
        if (keepThrowables.get() && preferredThrowableHotbarSlot >= 0 && preferredThrowableHotbarSlot <= 8
                && !usedHotbarSlots.contains(preferredThrowableHotbarSlot)) {
            int inventoryThrowableSlot = ItemUtils.findThrowableInInventorySlot(minecraft.thePlayer.inventory);
            if (inventoryThrowableSlot != -1 && inventoryThrowableSlot != preferredThrowableHotbarSlot) {
                usedHotbarSlots.add(preferredThrowableHotbarSlot);
                clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(inventoryThrowableSlot), preferredThrowableHotbarSlot, 2);
                return;
            }
        }

        if (dropTrash.get()) {
            int currentBlockCount = getStackSize(inventoryBlocksSlot);
            for (int i = 0; i < 36; i++) {
                if (!equippedArmorSlots.contains(i)
                        && !inventoryArmorSlots.contains(i)
                        && equippedSwordSlot != i
                        && inventorySwordSlot != i
                        && equippedPickaxeSlot != i
                        && inventoryPickaxeSlot != i
                        && equippedShovelSlot != i
                        && inventoryShovelSlot != i
                        && equippedAxeSlot != i
                        && inventoryAxeSlot != i
                        && inventoryBlocksSlot != i) {
                    ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (gappleSlot.get() > 0 && ItemUtils.isGoldenApple(stack)) continue;
                        if (keepThrowables.get() && ItemUtils.isThrowable(stack)) continue;
                        boolean isBlock = ItemUtils.isBlock(stack);
                        if (ItemUtils.isNotSpecialItem(stack) || isBlock && currentBlockCount >= blocks.get()) {
                            clickSlot(minecraft.thePlayer.inventoryContainer.windowId, convertSlotIndex(i), 1, 4);
                            return;
                        }
                        if (isBlock) {
                            currentBlockCount += stack.stackSize;
                        }
                    }
                }
            }
        }
    }
}
