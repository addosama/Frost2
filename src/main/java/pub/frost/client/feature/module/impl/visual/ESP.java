package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import imgui.ImVec2;
import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.base.rendering.FontManager;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.feature.module.impl.visual.esp.BoxESP;
import pub.frost.client.feature.module.impl.visual.esp.ChestESP;
import pub.frost.client.feature.module.impl.visual.esp.NameTagESP;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.preset.TargetSetting;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.ImTextRenderer;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.EnumTextFormatting;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import pub.frost.wrappers.shared.world.WWorld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module(
        key = "esp",
        category = ModuleCategory.VISUAL
)
public class ESP extends AbstractModule {
    @InsertProperty
    private final TargetSetting target = new TargetSetting(null, true, false, false);

    @InsertProperty("Box")
    public final BoxESP box = new BoxESP(this);
    @InsertProperty("NameTag")
    public final NameTagESP nameTag = new NameTagESP(this);
    @InsertProperty("Chest")
    public final ChestESP chest = new ChestESP(this);
    @Property("health")
    public final BooleanProperty renderHealth = new BooleanProperty(true);
    @Property("data")
    public final BooleanProperty renderData = new BooleanProperty(true);

    private final WEntity entityWrapper = Wrappers.Entity;
    private final WWorld worldWrapper = Wrappers.World;
    private final WEntityLivingBase livingEntityWrapper = Wrappers.EntityLivingBase;

    private final List<EntityData> cachedData = new ArrayList<>();
    private final List<Object> cachedChestData = new ArrayList<>();
    private Matrix4f cachedModelView;
    private Matrix4f cachedProjection;

