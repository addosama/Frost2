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
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventPreTickLoop;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.helper.game.state.EnumGameState;
import pub.frost.utils.InputUtils;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    private Timer timer;

    @Inject(
            method = "runTick",
            at = @At("HEAD")
    )
    private void preGameTick(CallbackInfo ci) {
        FrostCore.getHelpers().getGameStateListener().setCurrent(EnumGameState.PreGameTick);
        FrostCore.getEventBus().call(EventGameTick.PRE);
    }

    @Inject(
            method = "runTick",
            at = @At("TAIL")
    )
    private void postGameTick(CallbackInfo ci) {
        FrostCore.getHelpers().getGameStateListener().setCurrent(EnumGameState.PostGameTick);
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
        while (Mouse.next()) {
            int eventButton = Mouse.getEventButton();
            boolean ret;

            if (eventButton != -1) {
                ret = FrostCore.getInputManager().onMouseButton(eventButton, Mouse.getEventButtonState());
            }
            else {
                int dWheel = Mouse.getDWheel();
                float x = 0, y = 0;
                if (InputUtils.isKeyDown(Keyboard.KEY_LSHIFT))
                    x = dWheel;
                else y = dWheel;

                ret = FrostCore.getInputManager().onMouseScroll(x, y);
            }

            if (ret) return true;
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
        while (Keyboard.next()) {
            int eventKey = Keyboard.getEventKey();
            boolean ret;

            if (eventKey == 0) {
                ret = FrostCore.getInputManager().onChar(Keyboard.getEventCharacter());
            }
            else ret = FrostCore.getInputManager().onKey(eventKey, Keyboard.getEventKeyState());

            if (ret) return true;
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
        FrostCore.getHelpers().getGameStateListener().setCurrent(EnumGameState.PreTickLoop);
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
