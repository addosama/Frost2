package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventHandleMouseInput;
import pub.frost.client.core.FrostCore;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {
    @Shadow
    public int deltaX;

    @Shadow
    public int deltaY;

    @Inject(
            method = "mouseXYChange",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onMouseXYChange(CallbackInfo ci) {
        EventHandleMouseInput event = new EventHandleMouseInput(Mouse.getDX(), Mouse.getDY());
        FrostCore.getEventBus().call(event);
        this.deltaX = event.getDeltaX();
        this.deltaY = event.getDeltaY();
        ci.cancel();
    }
}
