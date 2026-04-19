package pub.frost.client.feature.module.impl.combat;

import net.minecraft.network.play.client.C0APacketAnimation;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;

import java.util.ArrayDeque;
import java.util.Deque;

@Module(
        key = "HitFlick",
        category = ModuleCategory.COMBAT
)
public class HitFlick extends AbstractModule {
    private int tickSinceAttack = -1;
    private final Deque<Object> storedPackets = new ArrayDeque<>();

    private boolean reset = false;

    @EventHandler
    private void onAttackPacket(EventPacket event) {
        if (event.isCancelled()) return;
        if (event.getType() == PacketType.OUT) {
            Object packet = event.getPacket();
            if (UseEntityPacket.isTarget(packet)) {
                if (!EntityPlayer.isTarget(UseEntityPacket.getEntityFromWorld(packet, mcWrapper.getWorld(mc)))) return;
                tickSinceAttack = 0;
                storedPackets.offerLast(packet);
                event.cancel();
            } else if (PlayerPacket.isTarget(packet)) {
                if (tickSinceAttack >= 0) {
                    System.out.printf(
                            "c03 at tick %s, yaw:%s\n",
                            tickSinceAttack,
                            PlayerPacket.getYaw(packet)
                    );
                }
                if (tickSinceAttack >= 1) {
                    System.out.printf(
                            "reset at tick%s, yaw:%s\n",
                            tickSinceAttack,
                            Entity.getYaw(mcWrapper.getPlayer(mc))
                    );
                    reset = false;
                    tickSinceAttack = -1;
                    while (!storedPackets.isEmpty()) {
                        Object p = storedPackets.pollFirst();
                        if (UseEntityPacket.isTarget(p)) {
                            FrostCore.getInstance().getPacketManager().sendPacket(new C0APacketAnimation(), false);
                        }
                        FrostCore.getInstance().getPacketManager().sendPacket(p, false);
                    }
                }
            }
            if (tickSinceAttack >= 0 && !event.isCancelled()) {
                storedPackets.offerLast(packet);
                event.cancel();
            }
        }
    }

    @EventHandler
    private void postGameTick(EventGameTick event) {
        if (event.getType() == TickType.POST) {
            if (tickSinceAttack >= 1) {
            }
            if (tickSinceAttack >= 0) tickSinceAttack++;
        }
    }

    @EventHandler
    private void onProcessInteract(EventPreProcessInteract e) {
        if (tickSinceAttack >= 1) {
        }
    }

    @EventHandler(priority = 20)
    private void onProcessRotation(EventRotation e) {
        if (tickSinceAttack >= 0) {
            System.out.printf("ProcessRotation %s\n", tickSinceAttack);
            e.setYaw(e.getYaw() + 95);
            e.setSpeed(0);
        }
    }

    @Override
    protected void onDisabled() {
        tickSinceAttack = -1;
        storedPackets.clear();
    }
}
