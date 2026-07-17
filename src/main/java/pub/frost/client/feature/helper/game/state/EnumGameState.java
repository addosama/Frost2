package pub.frost.client.feature.helper.game.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumGameState {
    Unknown(-1),
    PreTickLoop(10),
    PreGameTick(20),
    PostGameTick(30),
//    PostTickLoop(40),
    PreRender(50),
    PostRender(60);

    final int index;
}
