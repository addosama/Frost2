package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import imgui.ImVec2;
import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.utils.ImTextRenderer;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.targeting.EnumEntityTarget;
import pub.frost.wrappers.ClassEnum;
import pub.frost.wrappers.shared.entity.EnumEntity;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module(
        key = "esp",
        category = ModuleCategory.VISUAL
)
public class ESP extends AbstractModule {
    @Property("targets")
    public final MultipleBooleanProperty<EnumEntityTarget> targets = new MultipleBooleanProperty<>(EnumEntityTarget.class, EnumEntityTarget.PLAYERS);
    @Property("targetInvisible")
    public final BooleanProperty targetInvisible = new BooleanProperty(true);

    @Property("name")
    public final BooleanProperty renderName = new BooleanProperty(true);
    @Property("health")
    public final BooleanProperty renderHealth = new BooleanProperty(true);
    @Property("box")
    public final BooleanProperty renderBox = new BooleanProperty(true);

    private final List<EntityData> cachedData = new ArrayList<>();
    private Matrix4f cachedModelView;
    private Matrix4f cachedProjection;

    @EventHandler
    public void onUpdate(EventPlayerUpdateTick event) {
        cachedData.clear();
        mc.getWorld().getLoadedEntityList().stream().filter(
                this::isTarget
        ).forEach(en -> cachedData.add(new EntityData(en)));
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        cachedModelView = RenderUtils.getModelViewMatrix();
        cachedProjection = RenderUtils.getProjectionMatrix();
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        int width = (int) ImGui.getIO().getDisplaySizeX();
        int height = (int) ImGui.getIO().getDisplaySizeY();

        Vector3d playerPos = mc.getPlayer().getLerpedPositionVector(e.getTickDelta());
        cachedData.sort(Comparator.comparingDouble(data -> -data.getEntity().distanceTo(
                playerPos.x(), playerPos.y(), playerPos.z()
        )));

        ImGui.pushFont(FontManager.INSTANCE.puHui10);
        for (EntityData data : cachedData) {
            BoundingBox bb = data.getBoundingBox(e.getTickDelta());
            ImVec2 boxMinPos = null, boxMaxPos = null;

            {
                boolean anyVisible = false;
                float minX = Float.MAX_VALUE;
                float minY = Float.MAX_VALUE;
                float maxX = -Float.MAX_VALUE;
                float maxY = -Float.MAX_VALUE;

                for (Vector3d vertex : bb.getVertices()) {
                    vertex.sub(playerPos);
                    ImVec2 screenPos = RenderUtils.worldToScreen(
                            vertex,
                            cachedModelView, cachedProjection,
                            width, height
                    );

                    if (screenPos == null) continue;

                    // 更新四个边界值
                    if (screenPos.x < minX) minX = screenPos.x;
                    if (screenPos.y < minY) minY = screenPos.y;
                    if (screenPos.x > maxX) maxX = screenPos.x;
                    if (screenPos.y > maxY) maxY = screenPos.y;

                    anyVisible = true;
                }

                if (anyVisible) {
                    boxMinPos = new ImVec2(minX, minY);
                    boxMaxPos = new ImVec2(maxX, maxY);
                }
            }

            if (boxMinPos != null) {
                ImVec2 boxSize = boxMaxPos.minus(boxMinPos);
                if (renderBox.get()) {
                    ImGui.getBackgroundDrawList().addRect(
                            boxMinPos, boxMaxPos,
                            0xFF000000,
                            0,
                            3f
                    );
                    ImGui.getBackgroundDrawList().addRect(
                            boxMinPos, boxMaxPos,
                            -1,
                            0,
                            1f
                    );
                }
                if (renderHealth.get()) {
                    float leftHealthPercent = Math.max(Math.min(data.getHealth() - data.getMaxHealth(), 0) / data.getMaxHealth(), -1);

                    ImVec2 healthMinPos = boxMinPos.minus(4, leftHealthPercent * boxSize.y), healthMaxPos = boxMinPos.plus(-3, boxSize.y);
                    ImGui.getBackgroundDrawList().addRectFilled(
                            healthMinPos.minus(1, 1),
                            healthMaxPos.plus(1, 1),
                            0xFF000000
                    );
                    ImGui.getBackgroundDrawList().addRectFilled(
                            healthMinPos,
                            healthMaxPos,
                            0xFF00FF00
                    );
                }
                if (renderName.get()) {
                    String name = data.getName();
                    float nameWidth = ImTextRenderer.getTextWidth(name);

                    float textX = boxMinPos.x + (boxSize.x - nameWidth) / 2, textY = boxMaxPos.y;
                    ImTextRenderer.drawOutlinedText(
                            ImGui.getBackgroundDrawList(),
                            name,
                            textX, textY,
                            -1, 0xFF000000
                    );
                }
            }
        }
        ImGui.popFont();
    }

    private boolean isTarget(WEntity entity) {
        Class<?> clazz = entity.getWrappedClass();
        if (entity.getWrappedObject() == mc.getPlayer().getWrappedObject()) return false;
        if (entity.isInvisible() && !targetInvisible.get()) return false;
        for (EnumEntityTarget target : targets.getEnabled()) {
            if (target.isTarget(clazz)) return true;
        }
        return false;
    }

    @Getter
    private static class EntityData {
        final WEntity entity;
        final String name;

        final double prevX, prevY, prevZ;
        final double x, y, z;

        final float health, maxHealth;

        EntityData(WEntity entity) {
            this.entity = entity;

            this.name = entity.getName();

            this.prevX = entity.getPrevX();
            this.prevY = entity.getPrevY();
            this.prevZ = entity.getPrevZ();
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();

            float hp = 0, maxHP = 0;
            if (ClassEnum.isInstanceOf(entity, EnumEntity.EntityLivingBase)) {
                WEntityLivingBase living = entity.castTo(WEntityLivingBase.class);
                hp = living.getHealth();
                maxHP = living.getMaxHealth();
            }
            this.health = hp;
            this.maxHealth = maxHP;
        }

        BoundingBox getBoundingBox(float tickDelta) {
            return entity.getLerpedBoundingBox(tickDelta);
        }
    }
}
