package pub.frost.utils.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class BlockPlacementInfo {
    private final BlockPosition blockToUse;
    private final EnumDirection faceToUse;
}
