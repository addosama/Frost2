package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import imgui.ImVec2;
import javax.vecmath.Matrix4f;

import javafx.scene.chart.Axis;
import net.minecraft.block.BlockChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.tileentity.TileEntityChest;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.color.ColorProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.utils.*;

import java.util.*;

@Module(
        key = "ChestESP",
        category = ModuleCategory.VISUAL
)
public class ChestESP extends AbstractModule {
    @TranslationKey("modules.esp.props.box.mode")
    @Property("mode")
    public final ModeProperty<EnumBoxRenderType> mode = new ModeProperty<>(EnumBoxRenderType.BOX_2D);

    @TranslationKey("modules.esp.props.box.expand")
    @Property("expand")
    public final FloatProperty expand = new FloatProperty(0, 1, 0.1f, 0.1f);

    @TranslationKey("modules.esp.props.box.thickness")
    @Property("thickness")
    public final FloatProperty thickness = new FloatProperty(0.5f, 3f, 0.5f, 1f);
    @TranslationKey("modules.esp.props.box.shadow")
    @Property("shadow")
    public final BooleanProperty shadow = new BooleanProperty(true).setVisibilitySupplier(() -> !mode.is(EnumBoxRenderType.RECT));

    @TranslationKey("modules.esp.props.box.boxcolor")
    @Property("BoxColor")
    public final ColorProperty boxColor = new ColorProperty(0xFFFFFFFF, true);
    @TranslationKey("modules.esp.props.box.shadowcolor")
    @Property("ShadowColor")
    public final ColorProperty shadowColor = new ColorProperty(0x33000000, true).setVisibilitySupplier(shadow::get);
    
    private final List<TileEntity> cachedChestData = new ArrayList<>();
    private Matrix4f cachedModelView, cachedProjection;
    
    @EventHandler
    private void onRender3D(EventRender3D event) {
        cachedModelView = RenderUtils.getModelViewMatrix();
        cachedProjection = RenderUtils.getProjectionMatrix();
    }
    
    @EventHandler
    private void onUpdate(EventPlayerUpdateTick event) {
        cachedChestData.clear();
        mc.theWorld.loadedTileEntityList.stream().filter(
                tile -> tile instanceof TileEntityChest
        ).forEach(cachedChestData::add);
    }
    
    @EventHandler
    private void onRender2D(EventRender2D event) {
        final Vec3 playerPos = EntityUtils.getLerpedPositionVector(mc.thePlayer, event.getTickDelta());
        final Vec3 negatedPlayerPos = VecUtils.negate(playerPos);
        // render chests
        if (!cachedChestData.isEmpty()) {
            cachedChestData.sort(Comparator.comparingDouble(tile ->
                    -tile.getDistanceSq(playerPos.xCoord, playerPos.yCoord, playerPos.zCoord)
            ));
            final double expandSize = expand.get();
            final EnumBoxRenderType renderType = mode.get();
            final float thickness = this.thickness.get();
            final boolean shadow = this.shadow.get();
            Set<TileEntity> renderedChests = new HashSet<>();
            for (TileEntity tile : cachedChestData) {
                if (renderedChests.contains(tile)) continue;
                renderedChests.add(tile);

                BlockPos pos = tile.getPos();
                AxisAlignedBB bb = new AxisAlignedBB(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
                );
                if (tile instanceof TileEntityChest) {
                    TileEntityChest chest = (TileEntityChest) tile;
                    TileEntityChest adjacent;
                    blockAdjacentGetter:
                    {
                        adjacent = chest.adjacentChestXNeg;
                        if (adjacent != null) break blockAdjacentGetter;
                        else adjacent = chest.adjacentChestXPos;
                        if (adjacent != null) break blockAdjacentGetter;
                        else adjacent = chest.adjacentChestZPos;
                        if (adjacent != null) break blockAdjacentGetter;
                        else adjacent = chest.adjacentChestZNeg;
                    }

                    if (adjacent != null) {
                        renderedChests.add(adjacent);
                        BlockPos adjacentPos = adjacent.getPos().subtract(chest.getPos());
                        bb = bb.addCoord(adjacentPos.getX(), adjacentPos.getY(), adjacentPos.getZ());
                    }
                }

                if (expandSize != 0) {
                    bb = bb.expand(expandSize, expandSize, expandSize);
                }

                RenderUtils.renderBox(
                        BoundingBoxUtils.move(bb, negatedPlayerPos),
                        cachedModelView, cachedProjection,
                        (int) ImGui.getIO().getDisplaySizeX(), (int) ImGui.getIO().getDisplaySizeY(),
                        renderType, thickness, shadow? 2 : 0,
                        boxColor.getValueABGR(), shadowColor.getValueABGR()
                );
            }
        }
    }
}
