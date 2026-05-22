package pub.frost.client.feature.module.impl.utility;

import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventPreTickLoop;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.client.feature.helper.player.rotation.providers.impl.BasicRotationProvider;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.preset.RotationSetting;
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
    @Property("MaxPlacePerTick")
    public final IntegerProperty maxPlacePerTick = new IntegerProperty(1, 10, 1, 1);
    @Property("KeepY")
    public final BooleanProperty keepY = new BooleanProperty(false);
    @InsertProperty
    public final RotationSetting rotationSetting = new RotationSetting();
    @Property("SnapRotation")
    public final BooleanProperty snapRotation = new BooleanProperty(false);

    private final BasicRotationProvider rotationProvider = new BasicRotationProvider();

    private final List<EnumDirection> availableFaceList = Arrays.asList(
            EnumDirection.NORTH, EnumDirection.EAST, EnumDirection.SOUTH, EnumDirection.WEST,
            EnumDirection.UP
    );
    private final Deque<BlockPlacementInfo> placeDeque = new ArrayDeque<>();

    private int lastOnGroundY;
    private Rotation lastProvidedRotation = null;

    @EventHandler
    private void onPreTickLoop(EventPreTickLoop event) {
        final Object player = Minecraft.getPlayer(mc), world = Minecraft.getWorld(mc);
        if (player == null || world == null) {
            this.setEnabled(false);
            return;
        }

        Vector3dc playerPos = Entity.getPositionVector(player);
        if (Entity.isOnGround(player)) lastOnGroundY = (int) playerPos.y();

        BlockPosition targetBlock = new BlockPosition(
                (int) Math.floor(playerPos.x()),
                keepY.get()? lastOnGroundY : (int) Math.floor(playerPos.y()),
                (int) Math.floor(playerPos.z())
        ).offset(EnumDirection.DOWN);

        if (!placeDeque.isEmpty()) {
            BlockPlacementInfo data = placeDeque.peekLast();
            if (new BlockPosition(data.getBlockToUse()).offset(data.getFaceToUse()).equals(targetBlock)) return;
        }

        final int searchRange = this.searchRange.get();
        List<BlockPosition> usableBlocks = BlockUtils.getBlocksInRange(targetBlock, searchRange, world)
                .stream()
                .sorted(Comparator.comparingDouble(targetBlock::distance))
                .collect(Collectors.toList());

        placeDeque.clear();
        placeDeque.addAll(PathfindingUtils.placePathToBlock(
                usableBlocks, targetBlock,
                availableFaceList, pos -> World.isAirBlock(world, pos) && World.getCollidingBoundingBoxes(
                        world, player,
                        new BoundingBox(
                                pos.x, pos.y, pos.z,
                                pos.x + 1, pos.y + 1, pos.z + 1
                        )
                ).isEmpty()
        ));
    }

    @EventHandler(priority = 40)
    private void onRotation(EventRotation event) {
        Rotation rotation = null;
        if (!placeDeque.isEmpty()) {
            BlockPlacementInfo data = placeDeque.peekFirst();
            if (data == null) return;
            rotation = rotationProvider.getRotation(
                    Entity.getPositionEyes(Minecraft.getPlayer(mc), 1),
                    new BoundingBox(BoundingBoxUtils.getFaceCenter(
                            new BoundingBox(
                                    data.getBlockToUse().x,
                                    data.getBlockToUse().y,
                                    data.getBlockToUse().z,
                                    data.getBlockToUse().x + 1,
                                    data.getBlockToUse().y + 1,
                                    data.getBlockToUse().z + 1
                            ),
                            data.getFaceToUse()
                    ), 0.01, 0.01, 0.01),
                    null, r -> true, true
            );
        } else if (!snapRotation.get()) rotation = lastProvidedRotation;
        if (rotation != null) {
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());
        }
        event.setSpeed(rotationSetting.getSpeed());
        event.setLockView(rotationSetting.isLockViewEnabled());
        event.setProcessors(rotationSetting.getEnabledProcessors());
        lastProvidedRotation = rotation;
    }

    @EventHandler(priority = 40)
    private void onProcessInteract(EventPreProcessInteract event) {
        final int maxPlace = maxPlacePerTick.get();
        int placed = 0;
        while (!placeDeque.isEmpty() && placed < maxPlace) {
            net.minecraft.item.ItemStack stack = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem();
            if (stack == null) return;
            if (!(stack.getItem() instanceof ItemBlock)) return;

            BlockPlacementInfo data = placeDeque.poll();
            if (data == null) return;

            Vector3d faceCenter = BoundingBoxUtils.getFaceCenter(
                    new BoundingBox(
                            data.getBlockToUse().x,
                            data.getBlockToUse().y,
                            data.getBlockToUse().z,
                            data.getBlockToUse().x + 1,
                            data.getBlockToUse().y + 1,
                            data.getBlockToUse().z + 1
                    ),
                    data.getFaceToUse()
            );

            EntityClientPlayer.swingItem(Minecraft.getPlayer(mc));
            net.minecraft.client.Minecraft.getMinecraft().playerController.onPlayerRightClick(
                    net.minecraft.client.Minecraft.getMinecraft().thePlayer,
                    net.minecraft.client.Minecraft.getMinecraft().theWorld,
                    stack,
                    new BlockPos(data.getBlockToUse().x, data.getBlockToUse().y, data.getBlockToUse().z),
                    EnumFacing.VALUES[data.getFaceToUse().getIndex()],
                    new Vec3(faceCenter.x, faceCenter.y, faceCenter.z)
            );
            placed++;
        }
    }
}
