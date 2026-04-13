package pub.frost.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import pub.frost.client.core.FrostCore;
import pub.frost.wrappers.shared.item.WItemPickaxe;
import pub.frost.wrappers.shared.item.WItemStack;
import pub.frost.wrappers.shared.item.WItemTool;

public class ItemUtils {
    private static final WItemTool tool = FrostCore.getWrapper(WItemTool.class);
    private static final WItemPickaxe pickaxe = FrostCore.getWrapper(WItemPickaxe.class);
    private static final WItemStack itemStackWrapper = FrostCore.getWrapper(WItemStack.class);

    public static float getToolEfficiency(Object itemStack, Object block) {
        float efficiency = 1.0f;
        if (itemStack != null) {
            Object item = getItemInStack(itemStack);
            efficiency = couldItemHarvestBlock(itemStack, block) || !(pickaxe.isTarget(item))
                    ? itemStackWrapper.getStrVsBlock(itemStack, block) : 1.0f;
            if (tool.isTarget(item)) {
                int enchantLevel;
                if (efficiency > 1.0f && (enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, (ItemStack) itemStack)) > 0) {
                    efficiency += (float) (enchantLevel * enchantLevel + 1);
                }
            }
        }
        return efficiency;
    }

    public static boolean couldItemHarvestBlock(Object itemStack, Object block) {
        return itemStackWrapper.canHarvestBlock(itemStack, block);
    }

    public static Object getItemInStack(Object itemStack) {
        return itemStackWrapper.getItem(itemStack);
    }
}
