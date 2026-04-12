package pub.frost.wrappers.shared.entity;

import net.minecraft.client.entity.EntityPlayerSP;

public class WEntityClientPlayer extends WEntityPlayer {
    public WEntityClientPlayer() {
        super(EntityPlayerSP.class);
    }
    @Override
    public EntityPlayerSP cast(Object instance) {
        return cast(instance, EntityPlayerSP.class);
    }
}
