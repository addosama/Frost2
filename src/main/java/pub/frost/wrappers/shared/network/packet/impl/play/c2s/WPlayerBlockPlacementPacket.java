package pub.frost.wrappers.shared.network.packet.impl.play.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.EnumDirection;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WPlayerBlockPlacementPacket extends WPacket {
    public WPlayerBlockPlacementPacket() {
        super(C08PacketPlayerBlockPlacement.class);
    }

    public Object build(Object itemStack) {
        return new C08PacketPlayerBlockPlacement((ItemStack) itemStack);
    }

    public Object build(BlockPosition pos, EnumDirection face, Object itemStack, float hitX, float hitY, float hitZ) {
        return new C08PacketPlayerBlockPlacement(
                new BlockPos(pos.x, pos.y, pos.z),
                face.getIndex(),
                (ItemStack) itemStack,
                hitX, hitY, hitZ
        );
    }
}
