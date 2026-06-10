package pub.frost.utils;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import pub.frost.utils.data.BlockPlacementInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

public class PathfindingUtils {
    public static Queue<BlockPlacementInfo> placePathToBlock(
            List<BlockPos> usableBlocks, BlockPos targetBlock,
            List<EnumFacing> allowedDirections,
            Predicate<BlockPos> blockAvailablePredicate
    ) {
        Queue<BlockPlacementInfo> emptyResult = new ArrayDeque<>();
        if (usableBlocks == null || usableBlocks.isEmpty()
                || targetBlock == null
                || allowedDirections == null
                || blockAvailablePredicate == null) {
            return emptyResult;
        }

        List<BlockPos> sortedUsableBlocks = new ArrayList<>(usableBlocks.size());
        for (BlockPos blockPos : usableBlocks) {
            if (blockPos != null) sortedUsableBlocks.add(new BlockPos(blockPos));
        }
        sortedUsableBlocks.sort(Comparator.comparingDouble(targetBlock::distanceSq));

        if (sortedUsableBlocks.stream().anyMatch(targetBlock::equals)) {
            return emptyResult;
        }

        int minX = targetBlock.getX();
        int minY = targetBlock.getY();
        int minZ = targetBlock.getZ();
        int maxX = targetBlock.getX();
        int maxY = targetBlock.getY();
        int maxZ = targetBlock.getZ();
        for (BlockPos startBlock : sortedUsableBlocks) {
            minX = Math.min(minX, startBlock.getX());
            minY = Math.min(minY, startBlock.getY());
            minZ = Math.min(minZ, startBlock.getZ());
            maxX = Math.max(maxX, startBlock.getX());
            maxY = Math.max(maxY, startBlock.getY());
            maxZ = Math.max(maxZ, startBlock.getZ());
        }

        final int searchPadding = 1;
        minX -= searchPadding;
        minY -= searchPadding;
        minZ -= searchPadding;
        maxX += searchPadding;
        maxY += searchPadding;
        maxZ += searchPadding;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, BlockPlacementInfo> parentStepMap = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();

        for (BlockPos startBlock : sortedUsableBlocks) {
            if (visited.add(startBlock)) {
                queue.add(startBlock);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos currentBlock = queue.poll();
            BlockPlacementInfo parentStep = parentStepMap.get(currentBlock);

            EnumFacing preferredFace =
                    parentStep != null ? parentStep.getFaceToUse() : null;

            List<EnumFacing> prioritizedDirections =
                    buildPreferredDirections(preferredFace, allowedDirections);

            List<BlockPlacementInfo> nextSteps = getBlockPlacingSolution(
                    currentBlock,
                    prioritizedDirections,
                    blockAvailablePredicate
            );

            for (BlockPlacementInfo nextStep : nextSteps) {
                BlockPos placedBlock = getPlacedBlock(nextStep);
                if (!isWithinSearchBounds(placedBlock, minX, minY, minZ, maxX, maxY, maxZ)) continue;
                if (!visited.add(placedBlock)) continue;

                parentStepMap.put(placedBlock, nextStep);
                if (placedBlock.equals(targetBlock)) {
                    return rebuildPath(parentStepMap, targetBlock);
                }

                queue.add(placedBlock);
            }
        }

        return emptyResult;
    }

    public static List<BlockPlacementInfo> getBlockPlacingSolution(
            BlockPos blockToUse, List<EnumFacing> directionsToAnalyze,
            Predicate<BlockPos> predicate
    ) {
        return directionsToAnalyze.stream().collect(
                ArrayList::new,
                (list, direction) -> {
                    BlockPos placedBlock = new BlockPos(blockToUse).offset(direction);
                    if (predicate.test(placedBlock)) {
                        list.add(new BlockPlacementInfo(new BlockPos(blockToUse), direction));
                    }
                },
                ArrayList::addAll
        );
    }

    private static Queue<BlockPlacementInfo> rebuildPath(
            Map<BlockPos, BlockPlacementInfo> parentStepMap, BlockPos targetBlock
    ) {
        ArrayDeque<BlockPlacementInfo> path = new ArrayDeque<>();
        BlockPos currentBlock = new BlockPos(targetBlock);

        while (true) {
            BlockPlacementInfo step = parentStepMap.get(currentBlock);
            if (step == null) break;

            path.addFirst(step);
            currentBlock = new BlockPos(step.getBlockToUse());
        }

        return path;
    }

    private static List<EnumFacing> buildPreferredDirections(
            EnumFacing preferredFace,
            List<EnumFacing> allowedDirections
    ) {
        if (preferredFace == null) {
            return allowedDirections;
        }

        ArrayList<EnumFacing> result = new ArrayList<>(allowedDirections.size());

        if (allowedDirections.contains(preferredFace)) {
            result.add(preferredFace);
        }

        for (EnumFacing direction : allowedDirections) {
            if (direction != preferredFace) {
                result.add(direction);
            }
        }

        return result;
    }

    private static BlockPos getPlacedBlock(BlockPlacementInfo info) {
        return new BlockPos(info.getBlockToUse()).offset(info.getFaceToUse());
    }

    private static boolean isWithinSearchBounds(
            BlockPos block,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
        return block.getX() >= minX && block.getX() <= maxX
                && block.getY() >= minY && block.getY() <= maxY
                && block.getZ() >= minZ && block.getZ() <= maxZ;
    }
}
