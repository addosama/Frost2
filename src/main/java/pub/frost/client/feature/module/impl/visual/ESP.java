package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import imgui.ImVec2;
import org.lwjgl.input.Keyboard;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.overriding.OverrideData;
import pub.frost.client.property.overriding.suppliers.OverrideOnKey;
import pub.frost.utils.MathUtils;
import pub.frost.utils.RenderUtils;
import pub.frost.wrappers.ClassEnum;
import pub.frost.wrappers.shared.entity.EntityClasses;
import pub.frost.wrappers.shared.entity.WEntity;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

@Module(
        key = "esp",
        category = ModuleCategory.VISUAL
)
public class ESP extends AbstractModule {
    @Override
    protected void onInitialized() {
        getEnabledProperty().addOverrideData(new OverrideData<>(new OverrideOnKey(Keyboard.KEY_Z), true));
    }

    private final List<WEntity> cachedEntities = new ArrayList<>();
    private Matrix4f cachedModelView;
    private Matrix4f cachedProjection;

    @EventHandler
    public void onUpdate(EventPlayerUpdateTick event) {
        cachedEntities.clear();
        cachedEntities.addAll(mc.getWorld().getLoadedEntityList());
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        cachedModelView = RenderUtils.getModelViewMatrix();
        cachedProjection = RenderUtils.getProjectionMatrix();
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        float tickDelta = e.getTickDelta();
        Vector3d camera = mc.getPlayer().getPositionEyes(e.getTickDelta());
        int width = (int) ImGui.getIO().getDisplaySizeX();
        int height = (int) ImGui.getIO().getDisplaySizeY();
        for (WEntity entity : cachedEntities) {
            if (!ClassEnum.isInstanceOf(entity, EntityClasses.EntityPlayer)) continue;
            double lerpedX = MathUtils.lerp(entity.getPrevX(), entity.getX(), tickDelta);
            double lerpedY = MathUtils.lerp(entity.getPrevY(), entity.getY(), tickDelta);
            double lerpedZ = MathUtils.lerp(entity.getPrevZ(), entity.getZ(), tickDelta);
            Vector3d lerpedPosVec = new Vector3d(lerpedX, lerpedY, lerpedZ);
            lerpedPosVec.sub(camera);

            ImVec2 pos = RenderUtils.worldToScreen(
                    lerpedPosVec,
                    cachedModelView, cachedProjection,
                    width, height
            );

            if (pos != null) {
                ImGui.getBackgroundDrawList().addText(
                        pos,
                        -1,
                        entity.getName()
                );
            }
        }
    }
}
