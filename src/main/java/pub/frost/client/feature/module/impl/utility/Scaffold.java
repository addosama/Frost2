package pub.frost.client.feature.module.impl.utility;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.*;
import net.minecraft.world.World;
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
import pub.frost.utils.*;
import pub.frost.utils.data.BlockPlacementInfo;
import pub.frost.utils.data.Rotation;
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

    @Property("TryUsePresetYaws")
    public final BooleanProperty tryUsePresetYaws = new BooleanProperty(true);
    @Property("TryUsePresetPitches")
    public final BooleanProperty tryUsePresetPitches = new BooleanProperty(true)
            .setVisibilitySupplier(tryUsePresetYaws::get);
    @Property("CalcEveryPitch")
    public final BooleanProperty calcEveryPitch = new BooleanProperty(false);
    @Property("RandomizeHitPoint")
    public final BooleanProperty randomizeHitPoint = new BooleanProperty(true);

    @Property("PreferPitchChange")
    public final BooleanProperty preferPitchChange = new BooleanProperty(false);
    @Property("SlowRotationAfterPlace")
    public final BooleanProperty slowRotationAfterPlace = new BooleanProperty(true);
    @Property("SlowTicks")
    public final IntegerProperty slowTicks = new IntegerProperty(1, 10, 1, 2)
            .setVisibilitySupplier(slowRotationAfterPlace::get);
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

    private final List<EnumFacing> availableFaceList = Arrays.asList(
            EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST,
            EnumFacing.UP
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
        final EntityPlayerSP player = mc.thePlayer;
        World world = mc.theWorld;
        if (player == null || world == null) {
            this.setEnabled(false);
            return;
        }

        Vec3 playerPos = player.getPositionVector();
        if (player.onGround) {
            lastOnGroundY = (int) playerPos.yCoord;
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

        BlockPos targetBlock = new BlockPos(
                (int) Math.floor(playerPos.xCoord),
                (shouldKeepY() || (telly.get() && !upTelly && !shouldForceJump()))? lastOnGroundY : (int) Math.floor(playerPos.yCoord),
                (int) Math.floor(playerPos.zCoord)
        ).offset(EnumFacing.DOWN);

//        if (!placeDeque.isEmpty()) {
//            Map.Entry data = placeDeque.peekLast();
//            if (new BlockPos(data.getBlockToUse()).offset(data.getFaceToUse()).equals(targetBlock)) return;
//        }

        final int searchRange = this.searchRange.get();
        List<BlockPos> usableBlocks = BlockUtils.getBlocksInRange(targetBlock, searchRange, world)
                .stream()
                .sorted(Comparator.comparingDouble(targetBlock::distanceSq))
                .collect(Collectors.toList());

        placeDeque.clear();
        placeDeque.addAll(PathfindingUtils.placePathToBlock(
                usableBlocks, targetBlock,
                availableFaceList, pos -> world.isAirBlock(pos) && world.getCollidingBoundingBoxes(
                        player,
                        getBlockAxisAlignedBB(pos)
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
                EntityPlayerSP player = mc.thePlayer;
                Vec3 eyePos = player.getPositionEyes(1);
                AxisAlignedBB blockBB = getBlockAxisAlignedBB(data.getBlockToUse());
                AxisAlignedBB faceBB = BoundingBoxUtils.getFaceBoundingBox(
                        blockBB,
                        data.getFaceToUse()
                );
                Rotation bestRot = rotationProvider.getRotation(
                        eyePos,
                        faceBB,
                        getHitPoint(faceBB, data.getFaceToUse()),
                        r -> true, true
                );

                if (lastProvidedRotation != null) {
                    if (RayCastUtils.getSimpleHitResult(
                            eyePos,
                            lastProvidedRotation.getYaw(), lastProvidedRotation.getPitch(),
                            faceBB
                    ).getKey()) rotation = lastProvidedRotation;
                    else if (preferPitchChange.get()) {
                        Rotation rotWithPitchChange = new Rotation(lastProvidedRotation.getYaw(), bestRot.getPitch());
                        if (RayCastUtils.getSimpleHitResult(
                                eyePos,
                                rotWithPitchChange.getYaw(), rotWithPitchChange.getPitch(),
                                faceBB
                        ).getKey()) rotation = rotWithPitchChange;
                    }
                }

                if (rotation == null) {
                    if (tryUsePresetYaws.get()) {
                        Rotation lastRotation = lastProvidedRotation == null?
                                new Rotation(player.rotationYaw, player.rotationPitch) : lastProvidedRotation;

                        Float[] yawArray = {
                                -135F,
                                -90F,
                                -45F,
                                0F,
                                45F,
                                90F,
                                135F,
                                180F,
                                bestRot.getYaw()
                        };
                        Arrays.sort(
                                yawArray, (a, b) -> Float.compare(
                                        Math.abs(RotationUtils.wrapYawTo180(lastRotation.getYaw() - 180 - a)),
                                        Math.abs(RotationUtils.wrapYawTo180(lastRotation.getYaw() - 180 - b))
                                )
                        );
                        float[] pitchArray = {75.0F, 82.0F, 87.0F};
                        boolean usePresetPitches = this.tryUsePresetPitches.get();

                        for (float yaw : yawArray) {
                            if (usePresetPitches) {
                                for (float pitch : pitchArray) {
                                    // random range -0.3 to 0.3
                                    Rotation candidate = new Rotation(yaw, pitch);
                                    boolean matches = RayCastUtils.getSimpleHitResult(
                                            eyePos, yaw, pitch, faceBB
                                    ).getKey();
//                                    boolean matches = rayCast.is(EnumRaycastType.LEGIT)
//                                            ? RayCastUtil.overBlock(candidate, pos)
//                                            : RayCastUtil.overBlock(candidate, pos, direction);
                                    if (matches) {
                                        rotation = candidate;
                                        break;
                                    }
                                }
                            }

                            if (calcEveryPitch.get()) {
                                for (int pitch = -90; pitch < 90; pitch++) {
                                    Rotation candidate = new Rotation(yaw, pitch);
                                    boolean matches = RayCastUtils.getSimpleHitResult(
                                            eyePos, yaw, pitch, faceBB
                                    ).getKey();
//                                    boolean matches = rayCast.isMode("Normal")
//                                            ? RayCastUtil.overBlock(candidate, pos)
//                                            : RayCastUtil.overBlock(candidate, pos, direction);

                                    if (matches) {
                                        rotation = candidate;
                                        break;
                                    }
                                }
                            }
                            else {
                                Rotation candidate = new Rotation(yaw, bestRot.getPitch());
                                if (RayCastUtils.getSimpleHitResult(
                                        eyePos, yaw, candidate.getPitch(), faceBB
                                ).getKey()) {
                                    rotation = candidate;
                                    break;
                                }
                            }
                        }
                    }
                    else rotation = bestRot;
                }
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
        EntityPlayerSP player = mc.thePlayer;
        net.minecraft.item.ItemStack stack = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem();
        if (stack == null) return;
        if (!(stack.getItem() instanceof ItemBlock)) return;

        final int maxPlace = maxPlacePerTick.get();
        int placed = 0;
        while (!placeDeque.isEmpty() && placed < maxPlace) {
            placed++;
            BlockPlacementInfo data = placeDeque.peek();
            if (data == null) continue;
            if (placed == 0 && mc.theWorld.isAirBlock(data.getBlockToUse())) continue;

            AxisAlignedBB blockBB = getBlockAxisAlignedBB(data.getBlockToUse());
            Vec3 hitVec = BoundingBoxUtils.getFaceCenter(
                    blockBB,
                    data.getFaceToUse()
            );

            if (rayCast.get() != EnumRaycastType.DISABLED) {
                if (rayCast.is(EnumRaycastType.LEGIT)) {
                    MovingObjectPosition result = EntityUtils.getLookingObject(
                            player,
                            player.getLook(1),
                            3, 1
                    );
                    if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && data.getBlockToUse().equals(result.getBlockPos())) {
                        hitVec = result.hitVec;
                    } else continue;
                } else {
                    Map.Entry<Boolean, Vec3> result = RayCastUtils.getSimpleHitResult(
                            player.getPositionEyes(1),
                            player.rotationYaw,
                            player.rotationPitch,
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
                    new BlockPos(data.getBlockToUse().getX(), data.getBlockToUse().getY(), data.getBlockToUse().getZ()),
                    EnumFacing.VALUES[data.getFaceToUse().getIndex()],
                    new Vec3(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord)
            )) player.swingItem();
            placeDeque.poll();
            ticksSincePlace = 0;
            if (data.getBlockToUse().getY() <= lastOnGroundY) {
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
        boolean onGround = mc.thePlayer.onGround;
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

    private AxisAlignedBB getBlockAxisAlignedBB(BlockPos position) {
        return new AxisAlignedBB(
                position.getX(),
                position.getY(),
                position.getZ(),
                position.getX() + 1,
                position.getY() + 1,
                position.getZ() + 1
        );
    }
    private Vec3 getHitPoint(AxisAlignedBB faceBB, EnumFacing direction) {
        if (!randomizeHitPoint.get()) return BoundingBoxUtils.getFaceCenter(faceBB, direction);
        else return BoundingBoxUtils.getCenter(BoundingBoxUtils.createBox(
                BoundingBoxUtils.getMinVector(faceBB).add(new Vec3(
                        Math.random() * BoundingBoxUtils.getSizeX(faceBB),
                        Math.random() * BoundingBoxUtils.getSizeY(faceBB),
                        Math.random() * BoundingBoxUtils.getSizeZ(faceBB)
                )),
                BoundingBoxUtils.getMaxVector(faceBB).subtract(new Vec3(
                        Math.random() * BoundingBoxUtils.getSizeX(faceBB),
                        Math.random() * BoundingBoxUtils.getSizeY(faceBB),
                        Math.random() * BoundingBoxUtils.getSizeZ(faceBB)
                ))
        ));
    }

    public boolean shouldKeepY() {
        return !forceUp.get() && keepY.get();
    }
    public boolean shouldForceJump() {
        return forceUp.get() && blocksPlacedSinceJump >= forceUpAfterBlocks.get();
    }
}
