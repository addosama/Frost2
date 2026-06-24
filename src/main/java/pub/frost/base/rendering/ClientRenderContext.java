package pub.frost.base.rendering;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiConfigFlags;
import lombok.Getter;
import loutre.imgui.lwjgl2.ImGuiDisplay;
import loutre.imgui.lwjgl2.ImGuiLWJGL2;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventInput;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

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

    @EventHandler(priority = -100)
    private void onInput(EventInput event) {
        if (event.getType() == InputDevice.KEYBOARD) {
            ImGuiIO io = ImGui.getIO();

            processKey(Keyboard.getEventKey());
            io.addInputCharacter(Keyboard.getEventCharacter());
        }
        else {
            int mouseButton = Mouse.getEventButton();
            int mouseWheel = Mouse.getDWheel();
            if (mouseWheel != 0)
                imGuiDisplay.onMouseWheel(mouseWheel);
            if (mouseButton != -1) {
                imGuiDisplay.onMouseButton(
                        mouseButton, Mouse.getEventButtonState()
                );
            }
        }
    }

    private void processKey(int glKey) {
        ImGui.getIO().addKeyEvent(imGuiDisplay.keyToImGuiKey(glKey), Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent());
        System.out.printf("Key %s, %s\n", Keyboard.getKeyName(Keyboard.getEventKey()), Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent());
    }
}
