package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRotation;
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
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;
import pub.frost.wrappers.shared.world.WWorld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module(
        key = "KillAura",
        category = ModuleCategory.COMBAT
)
// todo FOV
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

    @Property("RotationSpeed")
    private final IntegerProperty rotationSpeed = new IntegerProperty(0, 180, 1, 180);
    @Property("LockView")
    private final BooleanProperty lockView = new BooleanProperty(true);

    private final WWorld worldWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WWorld.class);
    private final WEntity entityWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WEntity.class);
    private final WEntityLivingBase livingEntityWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WEntityLivingBase.class);

    private Object target = null;
    private void resetTarget() {
        target = null;
    }
    private boolean rotationProvided = false;

    @EventHandler
    private void onRotation(EventRotation event) {
        rotationProvided = false;
        List<Object> validTargets = provideValidTargetList();
        if (validTargets.isEmpty()) {
            resetTarget();
            return;
        }
        if (validTargets.size() > 1) sortByPriority(validTargets);

        if (mode.is(Mode.SINGLE)) {
            if (target == null || !validTargets.stream().collect(ArrayList::new,
                    ArrayList::add,
                    ArrayList::addAll
            ).contains(target)) {
                target = validTargets.get(0);
            }
        } else target = validTargets.get(0);

        if (target != null) {
            Rotation rotation = getRotation();
            if (rotation != null) {
                event.setYaw(rotation.getYaw());
                event.setPitch(rotation.getPitch());
                event.setSpeed(rotationSpeed.get());
                event.setLockView(lockView.get());
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
            mcWrapper.clickLMB(mc);
            attackCount --;
        }
    }

    private List<Object> provideValidTargetList() {
        List<Object> list = new ArrayList<>();
        for (Object entity : worldWrapper.getLoadedEntityList(mcWrapper.getWorld(mc))) {
            if (entityWrapper.isDead(entity)) continue;
            if (!livingEntityWrapper.isTarget(entity.getClass())) continue;
            if (entity == mcWrapper.getPlayer(mc)) continue;

            if (livingEntityWrapper.getHealth(entity) <= 0) continue;
            if (livingEntityWrapper.distanceTo(entity, entityWrapper.getPositionVector(mcWrapper.getPlayer(mc))) > targetRange.getValue()) continue;

            for (EnumEntityTarget target : targets.getEnabled()) {
                if (target.isTarget(entity.getClass())) {
                    if (targetInvisible.get() || !entityWrapper.isInvisible(entity)) {
                        list.add(entity);
                        break;
                    }
                }
            }
        }
        return list;
    }
    private void sortByPriority(List<Object> list) {
        EnumEntityTargetPriority value = priority.getValue();
        Comparator<Object> comparator = value.getComparator().apply(mcWrapper.getPlayer(mc));
        for (EnumEntityTargetPriority p : EnumEntityTargetPriority.values()) {
            if (p == value) continue;
            comparator = comparator.thenComparing(p.getComparator().apply(mcWrapper.getPlayer(mc)));
        }
        list.sort(comparator);
    }
    private Rotation getRotation() {
        return RotationUtils.searchRotationHittingBoundingBox(
                entityWrapper.getPositionEyes(mcWrapper.getPlayer(mc), 1),
                entityWrapper.getBoundingBox(target),
                r ->
                        entityWrapper.getPositionVector(mcWrapper.getPlayer(mc)).distance(entityWrapper.getPositionVector(target)) > attackRange.get()
                                || rayTraceTarget(r),
                2
        );
    }

    private boolean rayTraceTarget(Rotation r) {
        HitResult result = entityWrapper.rayTrace(
                mcWrapper.getPlayer(mc),
                RotationUtils.getVectorForRotation(r.getPitch(), r.getYaw()),
                attackRange.get(), 1
        );
        if (result == null) return false;
        if (result.getType() == HitResult.EnumHitType.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) result;
            return entityHit.getHitEntity() == target;
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
