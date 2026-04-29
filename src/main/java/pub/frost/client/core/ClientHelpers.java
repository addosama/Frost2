package pub.frost.client.core;

import lombok.Getter;
import pub.frost.client.feature.helper.network.LagManager;
import pub.frost.client.feature.helper.network.PacketManager;
import pub.frost.client.feature.helper.player.interact.PlayerListener;
import pub.frost.client.feature.helper.player.rotation.RotationManager;

@Getter
public final class ClientHelpers implements Initializer {
    final RotationManager rotationManager;
    final PlayerListener playerListener;
    final LagManager lagManager;
    final PacketManager packetManager;

    public ClientHelpers() {
        this.rotationManager = registerToEventBus(new RotationManager());
        this.playerListener = registerToEventBus(new PlayerListener());
        this.lagManager = registerToEventBus(new LagManager());
        this.packetManager = registerToEventBus(new PacketManager());
    }
}
