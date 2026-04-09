package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.player.EntityPlayer;

public class WEntityPlayer extends WEntityLivingBase {
    public WEntityPlayer(Object obj) {
        super(obj);
    }

    public boolean isSpectator() {
        return cast().isSpectator();
    }

    @Override
    public EntityPlayer cast() {
        return cast(getWrappedObject(), EntityPlayer.class);
    }
}
