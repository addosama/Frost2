package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pub.frost.base.event.impl.events.EventPlayerVelocity;
import pub.frost.client.core.FrostCore;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Redirect(
            method = "handleEntityVelocity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setVelocity(DDD)V"
            )
    )
    public void handleEntityVelocity(Entity instance, double x, double y, double z) {
        if (instance instanceof EntityPlayerSP) {
            EventPlayerVelocity event = new EventPlayerVelocity(x, y, z);
            FrostCore.getInstance().getEventBus().call(event);
            if (!event.isCancelled()) {
                instance.setVelocity(
                        x * event.getXMultiplier(),
                        y * event.getYMultiplier(),
                        z * event.getZMultiplier()
                );
            }
        }
    }
}
