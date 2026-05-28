package pub.frost.client.feature.helper.player.interact;

import pub.frost.client.core.FrostCore;

public interface PlayerListenerAccessor extends IPlayerListener {
    default boolean isDigging() {
        return FrostCore.getHelpers().getPlayerListener().isDigging();
    }
    default boolean isStartDiggingTick() {
        return FrostCore.getHelpers().getPlayerListener().isStartDiggingTick();
    }
    default boolean isStopDiggingTick() {
        return FrostCore.getHelpers().getPlayerListener().isStopDiggingTick();
    }

    default boolean isHeldItemChangeTick() {
        return FrostCore.getHelpers().getPlayerListener().isHeldItemChangeTick();
    }
    default int getTicksSinceHeldItemChange() {
        return FrostCore.getHelpers().getPlayerListener().getTicksSinceHeldItemChange();
    }
}
