package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventInput;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventPreTickLoop;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    private Timer timer;

    @Inject(
            method = "runTick",
            at = @At("HEAD")
    )
    private void preGameTick(CallbackInfo ci) {
        FrostCore.getEventBus().call(EventGameTick.PRE);
    }

    @Inject(
            method = "runTick",
            at = @At("TAIL")
    )
    private void postGameTick(CallbackInfo ci) {
        FrostCore.getEventBus().call(EventGameTick.POST);
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
            EventInput event = new EventInput(
                    InputDevice.MOUSE,
                    -1 - eventButton,
                    Mouse.getEventButtonState()? 1 : 0
            );
            FrostCore.getEventBus().call(event);
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

            EventInput event = new EventInput(
                    InputDevice.KEYBOARD,
                    eventKey,
                    Keyboard.getEventKeyState()? 1 : Keyboard.isRepeatEvent()? 2 : 0
            );
            FrostCore.getEventBus().call(event);
            return !event.isCancelled();
        }
        return false;
    }

    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/settings/KeyBinding;isPressed()Z",
                    ordinal = 10
            )
    )
    private void preProcessInteract(CallbackInfo ci) {
        FrostCore.getEventBus().call(new EventPreProcessInteract());
    }
    
    @Inject(
            method = "runGameLoop",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/Timer;elapsedTicks:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void preTickLoop(CallbackInfo ci) {
        FrostCore.getEventBus().call(new EventPreTickLoop(
                timer.elapsedTicks,
                timer.renderPartialTicks
        ));
    }

    @Inject(
            method = "shutdownMinecraftApplet",
            at = @At("HEAD")
    )
    private void onShutdown(CallbackInfo ci) {
        FrostCore.getInstance().shutdown();
    }
}
