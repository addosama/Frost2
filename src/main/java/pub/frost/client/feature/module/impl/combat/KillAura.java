package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.utils.data.raytrace.impl.EntityHitResult;
import pub.frost.utils.targeting.EnumEntityTarget;
import pub.frost.utils.targeting.EnumEntityTargetPriority;
import pub.frost.wrappers.ClassEnum;
import pub.frost.wrappers.shared.entity.EnumEntity;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module(
        key = "KillAura",
        category = ModuleCategory.COMBAT
)
public class KillAura extends AbstractModule {
    @Property("mode")
    public final ModeProperty<Mode> mode = new ModeProperty<>(Mode.SINGLE);

    @Property("targets")
    public final MultipleBooleanProperty<EnumEntityTarget> targets = new MultipleBooleanProperty<>(EnumEntityTarget.class, EnumEntityTarget.PLAYERS);
    @Property("targetInvisible")
    public final BooleanProperty targetInvisible = new BooleanProperty(true);
    @Property("priority")
    public final ModeProperty<EnumEntityTargetPriority> priority = new ModeProperty<>(EnumEntityTargetPriority.ANGLE);

    @Property("targetRange")
    public final FloatProperty targetRange = new FloatProperty(0, 8, 0.01f, 3.2f);
    @Property("attackRange")
    public final FloatProperty attackRange = new FloatProperty(0, 6, 0.01f, 3f);

    @Property("cps")
    private final IntegerProperty cps = new IntegerProperty(0, 20, 1, 12);

    private WEntityLivingBase target = null;
    private void resetTarget() {
        target = null;
    }
    private boolean rotationProvided = false;

    @EventHandler
    private void onRotation(EventRotation event) {
        rotationProvided = false;
        List<WEntityLivingBase> validTargets = provideValidTargetList();
        if (validTargets.isEmpty()) {
            resetTarget();
            return;
        }
        if (validTargets.size() > 1) sortByPriority(validTargets);

        if (mode.is(Mode.SINGLE)) {
            if (target == null || !validTargets.stream().collect(ArrayList::new,
                    (list, e) -> list.add(e.getWrappedObject()),
                    ArrayList::addAll
            ).contains(target.getWrappedObject())) {
                target = validTargets.get(0);
            }
        } else target = validTargets.get(0);

        if (target != null) {
            Rotation rotation = getRotation();
            if (rotation != null) {
                event.setYaw(rotation.getYaw());
                event.setPitch(rotation.getPitch());
                rotationProvided = true;
            }
        }
    }

    private long lastAttack = 0;
    private int attackCount = 0;
    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (target != null && rotationProvided) {
            int minimumDelay = 1000 / cps.get();
            if (System.currentTimeMillis() > lastAttack + minimumDelay) {
                lastAttack = System.currentTimeMillis();
                attackCount ++;
            }
        }
    }

    @EventHandler
    private void onProcessInteract(EventPreProcessInteract e) {
        while (attackCount > 0) {
            mc.clickLMB();
            attackCount --;
        }
    }

    private List<WEntityLivingBase> provideValidTargetList() {
        List<WEntityLivingBase> list = new ArrayList<>();
        for (WEntity entity : mc.getWorld().getLoadedEntityList()) {
            if (entity.isDead()) continue;
            if (!ClassEnum.isInstanceOf(entity, EnumEntity.EntityLivingBase)) continue;
            if (entity.getWrappedObject() == mc.getPlayer().getWrappedObject()) continue;

            WEntityLivingBase living = entity.castTo(WEntityLivingBase.class);
            if (living.getHealth() <= 0) continue;
            if (living.distanceTo(mc.getPlayer().getPositionVector()) > targetRange.getValue()) continue;

            for (EnumEntityTarget target : targets.getEnabled()) {
                if (target.isTarget(living.getWrappedClass())) {
                    if (targetInvisible.get() || !entity.isInvisible()) {
                        list.add(living);
                        break;
                    }
                }
            }
        }
        return list;
    }
    private void sortByPriority(List<WEntityLivingBase> list) {
        EnumEntityTargetPriority value = priority.getValue();
        Comparator<WEntityLivingBase> comparator = value.getComparator().apply(mc.getPlayer());
        for (EnumEntityTargetPriority p : EnumEntityTargetPriority.values()) {
            if (p == value) continue;
            comparator = comparator.thenComparing(p.getComparator().apply(mc.getPlayer()));
        }
        list.sort(comparator);
    }
    private Rotation getRotation() {
        return RotationUtils.searchRotationHittingBoundingBox(
                mc.getPlayer().getPositionEyes(1),
                target.getBoundingBox(),
                r ->
                        mc.getPlayer().getPositionVector().distance(target.getPositionVector()) > attackRange.get()
                                || rayTraceTarget(r),
                2
        );
    }

    private boolean rayTraceTarget(Rotation r) {
        HitResult result = mc.getPlayer().rayTrace(
                mc.getPlayer().getVectorForRotation(r.getPitch(), r.getYaw()),
                attackRange.get(), 1
        );
        if (result == null) return false;
        if (result.getType() == HitResult.EnumHitType.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) result;
            return entityHit.getHitEntity().getWrappedObject() == target.getWrappedObject();
        }
        return false;
    }

    @Override
    protected void onEnabled() {
        resetTarget();
        attackCount = 0;
        lastAttack = 0;
    }

    @RequiredArgsConstructor
    public enum Mode implements Named {
        SINGLE("single"),
        SWITCH("switch"),;
        final String key;

        @Override
        public String toString() {
            return key;
        }
        @Override
        public String getName() {
            return FrostCore.getLocalizer().get("strings.killaura.modes." + key + ".name");
        }
    }
}
