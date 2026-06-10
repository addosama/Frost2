package pub.frost.utils.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@RequiredArgsConstructor @Getter
public class BlockPlacementInfo {
    private final BlockPos blockToUse;
    private final EnumFacing faceToUse;
}
