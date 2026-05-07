package pub.frost.utils;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
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

    public static boolean isInteractable(Block block) {
        if (block instanceof BlockContainer) return true;
        if (block instanceof BlockWorkbench) return true;
        if (block instanceof BlockAnvil) return true;
        if (block instanceof BlockBed) return true;
        if (block instanceof BlockDoor) {
            if (block.getMaterial() != net.minecraft.block.material.Material.iron) return true;
        }
        if (block instanceof BlockTrapDoor) return true;
        if (block instanceof BlockFenceGate) return true;
        if (block instanceof BlockFence) return true;
        if (block instanceof BlockButton) return true;
        if (block instanceof BlockLever) return true;
        return block instanceof BlockJukebox;
    }

    public static boolean isSolid(Block block) {
        if (block instanceof BlockStairs) return false;
        if (block instanceof BlockSlab) return false;
        if (block instanceof BlockEndPortalFrame) return false;
        if (block instanceof BlockEndPortal) return false;
        if (block instanceof BlockVine) return false;
        if (block instanceof BlockPumpkin) return false;
        if (block instanceof BlockCactus) return false;
        if (block instanceof BlockBush) return false;
        if (block instanceof BlockFalling) return false;
        if (block instanceof BlockWeb) return false;
        if (block instanceof BlockPane) return false;
        if (block instanceof BlockCarpet) return false;
        if (block instanceof BlockSnow) return false;
        if (block instanceof BlockFence) return false;
        if (block instanceof BlockFenceGate) return false;
        if (block instanceof BlockWall) return false;
        if (block instanceof BlockLadder) return false;
        if (block instanceof BlockTorch) return false;
        if (block instanceof BlockRedstoneWire) return false;
        if (block instanceof BlockRedstoneDiode) return false;
        if (block instanceof BlockBasePressurePlate) return false;
        if (block instanceof BlockTripWire) return false;
        if (block instanceof BlockTripWireHook) return false;
        if (block instanceof BlockRailBase) return false;
        if (block instanceof BlockSlime) return false;
        return !(block instanceof BlockTNT);
    }

    public static boolean isReplaceable(BlockPos pos) {
        return net.minecraft.client.Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock().getMaterial().isReplaceable();
    }

    public static boolean isInteractable(BlockPos pos) {
        return isInteractable(net.minecraft.client.Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock());
    }
}
