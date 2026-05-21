package pub.frost.utils;

import pub.frost.utils.data.BlockPlacementInfo;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.EnumDirection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

public class PathfindingUtils {
    public static Queue<BlockPlacementInfo> placePathToBlock(
            List<BlockPosition> usableBlocks, BlockPosition targetBlock,
            List<EnumDirection> allowedDirections,
            Predicate<BlockPosition> blockAvailablePredicate
    ) {
        Queue<BlockPlacementInfo> emptyResult = new ArrayDeque<>();
        if (usableBlocks == null || usableBlocks.isEmpty()
                || targetBlock == null
                || allowedDirections == null
                || blockAvailablePredicate == null) {
            return emptyResult;
        }

        List<BlockPosition> sortedUsableBlocks = new ArrayList<>(usableBlocks.size());
        for (BlockPosition blockPos : usableBlocks) {
            if (blockPos != null) sortedUsableBlocks.add(new BlockPosition(blockPos));
        }
        sortedUsableBlocks.sort(Comparator.comparingDouble(targetBlock::distance));

        if (sortedUsableBlocks.stream().anyMatch(targetBlock::equals)) {
            return emptyResult;
        }

        int minX = targetBlock.x;
        int minY = targetBlock.y;
        int minZ = targetBlock.z;
        int maxX = targetBlock.x;
        int maxY = targetBlock.y;
        int maxZ = targetBlock.z;
        for (BlockPosition startBlock : sortedUsableBlocks) {
            minX = Math.min(minX, startBlock.x);
            minY = Math.min(minY, startBlock.y);
            minZ = Math.min(minZ, startBlock.z);
            maxX = Math.max(maxX, startBlock.x);
            maxY = Math.max(maxY, startBlock.y);
            maxZ = Math.max(maxZ, startBlock.z);
        }

        final int searchPadding = 1;
        minX -= searchPadding;
        minY -= searchPadding;
        minZ -= searchPadding;
        maxX += searchPadding;
        maxY += searchPadding;
        maxZ += searchPadding;

        ArrayDeque<BlockPosition> queue = new ArrayDeque<>();
        Map<BlockPosition, BlockPlacementInfo> parentStepMap = new HashMap<>();
        Set<BlockPosition> visited = new HashSet<>();

        for (BlockPosition startBlock : sortedUsableBlocks) {
            if (visited.add(startBlock)) {
                queue.add(startBlock);
            }
        }

        while (!queue.isEmpty()) {
            BlockPosition currentBlock = queue.poll();
            List<BlockPlacementInfo> nextSteps = getBlockPlacingSolution(
                    currentBlock, allowedDirections, blockAvailablePredicate
            );

            for (BlockPlacementInfo nextStep : nextSteps) {
                BlockPosition placedBlock = getPlacedBlock(nextStep);
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
            BlockPosition blockToUse, List<EnumDirection> directionsToAnalyze,
            Predicate<BlockPosition> predicate
    ) {
        return directionsToAnalyze.stream().collect(
                ArrayList::new,
                (list, direction) -> {
                    BlockPosition placedBlock = new BlockPosition(blockToUse).offset(direction);
                    if (predicate.test(placedBlock)) {
                        list.add(new BlockPlacementInfo(new BlockPosition(blockToUse), direction));
                    }
                },
                ArrayList::addAll
        );
    }

    private static Queue<BlockPlacementInfo> rebuildPath(
            Map<BlockPosition, BlockPlacementInfo> parentStepMap, BlockPosition targetBlock
    ) {
        ArrayDeque<BlockPlacementInfo> path = new ArrayDeque<>();
        BlockPosition currentBlock = new BlockPosition(targetBlock);

        while (true) {
            BlockPlacementInfo step = parentStepMap.get(currentBlock);
            if (step == null) break;

            path.addFirst(step);
            currentBlock = new BlockPosition(step.getBlockToUse());
        }

        return path;
    }

    private static BlockPosition getPlacedBlock(BlockPlacementInfo info) {
        return new BlockPosition(info.getBlockToUse()).offset(info.getFaceToUse());
    }

    private static boolean isWithinSearchBounds(
            BlockPosition block,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
        return block.x >= minX && block.x <= maxX
                && block.y >= minY && block.y <= maxY
                && block.z >= minZ && block.z <= maxZ;
    }
}
