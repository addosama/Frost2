package pub.frost.client.core;

import lombok.Getter;
import pub.frost.client.feature.helper.game.state.GameStateListener;
import pub.frost.client.feature.helper.network.lag.LagManager;
import pub.frost.client.feature.helper.network.PacketManager;
import pub.frost.client.feature.helper.player.interact.PlayerListener;
import pub.frost.client.feature.helper.player.rotation.RotationManager;

@Getter
public final class ClientHelpers implements Initializer {
    final GameStateListener gameStateListener;
    final RotationManager rotationManager;
    final PlayerListener playerListener;
    final LagManager lagManager;
    final PacketManager packetManager;

    public ClientHelpers() {
        this.gameStateListener = new GameStateListener();
        this.rotationManager = registerToEventBus(new RotationManager());
        this.playerListener = registerToEventBus(new PlayerListener());
        this.lagManager = registerToEventBus(new LagManager());
        this.packetManager = registerToEventBus(new PacketManager());
    }
}
