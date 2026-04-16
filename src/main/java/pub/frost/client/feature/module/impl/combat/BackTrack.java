package pub.frost.client.feature.module.impl.combat;

import imgui.ImGui;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
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

    private boolean usingLaggedEntity = false;
    private long lagStart = -1;

    private final Deque<Object> playerPacketDeque = new ConcurrentLinkedDeque<>();
    private final Map<Object, Vector3d> entityPositionMap = new ConcurrentHashMap<>();
    private final Map<Object, Deque<PacketData>> entityPacketMap = new ConcurrentHashMap<>();

    @EventHandler
    private void onGameTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            if (getPlayer() == null) return;
            Vector3d playerPos = getPlayerPos();
            if (usingLaggedEntity && lagStart > 0) {
                if (lagStart + latency.get() < System.currentTimeMillis()) {
                    flushStoredPackets();
                }
            }
            for (Map.Entry<Object, Vector3d> entry : entityPositionMap.entrySet()) {
                boolean unregister = Math.max(playerPos.distance(Entity.getPositionVector(entry.getKey())), playerPos.distance(entry.getValue())) > range.get();
                unregister |= Entity.isDead(entry.getKey());
                if (unregister) {
                    unregisterEntity(entry.getKey());
                } else {
                    Deque<PacketData> deque = entityPacketMap.get(entry.getKey());
                    if (deque == null) return;

                    Object netHandler = mcWrapper.getNetHandler(mc);
                    long time = System.currentTimeMillis();

                    while (!deque.isEmpty()) {
                        PacketData data = deque.peekFirst();
                        if (data.timestamp + latency.get() > time) {
                            data.release(netHandler);
                            deque.pollFirst();
                        } else break;
                    }
                }
            }
        }
    }
    @EventHandler
    private void onPacket(EventPacket event) {
        Object packet = event.getPacket();
        Object world = mcWrapper.getWorld(mc);
        if (event.getType() == PacketType.OUT) {
            if (UseEntityPacket.isTarget(packet)) {
                Object targetEntity = UseEntityPacket.getEntityFromWorld(packet, world);
                if (entityPacketMap.containsKey(targetEntity)) {
                    if (Entity.distanceTo(getPlayer(), Entity.getPositionVector(targetEntity)) > 3) {
                        if (!usingLaggedEntity) lagStart = System.currentTimeMillis();
                        usingLaggedEntity = true;
                    }
                }
            }
            if (usingLaggedEntity) {
                playerPacketDeque.addLast(packet);
                event.cancel();
            }
        } else {
            if (EntityPacket.isTarget(packet)) {
                Object entity = EntityPacket.getEntity(packet, world);
                if (entity == getPlayer()) return;

                Vector3d entityPos = Entity.getPositionVector(entity);
                if (entityPos.distance(getPlayerPos()) <= range.get()) {
                    updateEntity(entity, entityPos, packet);
                    event.cancel();
                }
            } else if (EntityTeleportPacket.isTarget(packet)) {
                Object entity = World.getEntityById(world, EntityTeleportPacket.getEntityId(packet, world));
                if (entity == getPlayer()) return;

                if (Entity.getPositionVector(entity).distance(getPlayerPos()) <= range.get()) resetEntity(entity, packet);
            }
        }
    }

    private Matrix4f cachedModelView;
    private Matrix4f cachedProjection;
    @EventHandler
    private void onRender3D(EventRender3D event) {
        cachedModelView = RenderUtils.getModelViewMatrix();
        cachedProjection = RenderUtils.getProjectionMatrix();
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
                    box,
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
            entityPacketMap.putIfAbsent(entity, new ArrayDeque<>());

            Deque<PacketData> deque = entityPacketMap.get(entity);
            deque.addLast(new PacketData(packet, System.currentTimeMillis()));
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
            flushStoredPackets();
            entityPositionMap.put(entity, new Vector3d(targetPos));
            entityPacketMap.computeIfPresent(entity, (en, deque) -> {
                deque.clear();
                return deque;
            });
        }
    }

    private void unregisterEntity(Object entity) {
        flushStoredPackets();
        entityPositionMap.remove(entity);
        entityPacketMap.computeIfPresent(entity, (en, deque) -> {
            Object netHandler = mcWrapper.getNetHandler(mc);
            for (PacketData data : deque) data.release(netHandler);
            return null;
        });
    }

    private void flushStoredPackets() {
        while (!playerPacketDeque.isEmpty()) {
            FrostCore.getInstance().getPacketManager().sendPacket(playerPacketDeque.pollFirst(), false);
        }
        usingLaggedEntity = false;
        lagStart = -1;
    }

    private Object getPlayer() {
        return mcWrapper.getPlayer(mc);
    }
    private Vector3d getPlayerPos() {
        return Entity.getPositionVector(getPlayer());
    }

    @Override
    protected void onDisabled() {
        flushStoredPackets();
        entityPacketMap.clear();
        entityPositionMap.clear();
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
