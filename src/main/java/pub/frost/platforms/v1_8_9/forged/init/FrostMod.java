package pub.frost.platforms.v1_8_9.forged.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import pub.frost.client.core.FrostCore;

@Mod(modid = "frost", useMetadata=true)
public class FrostMod {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new FrostCore().initClient();
    }
}
