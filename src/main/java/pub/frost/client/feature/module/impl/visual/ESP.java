package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import imgui.ImVec2;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.base.rendering.FontManager;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

import net.minecraft.client.Minecraft;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.feature.module.impl.visual.esp.BoxESP;
import pub.frost.client.feature.module.impl.visual.esp.NameTagESP;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.preset.legacy.TargetSetting;
import pub.frost.utils.*;
import pub.frost.utils.data.EnumTextFormatting;

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
    @Property("health")
    public final BooleanProperty renderHealth = new BooleanProperty(true);
    @Property("data")
    public final BooleanProperty renderData = new BooleanProperty(true);

    private final List<EntityData> cachedData = new ArrayList<>();
    private Matrix4f cachedModelView;
    private Matrix4f cachedProjection;

    @EventHandler
    public void onUpdate(EventPlayerUpdateTick event) {
        cachedData.clear();
        mc.theWorld.getLoadedEntityList().stream().filter(
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
        Vec3 playerPos = EntityUtils.getLerpedPositionVector(mc.thePlayer, e.getTickDelta());
        Vec3 negatedPlayerPos = VecUtils.negate(playerPos);

        // sort entities by distance
        {
            cachedData.sort(Comparator.comparingDouble(data -> -data.getEntity().getDistance(
                    playerPos.xCoord, playerPos.yCoord, playerPos.zCoord
            )));
        }

        FontManager.pushFont(FontManager.INSTANCE.puHui10);
        for (EntityData data : cachedData) {
            AxisAlignedBB lerpedBB = BoundingBoxUtils.move(data.getBoundingBox(e.getTickDelta()), negatedPlayerPos);
            AxisAlignedBB prevBB = BoundingBoxUtils.move(data.getBoundingBox(0), negatedPlayerPos);;
            AxisAlignedBB tickBB = BoundingBoxUtils.move(data.getBoundingBox(1), negatedPlayerPos);;

            // expand bounding box
            {
                double expandSize = box.expand.get();
                lerpedBB = lerpedBB.expand(expandSize, expandSize, expandSize);
                prevBB = prevBB.expand(expandSize, expandSize, expandSize);
                tickBB = tickBB.expand(expandSize, expandSize, expandSize);
            }

            // project vertices to screen
            final Vec3[] vertexArray = BoundingBoxUtils.getVertices(lerpedBB);
            final ImVec2[] vertexScreenPosArray = new ImVec2[vertexArray.length];
            ImVec2 minVec, maxVec;
            {
                for (int i = 0; i < 8; i++) {
                    Vec3 vertex = vertexArray[i];
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
                    width, height
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

        ImGui.popFont();
    }

    private boolean isTarget(Entity entity) {
        if (entity == mc.thePlayer) return false;
        return target.isTarget(entity);
    }

    @Getter
    private class EntityData {
        final Entity entity;

        final String name;

        final double prevX, prevY, prevZ;
        final double x, y, z;

        final float health, maxHealth;

        EntityData(Entity entity) {
            this.entity = entity;

            this.name = EntityUtils.tryGetDisplayName(entity);

            this.prevX = entity.prevPosX;
            this.prevY = entity.prevPosY;
            this.prevZ = entity.prevPosZ;
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;

            float hp = 0, maxHP = 0;
            if (entity instanceof EntityLivingBase) {
                hp = ((EntityLivingBase) entity).getHealth();
                maxHP = ((EntityLivingBase) entity).getMaxHealth();
            }
            this.health = hp;
            this.maxHealth = maxHP;
        }

        AxisAlignedBB getBoundingBox(float tickDelta) {
            return BoundingBoxUtils.lerp(EntityUtils.getPrevBoundingBox(entity), entity.getEntityBoundingBox(), tickDelta);
        }
    }
}
