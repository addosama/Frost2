package pub.frost.wrappers.shared.client.screen;

import net.minecraft.client.gui.GuiChat;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WGuiChat extends Wrapper implements FakeInstanceWrapper<GuiChat> {
    public WGuiChat() {
        super(GuiChat.class);
    }
}
