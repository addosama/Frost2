package pub.frost.base.rendering;

import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import lombok.Getter;
import loutre.imgui.lwjgl2.ImGuiDisplay;
import loutre.imgui.lwjgl2.ImGuiLWJGL2;
import org.lwjgl.opengl.GL11;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventInput;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;

public class ClientRenderContext {
    @Getter
    private static final ClientRenderContext instance = new ClientRenderContext();

    private final ImGuiDisplay imGuiDisplay = new ImGuiDisplay();
    private final ImGuiLWJGL2 imGuiImplGl2 = new ImGuiLWJGL2();

    private boolean initialized = false;
    public void initialize() {
        if (initialized) return;

        ImGui.createContext();
        ImGui.getIO().getFonts().setFreeTypeRenderer(true);
        ImGui.getIO().setIniFilename(FrostCore.getClientDir().resolve("imgui.ini").toAbsolutePath().toString());
        new FontManager();

        ImPlot.createContext();

        imGuiDisplay.init();
        imGuiImplGl2.init();

        initialized = true;
    }

    public void startFrame() {
        if (!initialized) initialize();
        ImGui.getStyle().setAntiAliasedFill(true);
        ImGui.getStyle().setAntiAliasedLines(true);
        ImGui.getStyle().setAntiAliasedLinesUseTex(true);
        ImGui.getStyle().setDisplayWindowPadding(0, 0);
        imGuiImplGl2.newFrame();
        imGuiDisplay.newFrame();
        ImGui.newFrame();
    }
    public void endFrame() {
        ImGui.render();

        boolean alphaState = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
        if (alphaState) GL11.glDisable(GL11.GL_ALPHA_TEST);
        imGuiImplGl2.renderDrawData(ImGui.getDrawData());
        if (alphaState) GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void draw(Runnable runnable) {
        instance.startFrame();
        runnable.run();
        instance.endFrame();
    }

    @EventHandler(priority = -100)
    private void onInput(EventInput event) {
        if (event.getType() == InputDevice.KEYBOARD) {
            imGuiDisplay.onKey();
        }
    }
}
