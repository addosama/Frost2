package pub.frost.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.wrappers.shared.item.WItemPickaxe;
import pub.frost.wrappers.shared.item.WItemStack;
import pub.frost.wrappers.shared.item.WItemTool;

public class ItemUtils implements Wrappers {
    public static float getToolEfficiency(Object itemStack, Object block) {
        float efficiency = 1.0f;
        if (itemStack != null) {
            Object item = getItemInStack(itemStack);
            efficiency = couldItemHarvestBlock(itemStack, block) || !(ItemPickaxe.isTarget(item))
                    ? ItemStack.getStrVsBlock(itemStack, block) : 1.0f;
            if (ItemTool.isTarget(item)) {
                int enchantLevel;
                if (efficiency > 1.0f && (enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, (ItemStack) itemStack)) > 0) {
                    efficiency += (float) (enchantLevel * enchantLevel + 1);
                }
            }
        }
        return efficiency;
    }

    public static boolean couldItemHarvestBlock(Object itemStack, Object block) {
        return ItemStack.canHarvestBlock(itemStack, block);
    }

    public static Object getItemInStack(Object itemStack) {
        return ItemStack.getItem(itemStack);
    }
}
