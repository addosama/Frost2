package pub.frost.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class PlayerUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean canMove(double x, double z) {
        return canMove(x, z, -1.0);
    }

    public static boolean canMove(double x, double z, double y) {
        AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(x, y, z);
        return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty();
    }

    public static boolean isAirAbove() {
        for (int y = (int) Math.ceil(mc.thePlayer.posY); y <= (int) Math.ceil(mc.thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
            if (!mc.theWorld.isAirBlock(new net.minecraft.util.BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAirBelow() {
        BlockPos pos = new BlockPos(
                mc.thePlayer.posX,
                mc.thePlayer.getEntityBoundingBox().minY - 0.05,
                mc.thePlayer.posZ
        );
        return mc.theWorld.isAirBlock(pos);
    }

    public static boolean isUsingItem() {
        return mc.thePlayer.isUsingItem();
    }
}
