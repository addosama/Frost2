package pub.frost.client.feature.module.impl.utility;

import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.helper.player.rotation.providers.impl.BasicRotationProvider;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.preset.legacy.RotationSetting;
import pub.frost.utils.BlockUtils;
import pub.frost.utils.BoundingBoxUtils;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.PathfindingUtils;
import pub.frost.utils.data.*;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.utils.raycast.EnumRaycastType;
import pub.frost.utils.raycast.RayCastUtils;

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

    @Property("ForceUp")
    public final BooleanProperty forceUp = new BooleanProperty(false);
    @Property("ForceUpAfterBlocks")
    public final IntegerProperty forceUpAfterBlocks = new IntegerProperty(1, 10, 1, 1)
            .setVisibilitySupplier(forceUp::get);

    @Property("Telly")
    public final BooleanProperty telly = new BooleanProperty(false);
    @Property("AirTicks")
    public final IntegerProperty airTicks = new IntegerProperty(1, 7, 1, 3)
            .setVisibilitySupplier(telly::get);
    @Property("DisableUpTelly")
    public final BooleanProperty disableUpTelly = new BooleanProperty(false)
            .setVisibilitySupplier(telly::get);

    @Property("RandomizeHitPoint")
    public final BooleanProperty randomizeHitPoint = new BooleanProperty(true);

    @Property("SlowRotationAfterPlace")
    public final BooleanProperty slowRotationAfterPlace = new BooleanProperty(true);
    @Property("SlowTicks")
    public final IntegerProperty slowTicks = new IntegerProperty(1, 10, 1, 2);
    @Property("SlowRotationSpeed")
    public final IntegerProperty slowRotationSpeed = new IntegerProperty(1, 180, 1, 15)
            .setVisibilitySupplier(slowRotationAfterPlace::get);
    @InsertProperty
    public final RotationSetting rotationSetting = new RotationSetting();
    @Property("SnapRotation")
    public final BooleanProperty snapRotation = new BooleanProperty(false);
    @Property("RayCast")
    public final ModeProperty<EnumRaycastType> rayCast = new ModeProperty<>(EnumRaycastType.DEFAULT);

    private final BasicRotationProvider rotationProvider = new BasicRotationProvider();

    private final List<EnumDirection> availableFaceList = Arrays.asList(
            EnumDirection.NORTH, EnumDirection.EAST, EnumDirection.SOUTH, EnumDirection.WEST,
            EnumDirection.UP
    );
    private final Deque<BlockPlacementInfo> placeDeque = new ArrayDeque<>();

    private int lastOnGroundY;

    private int ticksSincePlace;
    private int ticksSinceJump;

    private int blocksPlacedSinceJump;

    private boolean upTelly;

    private Rotation lastProvidedRotation = null;

    @Override
    protected void onEnabled() {
        lastProvidedRotation = null;
        blocksPlacedSinceJump = 0;
        ticksSincePlace = -1;
        ticksSinceJump = -1;
    }

    @EventHandler
    private void onPreTickLoop(EventPreTickLoop event) {
        final Object player = Minecraft.getPlayer(mc), world = Minecraft.getWorld(mc);
        if (player == null || world == null) {
            this.setEnabled(false);
            return;
        }

        Vector3dc playerPos = Entity.getPositionVector(player);
        if (Entity.isOnGround(player)) {
            lastOnGroundY = (int) playerPos.y();
            ticksSinceJump = -1;
        }

        if (telly.get()) {
            if (!upTelly || !disableUpTelly.get()) {
                if (ticksSinceJump > 0 && ticksSinceJump <= airTicks.get()) {
                    placeDeque.clear();
                    return;
                }
            }
        }

        BlockPosition targetBlock = new BlockPosition(
                (int) Math.floor(playerPos.x()),
                (shouldKeepY() || (telly.get() && !upTelly && !shouldForceJump()))? lastOnGroundY : (int) Math.floor(playerPos.y()),
                (int) Math.floor(playerPos.z())
        ).offset(EnumDirection.DOWN);

//        if (!placeDeque.isEmpty()) {
//            BlockPlacementInfo data = placeDeque.peekLast();
//            if (new BlockPosition(data.getBlockToUse()).offset(data.getFaceToUse()).equals(targetBlock)) return;
//        }

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
        boolean telly = this.telly.get();
        int airTicks = this.airTicks.get();
        boolean tellyFlag = telly && !upTelly && ticksSinceJump > 0 && ticksSinceJump <= airTicks;
        if (!tellyFlag) {
            if (!placeDeque.isEmpty()) {
                BlockPlacementInfo data = placeDeque.peekFirst();
                if (data == null) return;
                Vector3d eyePos = Entity.getPositionEyes(Minecraft.getPlayer(mc), 1);
                BoundingBox blockBB = getBlockBoundingBox(data.getBlockToUse());
                BoundingBox faceBB = BoundingBoxUtils.getFaceBoundingBox(
                        blockBB,
                        data.getFaceToUse()
                );
                if (
                        lastProvidedRotation != null
                        && RayCastUtils.getSimpleHitResult(
                                eyePos,
                                lastProvidedRotation.getYaw(), lastProvidedRotation.getPitch(),
                                faceBB
                        ).getKey()
                ) {
                    rotation = lastProvidedRotation;
                }
                else rotation = rotationProvider.getRotation(
                        eyePos,
                        faceBB,
                        getHitPoint(faceBB, data.getFaceToUse()),
                        r -> true, true
                );
            }
            else if (!snapRotation.get() && (!telly || ticksSinceJump > airTicks)) rotation = lastProvidedRotation;
        }
        float rotationSpeed = rotationSetting.getSpeed();
        if (slowRotationAfterPlace.get() && ticksSincePlace > 0 && ticksSincePlace <= slowTicks.get()) rotationSpeed = slowRotationSpeed.get();
        if (rotation != null) {
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());
        }
        event.setSpeed(rotationSpeed);
        event.setLockView(rotationSetting.isLockViewEnabled());
        event.setProcessors(rotationSetting.getEnabledProcessors());
        lastProvidedRotation = rotation;
    }

    @EventHandler(priority = 40)
    private void onProcessInteract(EventPreProcessInteract event) {
        Object player = Minecraft.getPlayer(mc);
        net.minecraft.item.ItemStack stack = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem();
        if (stack == null) return;
        if (!(stack.getItem() instanceof ItemBlock)) return;

        final int maxPlace = maxPlacePerTick.get();
        int placed = 0;
        while (!placeDeque.isEmpty() && placed < maxPlace) {
            placed++;
            BlockPlacementInfo data = placeDeque.peek();
            if (data == null) continue;
            if (placed == 0 && World.isAirBlock(Minecraft.getWorld(mc), data.getBlockToUse())) continue;

            BoundingBox blockBB = getBlockBoundingBox(data.getBlockToUse());
            Vector3d hitVec = BoundingBoxUtils.getFaceCenter(
                    blockBB,
                    data.getFaceToUse()
            );

            if (rayCast.get() != EnumRaycastType.DISABLED) {
                if (rayCast.is(EnumRaycastType.LEGIT)) {
                    HitResult result = EntityUtils.getLookingObject(
                            player,
                            Entity.getLook(player, 1),
                            3, 1
                    );
                    if (
                            result.getType() == HitResult.EnumHitType.BLOCK && data.getBlockToUse().equals(result.getBlockPos())
                    ) {
                        hitVec = result.getHitVec();
                    } else continue;
                } else {
                    Map.Entry<Boolean, Vector3d> result = RayCastUtils.getSimpleHitResult(
                            Entity.getPositionEyes(player, 1),
                            Entity.getYaw(player),
                            Entity.getPitch(player),
                            BoundingBoxUtils.getFaceBoundingBox(
                                    blockBB, data.getFaceToUse()
                            )
                    );
                    if (!result.getKey()) continue;
                    hitVec = result.getValue();
                }
            }

            if (net.minecraft.client.Minecraft.getMinecraft().playerController.onPlayerRightClick(
                    net.minecraft.client.Minecraft.getMinecraft().thePlayer,
                    net.minecraft.client.Minecraft.getMinecraft().theWorld,
                    stack,
                    new BlockPos(data.getBlockToUse().x, data.getBlockToUse().y, data.getBlockToUse().z),
                    EnumFacing.VALUES[data.getFaceToUse().getIndex()],
                    new Vec3(hitVec.x, hitVec.y, hitVec.z)
            )) EntityClientPlayer.swingItem(player);
            placeDeque.poll();
            ticksSincePlace = 0;
            if (data.getBlockToUse().y() <= lastOnGroundY) {
                blocksPlacedSinceJump++;
            }
        }
    }

    @EventHandler
    private void onPostTick(EventGameTick event) {
        if (event.getType() == TickType.POST) {
            if (ticksSinceJump >= 0) ticksSinceJump++;
            if (ticksSincePlace >= 0) ticksSincePlace++;
        }
    }

    @EventHandler(priority = 8)
    private void onMovementInput(EventUpdateMovementInput event) {
        upTelly = false;
        boolean onGround = Entity.isOnGround(Minecraft.getPlayer(mc));
        if (onGround) {
            if (forceUp.get()) {
                if (shouldForceJump()) {
                    event.setJump(true);
                }
            }
            if (telly.get()) {
                if (Math.max(Math.abs(event.getMoveForward()), Math.abs(event.getMoveStrafe())) > 0.01) {
                    event.setJump(true);
                }
            }
        }

        if (event.isJump()) {
            blocksPlacedSinceJump = 0;
            if (!onGround) upTelly = true;
            else ticksSinceJump = 0;
        }
    }

    private BoundingBox getBlockBoundingBox(BlockPosition position) {
        return new BoundingBox(
                position.x,
                position.y,
                position.z,
                position.x + 1,
                position.y + 1,
                position.z + 1
        );
    }
    private Vector3d getHitPoint(BoundingBox faceBB, EnumDirection direction) {
        if (!randomizeHitPoint.get()) return BoundingBoxUtils.getFaceCenter(faceBB, direction);
        else return new BoundingBox(
                faceBB.getMinVector().add(
                        Math.random() * faceBB.getSizeX(),
                        Math.random() * faceBB.getSizeY(),
                        Math.random() * faceBB.getSizeZ()
                ),
                faceBB.getMaxVector().sub(
                        Math.random() * faceBB.getSizeX(),
                        Math.random() * faceBB.getSizeY(),
                        Math.random() * faceBB.getSizeZ()
                )
        ).getCenter();
    }

    public boolean shouldKeepY() {
        return !forceUp.get() && keepY.get();
    }
    public boolean shouldForceJump() {
        return forceUp.get() && blocksPlacedSinceJump >= forceUpAfterBlocks.get();
    }
}
