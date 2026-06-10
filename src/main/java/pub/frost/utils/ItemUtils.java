package pub.frost.utils;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Iterator;

public class ItemUtils {
    public static float getToolEfficiency(Object itemStack, Object block) {
        float efficiency = 1.0f;
        if (itemStack instanceof ItemStack) {
            ItemStack stack = (ItemStack) itemStack;
            Item item = stack.getItem();
            efficiency = couldItemHarvestBlock(stack, (Block) block) || !(item instanceof ItemPickaxe)
                    ? stack.getStrVsBlock((Block) block) : 1.0f;
            if (item instanceof ItemTool) {
                int enchantLevel;
                if (efficiency > 1.0f && (enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack)) > 0) {
                    efficiency += (float) (enchantLevel * enchantLevel + 1);
                }
            }
        }
        return efficiency;
    }

    public static boolean couldItemHarvestBlock(ItemStack itemStack, Block block) {
        return itemStack.canHarvestBlock(block);
    }

    public static Item getItemInStack(ItemStack itemStack) {
        return itemStack.getItem();
    }

    // === Ported from skid ItemUtil ===

    private static final ArrayList<Integer> specialItems = new SpecialItems();

    public static boolean isNotSpecialItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemBlock) {
            return !ItemUtils.isContainerBlock((ItemBlock) item);
        }
        if (item instanceof ItemPotion) {
            return ((ItemPotion) item).getEffects(itemStack).stream()
                    .map(PotionEffect::getPotionID).noneMatch(specialItems::contains);
        }
        if (item instanceof ItemEnderPearl) return false;
        if (item instanceof ItemFood) {
            if (item != Items.spider_eye) return false;
        }
        return item != Items.nether_star;
    }

    public static boolean isGoldenApple(ItemStack itemStack) {
        if (itemStack == null) return false;
        return itemStack.getItem() instanceof ItemAppleGold;
    }

    public static boolean isThrowable(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item item = itemStack.getItem();
        return item instanceof ItemEnderPearl
                || item instanceof ItemEgg
                || item instanceof ItemSnowball
                || item instanceof ItemExpBottle
                || (item instanceof ItemPotion && ItemPotion.isSplash(itemStack.getItemDamage()));
    }

    public static boolean isBlock(ItemStack itemStack) {
        if (itemStack == null || itemStack.stackSize < 1) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemBlock) {
            return ItemUtils.isContainerBlock((ItemBlock) item);
        }
        return false;
    }

    public static boolean isContainerBlock(ItemBlock itemBlock) {
        net.minecraft.block.Block block = itemBlock.getBlock();
        if (BlockUtils.isInteractable(block)) return false;
        return BlockUtils.isSolid(block);
    }

    public static double getAttackBonus(ItemStack itemStack) {
        double attackBonus = 0.0;
        if (itemStack == null) {
            return 0.0;
        }
        Multimap<String, AttributeModifier> multimap = itemStack.getAttributeModifiers();
        for (String attributeName : multimap.keySet()) {
            if (!attributeName.equals("generic.attackDamage")) continue;
            Iterator<AttributeModifier> iterator = multimap.get(attributeName).iterator();
            if (!iterator.hasNext()) break;
            attackBonus += (iterator.next()).getAmount();
            break;
        }
        if (itemStack.isItemEnchanted()) {
            attackBonus = attackBonus
                    + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack)
                    + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25;
        }
        return attackBonus;
    }

    public static float getToolMaterialEfficiency(ItemStack itemStack) {
        float efficiency = 1.0f;
        if (itemStack != null) {
            if (itemStack.getItem() instanceof ItemTool) {
                int enchantLevel;
                efficiency = ((ItemTool) itemStack.getItem()).getToolMaterial().getEfficiencyOnProperMaterial();
                if (efficiency > 1.0f
                        && (enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack)) > 0) {
                    efficiency += (float) (enchantLevel * enchantLevel + 1);
                }
            }
        }
        return efficiency;
    }

    public static double getArmorProtection(ItemStack itemStack) {
        double protection = 0.0;
        if (itemStack != null) {
            if (itemStack.getItem() instanceof ItemArmor) {
                protection = 0.0 + (double) ((ItemArmor) itemStack.getItem()).damageReduceAmount;
                if (itemStack.isItemEnchanted()) {
                    protection += (double) EnchantmentHelper.getEnchantmentLevel(
                            Enchantment.protection.effectId, itemStack) * 0.25;
                }
            }
        }
        return protection;
    }

    public static int findSwordInInventorySlot(InventoryPlayer inventory, int startSlot, boolean checkDurability) {
        int bestSlot = -1;
        double bestAttackBonus = 0.0;
        for (int i = 0; i < 36; ++i) {
            int currentSlot = ((startSlot + i) % 36 + 36) % 36;
            ItemStack itemStack = inventory.getStackInSlot(currentSlot);
            if (itemStack == null) continue;
            if (!(itemStack.getItem() instanceof ItemSword)) continue;
            if (checkDurability) {
                if (itemStack.isItemDamaged()) {
                    if (itemStack.getMaxDamage() - itemStack.getItemDamage() < 30) {
                        continue;
                    }
                }
            }
            double attackBonus = ItemUtils.getAttackBonus(itemStack);
            if (!(attackBonus > bestAttackBonus)) continue;
            bestSlot = currentSlot;
            bestAttackBonus = attackBonus;
        }
        return bestSlot;
    }

    public static int findInventorySlot(InventoryPlayer inventory, String toolClass, int startSlot, boolean checkDurability) {
        int bestSlot = -1;
        float bestEfficiency = 1.0f;
        for (int i = 0; i < 36; ++i) {
            int currentSlot = ((startSlot + i) % 36 + 36) % 36;
            ItemStack itemStack = inventory.getStackInSlot(currentSlot);
            if (itemStack == null) continue;
            if (!(itemStack.getItem() instanceof ItemTool)) continue;
            if (!itemStack.getItem().getToolClasses(itemStack).contains(toolClass)) continue;
            if (checkDurability) {
                if (itemStack.isItemDamaged()) {
                    if (itemStack.getMaxDamage() - itemStack.getItemDamage() < 30) {
                        continue;
                    }
                }
            }
            float efficiency = ItemUtils.getToolMaterialEfficiency(itemStack);
            if (!(efficiency > bestEfficiency)) continue;
            bestSlot = currentSlot;
            bestEfficiency = efficiency;
        }
        return bestSlot;
    }

    public static int findInventorySlot(InventoryPlayer inventory, int startSlot) {
        int bestSlot = -1;
        int maxStackSize = 0;
        for (int i = 0; i < 36; ++i) {
            int currentSlot = ((startSlot + i) % 36 + 36) % 36;
            ItemStack itemStack = inventory.getStackInSlot(currentSlot);
            if (itemStack == null) continue;
            if (!ItemUtils.isBlock(itemStack)) continue;
            if (maxStackSize >= itemStack.stackSize) continue;
            bestSlot = currentSlot;
            maxStackSize = itemStack.stackSize;
        }
        return bestSlot;
    }

    public static int findArmorInventorySlot(InventoryPlayer inventory, int armorType, boolean checkDurability) {
        int bestSlot = -1;
        double bestProtection = 0.0;
        for (int i = 0; i < 40; ++i) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack == null) continue;
            if (!(itemStack.getItem() instanceof ItemArmor)) continue;
            if (((ItemArmor) itemStack.getItem()).armorType != armorType) {
                continue;
            }
            if (checkDurability) {
                if (itemStack.isItemDamaged()) {
                    if (itemStack.getMaxDamage() - itemStack.getItemDamage() < 30) {
                        continue;
                    }
                }
            }
            double protection = ItemUtils.getArmorProtection(itemStack);
            if (!(protection >= bestProtection)) continue;
            bestSlot = i;
            bestProtection = protection;
        }
        return bestSlot;
    }

    public static int findAppleGoldInInventorySlot(InventoryPlayer inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (isGoldenApple(stack)) return i;
        }
        return -1;
    }

    public static int findThrowableInInventorySlot(InventoryPlayer inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (isThrowable(stack)) return i;
        }
        return -1;
    }

    static final class SpecialItems extends ArrayList<Integer> {
        SpecialItems() {
            this.add(1);
            this.add(3);
            this.add(5);
            this.add(6);
            this.add(8);
            this.add(10);
            this.add(11);
            this.add(12);
            this.add(14);
            this.add(21);
            this.add(22);
        }
    }
}
