package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.color.ColorProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.utils.EnumBoxRenderType;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module(
        key = "ChestESP",
        category = ModuleCategory.VISUAL
)
public class ChestESP extends AbstractModule {
    @TranslationKey("modules.esp.props.box.subprops.mode")
    @Property("mode")
    public final ModeProperty<EnumBoxRenderType> mode = new ModeProperty<>(EnumBoxRenderType.BOX_2D);

    @TranslationKey("modules.esp.props.box.subprops.expand")
    @Property("expand")
    public final FloatProperty expand = new FloatProperty(0, 1, 0.1f, 0.1f);

    @TranslationKey("modules.esp.props.box.subprops.thickness")
    @Property("thickness")
    public final FloatProperty thickness = new FloatProperty(0.5f, 3f, 0.5f, 1f);
    @TranslationKey("modules.esp.props.box.subprops.shadow")
    @Property("shadow")
    public final BooleanProperty shadow = new BooleanProperty(true).setVisibilitySupplier(() -> !mode.is(EnumBoxRenderType.RECT));

    @TranslationKey("modules.esp.props.box.subprops.boxcolor")
    @Property("BoxColor")
    public final ColorProperty boxColor = new ColorProperty(0xFFFFFFFF, true);
    @TranslationKey("modules.esp.props.box.subprops.shadowcolor")
    @Property("ShadowColor")
    public final ColorProperty shadowColor = new ColorProperty(0x33000000, true).setVisibilitySupplier(shadow::get);
    
    private final List<Object> cachedChestData = new ArrayList<>();
    private Matrix4f cachedModelView, cachedProjection;
    
    @EventHandler
    private void onRender3D(EventRender3D event) {
        cachedModelView = RenderUtils.getModelViewMatrix();
        cachedProjection = RenderUtils.getProjectionMatrix();
    }
    
    @EventHandler
    private void onUpdate(EventPlayerUpdateTick event) {
        cachedChestData.clear();
        World.getLoadedTileEntityList(Minecraft.getWorld(mc)).stream().filter(
                Wrappers.TileEntity::isChest
        ).forEach(cachedChestData::add);
    }
    
    @EventHandler
    private void onRender2D(EventRender2D event) {
        final Vector3d playerPos = Entity.getLerpedPositionVector(Minecraft.getPlayer(mc), event.getTickDelta());
        final Vector3d negatedPlayerPos = playerPos.negate(new Vector3d());
        // render chests
        if (!cachedChestData.isEmpty()) {
            cachedChestData.sort(Comparator.comparingDouble(tile ->
                    -Wrappers.TileEntity.distanceTo(tile, playerPos.x(), playerPos.y(), playerPos.z())
            ));
            final double expandSize = expand.get();
            final EnumBoxRenderType renderType = mode.get();
            final float thickness = this.thickness.get();
            final boolean shadow = this.shadow.get();
            for (Object tile : cachedChestData) {
                BoundingBox bb = Wrappers.TileEntity.getBoundingBox(tile).move(negatedPlayerPos);
                if (expandSize != 0) {
                    bb = bb.expand(expandSize, expandSize, expandSize);
                }

                RenderUtils.renderBox(
                        bb,
                        cachedModelView, cachedProjection,
                        (int) ImGui.getIO().getDisplaySizeX(), (int) ImGui.getIO().getDisplaySizeY(),
                        renderType, thickness, shadow? 2 : 0,
                        boxColor.getValueABGR(), shadowColor.getValueABGR()
                );
            }
        }
    }
}
