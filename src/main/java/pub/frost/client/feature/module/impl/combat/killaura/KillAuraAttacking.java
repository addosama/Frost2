package pub.frost.client.feature.module.impl.combat.killaura;

import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.interacting.EnumInteractType;

public class KillAuraAttacking extends AbstractSubModule<KillAura> {
    public KillAuraAttacking(KillAura killAura) {
        super(killAura);
    }

    @PropertyGroupMain
    @Property("Mode")
    public final ModeProperty<EnumInteractType> mode = new ModeProperty<>(EnumInteractType.LEGIT);
    
    @Property("attackRange")
    public final FloatProperty attackRange = new FloatProperty(0, 6, 0.01f, 3f).setVisibilitySupplier(() -> mode.is(EnumInteractType.PACKET));

    @Property("cps")
    public final IntegerProperty cps = new IntegerProperty(0, 20, 1, 12);

    private long lastAttack = 0;
    private int attackCount = 0;
    public void resetCpsLimiter() {
        lastAttack = 0;
        resetAttackCount();
    }
    public void updateCpsLimiter() {
        int minimumDelay = 1000 / cps.get();
        if (System.currentTimeMillis() > lastAttack + minimumDelay) {
            lastAttack = System.currentTimeMillis();
            attackCount ++;
        }
    }
    public void resetAttackCount() {
        attackCount = 0;
    }
    
    public void doAttack(Object target) {
        while (attackCount > 0) {
            attack(target);
        }
    }
    private void attack(Object target) {
        if (mode.is(EnumInteractType.LEGIT)) Minecraft.clickLMB(mc);
        else {
            Object player = Minecraft.getPlayer(mc);
            EntityLivingBase.swingItem(player);


            boolean rayCastResult = parent.rayTraceTarget(
                    new Rotation(
                            Entity.getYaw(player),
                            Entity.getPitch(player)
                    ),
                    getRealAttackRange()
            );

            if (rayCastResult) {
                PlayerControllerMP.attackEntity(
                        Minecraft.getPlayerController(mc),
                        player, target
                );
            }
        }
        attackCount --;
    }

    public double getRealAttackRange() {
        if (mode.is(EnumInteractType.LEGIT)) return 3;
        return attackRange.get();
    }

}
