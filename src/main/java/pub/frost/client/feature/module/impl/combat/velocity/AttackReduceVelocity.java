package pub.frost.client.feature.module.impl.combat.velocity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.*;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;

import java.util.function.Supplier;

public class AttackReduceVelocity extends AbstractSubModule<Velocity> implements Supplier<Boolean> {
    public AttackReduceVelocity(Velocity velocity) {
        super(velocity);
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(false);

    @Property("TickExact")
    public final BooleanProperty tickExact = new BooleanProperty(true);

    @Property("Tick500")
    public final IntegerProperty tick500 = new IntegerProperty(0, 20, 1, 3).setVisibilitySupplier(tickExact::get);
    @Property("Tick1000")
    public final IntegerProperty tick1000 = new IntegerProperty(0, 20, 1, 4).setVisibilitySupplier(tickExact::get);
    @Property("Tick2000")
    public final IntegerProperty tick2000 = new IntegerProperty(0, 20, 1, 4).setVisibilitySupplier(tickExact::get);
    @Property("Tick3000")
    public final IntegerProperty tick3000 = new IntegerProperty(0, 20, 1, 5).setVisibilitySupplier(tickExact::get);
    @Property("Tick4000")
    public final IntegerProperty tick4000 = new IntegerProperty(0, 20, 1, 6).setVisibilitySupplier(tickExact::get);
    @Property("Tick5000")
    public final IntegerProperty tick5000 = new IntegerProperty(0, 20, 1, 6).setVisibilitySupplier(tickExact::get);
    @Property("Tick6000")
    public final IntegerProperty tick6000 = new IntegerProperty(0, 20, 1, 7).setVisibilitySupplier(tickExact::get);
    @Property("Tick7000")
    public final IntegerProperty tick7000 = new IntegerProperty(0, 20, 1, 7).setVisibilitySupplier(tickExact::get);
    @Property("Tick8000")
    public final IntegerProperty tick8000 = new IntegerProperty(0, 20, 1, 8).setVisibilitySupplier(tickExact::get);
    @Property("Tick9000")
    public final IntegerProperty tick9000 = new IntegerProperty(0, 20, 1, 8).setVisibilitySupplier(tickExact::get);
    @Property("Tick10000")
    public final IntegerProperty tick10000 = new IntegerProperty(0, 20, 1, 9).setVisibilitySupplier(tickExact::get);

    private int reduceTicks = 0;

    private boolean slot = false;
    private boolean attack = false;
    private boolean swing = false;
    private boolean block = false;
    private boolean inventory = false;
    private boolean dig = false;

    public void calculateReduceTicks(double motionX, double motionZ) {
        double kb = Math.hypot(motionX, motionZ);

        if (!tickExact.get()) {
            double ticks = 6.43153527E-4 * kb + 2.9419087136;
            int result = (int) Math.round(ticks);
            if (result < 1) result = 1;
            if (result > 10) result = 10;
            this.reduceTicks = result;
            return;
        }

        if (kb <= 500) this.reduceTicks = tick500.get();
        else if (kb <= 1000) this.reduceTicks = tick1000.get();
        else if (kb <= 2000) this.reduceTicks = tick2000.get();
        else if (kb <= 3000) this.reduceTicks = tick3000.get();
        else if (kb <= 4000) this.reduceTicks = tick4000.get();
        else if (kb <= 5000) this.reduceTicks = tick5000.get();
        else if (kb <= 6000) this.reduceTicks = tick6000.get();
        else if (kb <= 7000) this.reduceTicks = tick7000.get();
        else if (kb <= 8000) this.reduceTicks = tick8000.get();
        else if (kb <= 9000) this.reduceTicks = tick9000.get();
        else this.reduceTicks = tick10000.get();
    }

    public void update() {
        if (reduceTicks <= 0) return;

        reduceTicks--;

        KillAura killAura = FrostCore.getInstance().getModuleManager().getModule(KillAura.class);
        if (killAura == null || !killAura.isEnabled()) return;

        Entity target = killAura.getTarget();
        if (target == null) return;

        EntityPlayerSP player = mc.thePlayer;

        Entity rawEntity = (Entity) player;
        if (!rawEntity.isSprinting()) return;
        if (!isMoving(rawEntity)) return;
        if (target == player) return;
        if (badPackets()) return;

        FrostCore.getHelpers().getPacketManager().sendPacket(new C0APacketAnimation(), true);
        FrostCore.getHelpers().getPacketManager().sendPacket(
                new C02PacketUseEntity((Entity) target, C02PacketUseEntity.Action.ATTACK), true
        );

        double mx = player.motionX * 0.6;
        double my = player.motionY;
        double mz = player.motionZ * 0.6;
        player.setVelocity(mx, my, mz);
        player.setSprinting(false);
    }

    public void processOutgoingPacket(EventPacket event) {
        Object packet = event.getPacket();

        if (packet instanceof C09PacketHeldItemChange) {
            slot = true;
        } else if (packet instanceof C0APacketAnimation) {
            swing = true;
        } else if (packet instanceof C02PacketUseEntity) {
            if (((C02PacketUseEntity) packet).getAction() == C02PacketUseEntity.Action.ATTACK) {
                attack = true;
            }
        } else if (packet instanceof C08PacketPlayerBlockPlacement) {
            block = true;
        } else if (packet instanceof C07PacketPlayerDigging) {
            block = true;
            dig = true;
        } else if (packet instanceof C0DPacketCloseWindow ||
                packet instanceof C0EPacketClickWindow ||
                (packet instanceof C16PacketClientStatus &&
                        ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
            inventory = true;
        } else if (packet instanceof C03PacketPlayer) {
            resetBadPackets();
        }
    }

    private boolean badPackets() {
        return badPackets(false, false, false, false, false, false);
    }

    private boolean badPackets(boolean p1, boolean p2, boolean p3, boolean p4, boolean p5, boolean p6) {
        if (slot && !p1) return true;
        if (attack && !p2) return true;
        if (swing && !p3) return true;
        if (block && !p4) return true;
        if (inventory && !p5) return true;
        if (dig && !p6) return true;
        return false;
    }

    private void resetBadPackets() {
        slot = false;
        swing = false;
        attack = false;
        block = false;
        inventory = false;
        dig = false;
    }

    private static boolean isMoving(Entity player) {
        if (player instanceof EntityPlayerSP) {
            EntityPlayerSP sp = (EntityPlayerSP) player;
            return sp.movementInput.moveForward != 0 || sp.movementInput.moveStrafe != 0;
        }
        return false;
    }

    @Override
    public Boolean get() {
        return !parent.cancel.get();
    }
}
