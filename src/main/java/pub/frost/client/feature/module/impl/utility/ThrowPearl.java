package pub.frost.client.feature.module.impl.utility;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.utils.EnumModuleToggleType;
import pub.frost.utils.InputUtils;

@Module(
        key = "ThrowPearl",
        category = ModuleCategory.UTILITY
)
public class ThrowPearl extends AbstractModule {
    @Property("SwitchTiming")
    public final ModeProperty<EnumModuleToggleType> switchTiming = new ModeProperty<>(EnumModuleToggleType.ON_ENABLE);
    @Property("ThrowTiming")
    public final ModeProperty<EnumModuleToggleType> throwTiming = new ModeProperty<>(EnumModuleToggleType.ON_DISABLE);
    @Property("SwitchBack")
    public final BooleanProperty switchBack = new BooleanProperty(true);

    private boolean foundPearl = false;
    private int switchFrom = 0;

    @Override
    protected void onEnabled() {
        if (switchTiming.is(EnumModuleToggleType.ON_ENABLE)) trySwitch();
        if (throwTiming.is(EnumModuleToggleType.ON_ENABLE)) tryThrow();
    }
    @Override
    protected void onDisabled() {
        if (switchTiming.is(EnumModuleToggleType.ON_DISABLE)) trySwitch();
        if (throwTiming.is(EnumModuleToggleType.ON_DISABLE)) tryThrow();
    }

    private void trySwitch() {
        InventoryPlayer inventory = mc.thePlayer.inventory;
        switchFrom = inventory.currentItem;
        foundPearl = false;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null || stack.stackSize < 1) continue;
            if (stack.getItem() instanceof ItemEnderPearl) {
                inventory.currentItem = slot;
                foundPearl = true;
                break;
            }
        }
    }
    private void tryThrow() {
        if (!foundPearl)
            return;
        InputUtils.clickRMB();
        if (switchBack.get()) mc.thePlayer.inventory.currentItem = switchFrom;
    }
}
