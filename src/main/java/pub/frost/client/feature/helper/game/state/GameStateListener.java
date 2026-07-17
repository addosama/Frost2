package pub.frost.client.feature.helper.game.state;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class GameStateListener {
    private EnumGameState current = EnumGameState.Unknown;
}
