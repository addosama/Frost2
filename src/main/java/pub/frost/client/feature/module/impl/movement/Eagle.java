package pub.frost.client.feature.module.impl.movement;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.EnumDirection;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.world.WWorld;

@Module(
        key = "eagle",
        category = ModuleCategory.MOVEMENT
)
public class Eagle extends AbstractModule {
    @Property("PitchCheck")
    public final BooleanProperty pitchCheck = new BooleanProperty(true);

    private final WEntity entityWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WEntity.class);
    private final WWorld worldWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WWorld.class);

    private boolean onEdge;

    @EventHandler
    private void onPlayerUpdate(EventPlayerUpdateTick event) {
        if (event.getType() == TickType.PRE) {
            onEdge = worldWrapper.isAirBlock(
                    mcWrapper.getWorld(mc),
                    new BlockPosition(entityWrapper.getPositionVector(mcWrapper.getPlayer(mc))).offset(EnumDirection.DOWN)
            );
        }
    }

    @EventHandler
    private void onMoveInput(EventUpdateMovementInput event) {
        if (!onEdge) return;
        if (!pitchCheck.get() || entityWrapper.getPitch(mcWrapper.getPlayer(mc)) > 75) {
            event.setSneak(true);
        }
    }
}
