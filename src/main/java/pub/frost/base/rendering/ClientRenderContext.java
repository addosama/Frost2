package pub.frost.base.rendering;

import imgui.ImGui;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.extension.implot.ImPlot;
import lombok.Getter;
import loutre.imgui.lwjgl2.ImGuiDisplay;
import loutre.imgui.lwjgl2.ImGuiLWJGL2;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import pub.frost.base.input.api.InputListener;
import pub.frost.client.core.FrostCore;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ClientRenderContext implements InputListener {
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

        ImGui.getIO().setGetClipboardTextFn(new ImStrSupplier() {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            @Override
            public String get() {
                Transferable data = clipboard.getContents(null);
                if (data != null) {
                    if (data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            return  (String) data.getTransferData(DataFlavor.stringFlavor);
                        } catch (Exception ignored) {}
                    }
                }
                return null;
            }
        });
        ImGui.getIO().setSetClipboardTextFn(new ImStrConsumer() {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            @Override
            public void accept(String str) {
                clipboard.setContents(new StringSelection(str), null);
            }
        });

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

        int keyMods = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) keyMods |= imGuiDisplay.keyToImGuiKey(Keyboard.KEY_LCONTROL);

        ImGui.newFrame();
        ImGui.getIO().setKeyMods(keyMods);
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

    @Override
    public int inputPriority() {
        return 100;
    }

    @Override
    public boolean onKey(int key, boolean state) {
        ImGui.getIO().addKeyEvent(imGuiDisplay.keyToImGuiKey(key), state);
        ImGui.getIO().addInputCharacter(Keyboard.getEventCharacter());
        return true;
    }

    @Override
    public boolean onChar(char ch) {
        ImGui.getIO().addInputCharacter(Keyboard.getEventCharacter());
        return true;
    }

    @Override
    public boolean onMouseButton(int button, boolean state) {
        imGuiDisplay.onMouseButton(button, state);
        return true;
    }

    @Override
    public boolean onMouseScroll(float scrollX, float scrollY) {
        ImGui.getIO().setMouseWheel(ImGui.getIO().getMouseWheel() + (scrollY / 120));
        ImGui.getIO().setMouseWheelH(ImGui.getIO().getMouseWheelH() + (scrollX / 120));
        return true;
    }
}
