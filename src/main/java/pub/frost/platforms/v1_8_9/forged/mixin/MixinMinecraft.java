package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventKeyInput;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Inject(
            method = "runTick",
            at = @At("HEAD")
    )
    private void preGameTick(CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(EventGameTick.PRE);
    }

    @Inject(
            method = "runTick",
            at = @At("TAIL")
    )
    private void postGameTick(CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(EventGameTick.POST);
    }

    @Redirect(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/input/Mouse;next()Z"
            )
    )
    private boolean handleMouse() {
        if (Mouse.next()) {
            int eventButton = Mouse.getEventButton();
            if (eventButton == -1) return true;
            EventKeyInput event = new EventKeyInput(
                    InputDevice.MOUSE,
                    -1 - eventButton,
                    Mouse.getEventButtonState()? 1 : 0
            );
            FrostCore.getInstance().getEventBus().call(event);
            return !event.isCancelled();
        }
        return false;
    }

    @Redirect(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/input/Keyboard;next()Z"
            )
    )
    private boolean handleKeyboard() {
        if (Keyboard.next()) {
            int eventKey = Keyboard.getEventKey();
            if (eventKey == 0) return true;

            EventKeyInput event = new EventKeyInput(
                    InputDevice.KEYBOARD,
                    eventKey,
                    Keyboard.getEventKeyState()? 1 : Keyboard.isRepeatEvent()? 2 : 0
            );
            FrostCore.getInstance().getEventBus().call(event);
            return !event.isCancelled();
        }
        return false;
    }
}
