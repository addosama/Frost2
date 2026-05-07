package pub.frost.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class MoveUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int getForwardValue() {
        int forwardValue = 0;
        if (mc.gameSettings.keyBindForward.isKeyDown()) {
            ++forwardValue;
        }
        if (mc.gameSettings.keyBindBack.isKeyDown()) {
            --forwardValue;
        }
        return forwardValue;
    }

    public static int getStrafeValue() {
        int strafeValue = 0;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            ++strafeValue;
        }
        if (mc.gameSettings.keyBindRight.isKeyDown()) {
            --strafeValue;
        }
        return strafeValue;
    }

    public static float getAllowedHorizontalDistance() {
        float slipperiness = mc.thePlayer.worldObj.getBlockState(
                new net.minecraft.util.BlockPos(
                        MathHelper.floor_double(mc.thePlayer.posX),
                        MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1,
                        MathHelper.floor_double(mc.thePlayer.posZ)
                )
        ).getBlock().slipperiness * 0.91f;
        return mc.thePlayer.getAIMoveSpeed() * (0.16277136f / (slipperiness * slipperiness * slipperiness));
    }

    public static double[] predictMovement() {
        float strafeInput = (float) getStrafeValue() * 0.98f;
        float forwardInput = (float) getForwardValue() * 0.98f;
        float inputMagnitude = strafeInput * strafeInput + forwardInput * forwardInput;
        if (inputMagnitude >= 1.0E-4f) {
            inputMagnitude = MathHelper.sqrt_float(inputMagnitude);
            if (inputMagnitude < 1.0f) {
                inputMagnitude = 1.0f;
            }
            inputMagnitude = getAllowedHorizontalDistance() / inputMagnitude;
            float sinYaw = MathHelper.sin(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0f);
            float cosYaw = MathHelper.cos(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0f);
            strafeInput *= inputMagnitude;
            forwardInput *= inputMagnitude;
            return new double[]{strafeInput * cosYaw - forwardInput * sinYaw, forwardInput * cosYaw + strafeInput * sinYaw};
        }
        return new double[]{0.0, 0.0};
    }
}
