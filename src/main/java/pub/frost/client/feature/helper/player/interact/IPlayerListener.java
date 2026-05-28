package pub.frost.client.feature.helper.player.interact;

interface IPlayerListener {
    boolean isDigging();
    boolean isStartDiggingTick();
    boolean isStopDiggingTick();

    boolean isHeldItemChangeTick();
    int getTicksSinceHeldItemChange();
}
