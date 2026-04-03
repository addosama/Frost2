package pub.frost.wrappers.shared.world;

import net.minecraft.world.World;
import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.wrappers.FakeInstanceWrapper;
import pub.frost.wrappers.shared.entity.WEntity;

import java.util.ArrayList;
import java.util.List;

public class WWorld extends InstanceWrapper implements FakeInstanceWrapper<World> {
    public WWorld(Object wrappedObject) {
        super(wrappedObject);
    }

    public List<WEntity> getLoadedEntityList() {
        List<WEntity> list = new ArrayList<>();
        cast().getLoadedEntityList().forEach(
                en -> list.add(new WEntity(en))
        );
        return list;
    }
}
