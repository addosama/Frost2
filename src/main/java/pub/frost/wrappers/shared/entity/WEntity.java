package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.Entity;
import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WEntity extends InstanceWrapper implements FakeInstanceWrapper<Entity> {
    public WEntity(Object wrappedObject) {
        super(wrappedObject);
    }

    public boolean isSprinting() {
        return cast().isSprinting();
    }
    public void setSprinting(boolean sprinting) {
        cast().setSprinting(sprinting);
    }
}
