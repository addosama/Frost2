package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventTestPlayerLookingEntity;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.utils.BoundingBoxUtils;
import pub.frost.utils.EntityUtils;

@Module(
        key = "Hitbox",
        category = ModuleCategory.COMBAT
)
public class Hitbox extends AbstractModule {
    @Property("Mode")
    public final ModeProperty<Mode> mode = new ModeProperty<>(Mode.LEGIT);
    @Property("ExpandSize")
    public final FloatProperty expandSize = new FloatProperty(0, 1, 0.01f, 0.1f)
            .setVisibilitySupplier(() -> mode.is(Mode.VANILLA));

    @EventHandler
    private void onTestPlayerLookingEntity(EventTestPlayerLookingEntity event) {
        switch (mode.get()) {
            case VANILLA: {
                double expandSize = this.expandSize.get();
                event.setHitbox(event.getHitbox().expand(expandSize, expandSize, expandSize));
                break;
            }
            case LEGIT: {
                Entity sourceEntity = event.getSourceEntity() == null ? mc.thePlayer : event.getSourceEntity();
                Vec3 lookingVec = event.getLookingVec() == null
                        ? sourceEntity.getLook(event.getTickDelta())
                        : event.getLookingVec();
                double reachDistance = Double.isNaN(event.getReachDistance()) ? 3.0D : event.getReachDistance();

                event.setHitResult(BoundingBoxUtils.getAreaHitResult(
                        BoundingBoxUtils.lerp(EntityUtils.getPrevBoundingBox(event.getEntity()), event.getEntity().getEntityBoundingBox(), 0),
                        BoundingBoxUtils.lerp(EntityUtils.getPrevBoundingBox(event.getEntity()), event.getEntity().getEntityBoundingBox(), 1),
                        sourceEntity,
                        lookingVec,
                        reachDistance,
                        event.getTickDelta()
                ));
            }
        }
    }

    @RequiredArgsConstructor
    @TranslationKey("strings.enum.hitbox.mode.~")
    public enum Mode implements Named {
        VANILLA("Vanilla"),
        LEGIT("Legit");

        final String key;
        @Override public String toString() {
            return key;
        }
    }
}
