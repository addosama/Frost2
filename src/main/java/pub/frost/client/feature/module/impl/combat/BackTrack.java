package pub.frost.client.feature.module.impl.combat;

import imgui.ImGui;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Module(
        key = "BackTrack",
        category = ModuleCategory.COMBAT
)
public class BackTrack extends AbstractModule {
    @Property("Range")
    public final FloatProperty range = new FloatProperty(0, 6, 0.01f, 3.7f);
    @Property("Latency")
    public final IntegerProperty latency = new IntegerProperty(0, 500, 1, 300);
    @Property("RenderRealPos")
    public final BooleanProperty renderRealPos = new BooleanProperty(true);

    private boolean active;

    private final Map<Object, Vector3d> entityPositionMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<PacketData> packets = new ConcurrentLinkedDeque<>();

    @EventHandler
    private void onGameTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            if (getPlayer() == null) return;
            Vector3d playerPos = getPlayerPos();
            active = false;
            for (Map.Entry<Object, Vector3d> entry : entityPositionMap.entrySet()) {
                boolean unregister = Math.max(playerPos.distance(Entity.getPositionVector(entry.getKey())), playerPos.distance(entry.getValue())) > range.get();
                unregister |= Entity.isDead(entry.getKey());
                if (unregister) {
                    unregisterEntity(entry.getKey());
                } else {
                    active = true;
                }
            }
        }
    }
    @EventHandler
    private void onPacket(EventPacket event) {
        Object packet = event.getPacket();
        Object world = mcWrapper.getWorld(mc);

        if (EntityPacket.isTarget(packet)) {
            Object entity = EntityPacket.getEntity(packet, world);
            if (entity == getPlayer()) return;
            Vector3d entityPos = Entity.getPositionVector(entity);
            updateEntity(entity, entityPos, packet);
        } else if (EntityTeleportPacket.isTarget(packet)) {
            Object entity = World.getEntityById(world, EntityTeleportPacket.getEntityId(packet, world));
            if (entity == getPlayer()) return;
            resetEntity(entity, packet);
        }

        if (event.getType() == PacketType.IN && active) {
            packets.add(new PacketData(packet, System.currentTimeMillis()));
            event.setCancelled(true);
        }
    }

    private Matrix4f cachedModelView;
    private Matrix4f cachedProjection;
    @EventHandler
    private void onRender3D(EventRender3D event) {
        cachedModelView = RenderUtils.getModelViewMatrix();
        cachedProjection = RenderUtils.getProjectionMatrix();
        if (!packets.isEmpty()) {
            for (PacketData packet : packets) {
                if (packet.timestamp + latency.get() >= System.currentTimeMillis()) {
                    packet.release(mcWrapper.getNetHandler(mc));
                }
            }
        }
    }
    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (!renderRealPos.get()) return;
        for (Map.Entry<Object, Vector3d> entry : entityPositionMap.entrySet()) {
            BoundingBox box = EntityUtils.getBoundingBoxAtPosition(entry.getKey(), entry.getValue()).move(
                    Entity.getLerpedPositionVector(getPlayer(), event.getTickDelta()).negate()
            );
            RenderUtils.drawBoundingBox(
                    ImGui.getBackgroundDrawList(),
                    box, 1f,
                    0xFF00FF00,
                    cachedModelView, cachedProjection,
                    (int) ImGui.getIO().getDisplaySizeX(),
                    (int) ImGui.getIO().getDisplaySizeY()
            );
        }
    }

    private void updateEntity(Object entity, Vector3d entityPos, Object packet) {
        Vector3d targetPos = new Vector3d(entityPositionMap.getOrDefault(entity, entityPos)).add(
                EntityPacket.getPosX(packet) / 32d,
                EntityPacket.getPosY(packet) / 32d,
                EntityPacket.getPosZ(packet) / 32d
        );
        if (getPlayerPos().distance(targetPos) > range.get()) unregisterEntity(entity);
        else {
            entityPositionMap.put(entity, targetPos);
        }
    }

    private void resetEntity(Object entity, Object packet) {
        Vector3d targetPos = new Vector3d(
                EntityTeleportPacket.getX(packet) / 32d,
                EntityTeleportPacket.getY(packet) / 32d,
                EntityTeleportPacket.getZ(packet) / 32d
        );
        if (getPlayerPos().distance(targetPos) > range.get()) unregisterEntity(entity);
        else {
            entityPositionMap.put(entity, new Vector3d(targetPos));
        }
    }

    private void unregisterEntity(Object entity) {
        entityPositionMap.remove(entity);
    }

    private Object getPlayer() {
        return mcWrapper.getPlayer(mc);
    }
    private Vector3d getPlayerPos() {
        return Entity.getPositionVector(getPlayer());
    }

    @Override
    protected void onDisabled() {
        entityPositionMap.clear();
        if (!packets.isEmpty()) {
            for (PacketData packet : packets) {
                packet.release(mcWrapper.getNetHandler(mc));
            }
        }
    }

    @RequiredArgsConstructor
    private static class PacketData {
        final Object packet;
        final long timestamp;

        void release(Object netHandler) {
            Packet.processPacket(packet, netHandler);
        }
    }
}
