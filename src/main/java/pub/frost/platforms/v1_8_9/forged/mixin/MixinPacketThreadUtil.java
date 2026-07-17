package pub.frost.platforms.v1_8_9.forged.mixin;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.util.IThreadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.client.core.FrostCore;

@Mixin(PacketThreadUtil.class)
public class MixinPacketThreadUtil {
    @Redirect(
            method = "checkThreadAndEnqueue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/IThreadListener;addScheduledTask(Ljava/lang/Runnable;)Lcom/google/common/util/concurrent/ListenableFuture;"
            )
    )
    private static <T extends INetHandler> ListenableFuture<Object> onProcess(IThreadListener instance, Runnable runnable, final Packet<T> packet, final T iNetHandler, IThreadListener iThreadListener) {
        return instance.addScheduledTask(() -> {
            if (!FrostCore.getHelpers().getPacketManager().processIncoming(packet)) {
                packet.processPacket(iNetHandler);
            }
        });
    }
}