    @EventHandler
    public void onUpdate(EventPlayerUpdateTick event) {
        cachedData.clear();
        worldWrapper.getLoadedEntityList(mcWrapper.getWorld(mc)).stream().filter(
                this::isTarget
        ).forEach(en -> cachedData.add(new EntityData(en)));

        cachedChestData.clear();
        if (chest.enabled.get()) {
            worldWrapper.getLoadedTileEntityList(mcWrapper.getWorld(mc)).stream().filter(
                    Wrappers.TileEntity::isChest
            ).forEach(cachedChestData::add);
        }
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
        Vector3d playerPos = entityWrapper.getLerpedPositionVector(mcWrapper.getPlayer(mc), e.getTickDelta());
        Vector3d negatedPlayerPos = playerPos.negate(new Vector3d());

        // sort entities by distance
        {
            cachedData.sort(Comparator.comparingDouble(data -> -entityWrapper.distanceTo(
                    data.getEntity(),
                    playerPos.x(), playerPos.y(), playerPos.z()
            )));
        }

        ImGui.pushFont(FontManager.INSTANCE.puHui10);
        for (EntityData data : cachedData) {
            BoundingBox lerpedBB = data.getBoundingBox(e.getTickDelta()).move(negatedPlayerPos);
            BoundingBox prevBB = data.getBoundingBox(0).move(negatedPlayerPos);
            BoundingBox tickBB = data.getBoundingBox(1).move(negatedPlayerPos);

            // expand bounding box
            {
                double expandSize = box.expand.get();
                lerpedBB = lerpedBB.expand(expandSize, expandSize, expandSize);
                prevBB = prevBB.expand(expandSize, expandSize, expandSize);
                tickBB = tickBB.expand(expandSize, expandSize, expandSize);
            }

            // project vertices to screen
            final Vector3d[] vertexArray = lerpedBB.getVertices();
            final ImVec2[] vertexScreenPosArray = new ImVec2[vertexArray.length];
            ImVec2 minVec, maxVec, centerVec = RenderUtils.worldToScreen(
                    lerpedBB.getCenter(),
                    cachedModelView, cachedProjection,
                    width, height
            );
            {
                for (int i = 0; i < 8; i++) {
                    Vector3d vertex = vertexArray[i];
                    vertexScreenPosArray[i] = RenderUtils.worldToScreen(
                            vertex,
                            cachedModelView, cachedProjection,
                            width, height
                    );
                }

                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
                for (ImVec2 vec : vertexScreenPosArray) {
                    if (vec == null) {
                        continue;
                    }
                    if (vec.x < minX) minX = vec.x;
                    if (vec.y < minY) minY = vec.y;
                    if (vec.x > maxX) maxX = vec.x;
                    if (vec.y > maxY) maxY = vec.y;
                }
                minVec = new ImVec2(minX, minY);
                maxVec = new ImVec2(maxX, maxY);
            }

            // render box
            if (box.enabled.get()) box.renderBox(
                    lerpedBB, prevBB, tickBB,
                    cachedModelView, cachedProjection,
                    width, height,
                    minVec, maxVec
            );

            ImVec2 boxSize = maxVec.minus(minVec);

            if (renderHealth.get()) {
                float leftHealthPercent = Math.max(Math.min(data.getHealth() - data.getMaxHealth(), 0) / data.getMaxHealth(), -1);

                ImVec2 healthMinPos = minVec.minus(4, leftHealthPercent * boxSize.y), healthMaxPos = minVec.plus(-3, boxSize.y);
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
            if (nameTag.enabled.get()) nameTag.renderNameTag(
                    lerpedBB,
                    cachedModelView, cachedProjection,
                    width, height,
                    minVec, maxVec,
                    data.getName(), boxSize.x
            );
            if (renderData.get()) {
                float x = minVec.x + boxSize.x + 2f;
                float yOffset = 0;
                if (Teams.isTeammate(data.getEntity())) {
                    ImTextRenderer.drawOutlinedText(
                            ImGui.getBackgroundDrawList(),
                            EnumTextFormatting.GREEN + "TEAM",
                            x, minVec.y,
                            -1,
                            0xFF000000
                    );
                    yOffset += ImTextRenderer.getTextHeight();
                }
            }
        }

        // render chests
        if (chest.enabled.get() && !cachedChestData.isEmpty()) {
            cachedChestData.sort(Comparator.comparingDouble(tile ->
                    -Wrappers.TileEntity.distanceTo(tile, playerPos.x(), playerPos.y(), playerPos.z())
            ));
            double expandSize = box.expand.get();
            for (Object tile : cachedChestData) {
                BoundingBox bb = Wrappers.TileEntity.getBoundingBox(tile).move(negatedPlayerPos);
                if (expandSize != 0) {
                    bb = bb.expand(expandSize, expandSize, expandSize);
                }
                chest.renderChest(bb, cachedModelView, cachedProjection, width, height);
            }
        }

        ImGui.popFont();
    }

    private boolean isTarget(Object entity) {
        if (entity == mcWrapper.getPlayer(mc)) return false;
        return target.isTarget(entity);
    }

    @Getter
    private class EntityData {
        final Object entity;

        final String name;

        final double prevX, prevY, prevZ;
        final double x, y, z;

        final float health, maxHealth;

        EntityData(Object entity) {
            this.entity = entity;

            this.name = EntityUtils.tryGetDisplayName(entity);

            this.prevX = entityWrapper.getPrevX(entity);
            this.prevY = entityWrapper.getPrevY(entity);
            this.prevZ = entityWrapper.getPrevZ(entity);
            this.x = entityWrapper.getX(entity);
            this.y = entityWrapper.getY(entity);
            this.z = entityWrapper.getZ(entity);

            float hp = 0, maxHP = 0;
            if (livingEntityWrapper.isTarget(entity.getClass())) {
                hp = livingEntityWrapper.getHealth(entity);
                maxHP = livingEntityWrapper.getMaxHealth(entity);
            }
            this.health = hp;
            this.maxHealth = maxHP;
        }

        BoundingBox getBoundingBox(float tickDelta) {
            return entityWrapper.getLerpedBoundingBox(entity, tickDelta);
        }
    }
}
