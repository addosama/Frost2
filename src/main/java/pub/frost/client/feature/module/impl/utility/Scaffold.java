package pub.frost.client.feature.module.impl.utility;

import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventPreTickLoop;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.BlockUtils;
import pub.frost.utils.BoundingBoxUtils;
import pub.frost.utils.PathfindingUtils;
import pub.frost.utils.data.*;

import java.util.*;
import java.util.stream.Collectors;

@Module(
        key = "Scaffold",
        category = ModuleCategory.UTILITY
)
public class Scaffold extends AbstractModule {
    @Property("SearchRange")
    public final IntegerProperty searchRange = new IntegerProperty(1, 4, 1, 3);

    private final List<EnumDirection> availableFaceList = Arrays.asList(
            EnumDirection.NORTH, EnumDirection.EAST, EnumDirection.SOUTH, EnumDirection.WEST,
            EnumDirection.UP
    );

    private boolean place = false;
    private final Deque<BlockPlacementInfo> placeDeque = new ArrayDeque<>();
    private Rotation lastProvidedRotation = null;

    @EventHandler
    private void onPreTickLoop(EventPreTickLoop event) {
        final Object player = Minecraft.getPlayer(mc), world = Minecraft.getWorld(mc);
        if (player == null || world == null) {
            this.setEnabled(false);
            return;
        }

        place = false;

        Vector3dc playerPos = Entity.getPositionVector(player);
        BlockPosition playerStandingBlock = new BlockPosition(playerPos.floor(new Vector3d())).offset(EnumDirection.DOWN);
        if (!placeDeque.isEmpty()) {
            BlockPlacementInfo data = placeDeque.peekLast();
            if (new BlockPosition(data.getBlockToUse()).offset(data.getFaceToUse()).equals(playerStandingBlock)) return;
        }

        final int searchRange = this.searchRange.get();
        List<BlockPosition> usableBlocks = BlockUtils.getBlocksInRange(playerStandingBlock, searchRange, world)
                .stream()
                .sorted(Comparator.comparingDouble(playerStandingBlock::distance))
                .collect(Collectors.toList());

        placeDeque.clear();
        placeDeque.addAll(PathfindingUtils.placePathToBlock(
                usableBlocks, playerStandingBlock,
                availableFaceList, pos -> World.isAirBlock(world, pos)
        ));
    }

    @EventHandler(priority = 40)
    private void onRotation(EventRotation event) {
//        if (lastProvidedRotation != null) {
//            event.setYaw(lastProvidedRotation.getYaw());
//            event.setPitch(lastProvidedRotation.getPitch());
//            event.setSpeed(180);
//        }
    }

    @EventHandler(priority = 40)
    private void onProcessInteract(EventPreProcessInteract event) {
        while (!placeDeque.isEmpty()) {
            net.minecraft.item.ItemStack stack = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem();
            if (stack == null) return;
            if (!(stack.getItem() instanceof ItemBlock)) return;

            BlockPlacementInfo data = placeDeque.poll();
            if (data == null) return;
            Vector3dc hitVec = BoundingBoxUtils.getFaceCenter(
                    new BoundingBox(
                            data.getBlockToUse().x, data.getBlockToUse().y, data.getBlockToUse().z,
                            data.getBlockToUse().x + 1, data.getBlockToUse().y + 1, data.getBlockToUse().z + 1
                    ),
                    data.getFaceToUse()
            );
            FrostCore.getHelpers().getPacketManager().sendPacket(
                    new C08PacketPlayerBlockPlacement(
                            new BlockPos(data.getBlockToUse().x, data.getBlockToUse().y, data.getBlockToUse().z),
                            data.getFaceToUse().getIndex(),
                            stack,
                            (float) hitVec.x(), (float) hitVec.y(), (float) hitVec.z()
                    ),
                    true
            );
        }


    }
}
