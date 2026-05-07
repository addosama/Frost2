package pub.frost.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;

public class PlayerUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean canMove(double x, double z) {
        return canMove(x, z, -1.0);
    }

    public static boolean canMove(double x, double z, double y) {
        AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(x, y, z);
        return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty();
    }
}
