package pub.frost.utils;

import pub.frost.base.wrapping.Wrappers;
import pub.frost.utils.data.BlockPosition;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils implements Wrappers {
    public static List<BlockPosition> getBlocksInRange(BlockPosition centerPos, int radius, Object world) {
        List<BlockPosition> list = new ArrayList<>();

        int posX = centerPos.x;
        int posY = centerPos.y;
        int posZ = centerPos.z;

        for (int x = posX - radius; x <= posX + radius; x++) {
            for (int y = posY - radius; y <= posY + radius; y++) {
                for (int z = posZ - radius; z <= posZ + radius; z++) {
                    BlockPosition pos = new BlockPosition(x, y, z);
                    if (World.isAirBlock(world, pos)) continue;
                    list.add(pos);
                }
            }
        }

        return list;
    }
}
