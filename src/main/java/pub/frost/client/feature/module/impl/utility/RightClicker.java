package pub.frost.client.feature.module.impl.utility;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.InputUtils;

@Module(
        key = "RightClicker",
        category = ModuleCategory.UTILITY
)
public class RightClicker extends AbstractModule {
    @Property("cps")
    private final IntegerProperty cps = new IntegerProperty(1, 20, 1, 12);
    @Property("BlockCheck")
    private final BooleanProperty blockCheck = new BooleanProperty(true);

    private long lastClick = 0;
    private int clickCount = 0;

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (!blockCheck.get() || isHoldingBlock()) {
            if (InputUtils.isMouseDown(1)) {
                int minimumDelay = 1000 / cps.get();
                if (System.currentTimeMillis() > lastClick + minimumDelay) {
                    lastClick = System.currentTimeMillis();
                    clickCount++;
                    return;
                }
            }
        }
        resetRecorders();
    }

    @EventHandler
    private void onProcessInteract(EventPreProcessInteract e) {
        if (InputUtils.isMouseDown(1)) {
            while (clickCount > 0) {
                InputUtils.clickRMB();
                clickCount--;
            }
        } else resetRecorders();
    }

    private void resetRecorders() {
        clickCount = 0;
        lastClick = 0;
    }

    private boolean isHoldingBlock() {
        ItemStack itemHeld = mc.thePlayer.getHeldItem();
        if (itemHeld != null) {
            return itemHeld.getItem() instanceof ItemBlock;
        }
        return false;
    }
}
