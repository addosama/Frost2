package pub.frost.base.rendering;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.implot.ImPlot;
import lombok.Getter;
import loutre.imgui.lwjgl2.ImGuiDisplay;
import loutre.imgui.lwjgl2.ImGuiLWJGL2;
import org.lwjgl.opengl.GL11;
import pub.frost.client.core.FrostCore;

public class ImGuiContext {
    @Getter
    private static final ImGuiContext instance = new ImGuiContext();

    private final ImGuiDisplay imGuiDisplay = new ImGuiDisplay();
    private final ImGuiLWJGL2 imGuiImplGl2 = new ImGuiLWJGL2();

    private boolean initialized = false;
    public void initialize() {
        if (initialized) return;

        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(FrostCore.getClientDir().resolve("imgui.ini").toAbsolutePath().toString());
        io.getFonts().setFreeTypeRenderer(true);

        imGuiDisplay.init();
        imGuiImplGl2.init();
        new FontManager();

        initialized = true;
    }

    public void startFrame() {
        if (!initialized) initialize();
        ImGui.getStyle().setAntiAliasedFill(true);
        ImGui.getStyle().setAntiAliasedLines(true);
        ImGui.getStyle().setAntiAliasedLinesUseTex(true);
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
}
