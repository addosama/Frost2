package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.client.core.FrostCore;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;flushOutboundQueue()V"),
            cancellable = true
    )
    public void preSendPacket(Packet packetIn, CallbackInfo ci) {
        EventPacket event = new EventPacket(PacketType.OUT, packetIn);
        FrostCore.getInstance().getEventBus().call(event);
        if (event.isCancelled()) ci.cancel();
    }
}
