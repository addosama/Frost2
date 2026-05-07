package pub.frost.base.wrapping;

import pub.frost.wrappers.shared.block.*;
import pub.frost.wrappers.shared.client.*;
import pub.frost.wrappers.shared.client.controller.WPlayerControllerMP;
import pub.frost.wrappers.shared.client.screen.WGuiChat;
import pub.frost.wrappers.shared.client.settings.WGameSettings;
import pub.frost.wrappers.shared.entity.*;
import pub.frost.wrappers.shared.item.*;
import pub.frost.wrappers.shared.network.WNetHandlerPlayClient;
import pub.frost.wrappers.shared.network.packet.WPacket;
import pub.frost.wrappers.shared.network.packet.impl.play.c2s.*;
import pub.frost.wrappers.shared.network.packet.impl.handshake.WC2SHandshakePacket;
import pub.frost.wrappers.shared.network.packet.impl.handshake.WC2SPingPacket;
import pub.frost.wrappers.shared.network.packet.impl.handshake.WC2SServerQueryPacket;
import pub.frost.wrappers.shared.network.packet.impl.login.WC2SEncryptionResponsePacket;
import pub.frost.wrappers.shared.network.packet.impl.login.WC2SLoginStartPacket;
import pub.frost.wrappers.shared.network.packet.impl.play.s2c.WEntityPacket;
import pub.frost.wrappers.shared.network.packet.impl.play.s2c.WEntityTeleportPacket;
import pub.frost.wrappers.shared.network.packet.impl.play.s2c.WEntityVelocityPacket;
import pub.frost.wrappers.shared.player.*;
import pub.frost.wrappers.shared.tileentity.*;
import pub.frost.wrappers.shared.world.*;

public interface Wrappers {
    WBlock Block = new WBlock();
    WIBlockState IBlockState = new WIBlockState();

    WETileEntity TileEntity = new WETileEntity();
    WITileEntityProvider ITileEntityProvider = new WITileEntityProvider();

    WPlayerControllerMP PlayerControllerMP = new WPlayerControllerMP();
    WGuiChat GuiChat = new WGuiChat();
    WGameSettings GameSettings = new WGameSettings();
    WMinecraft Minecraft = new WMinecraft();

    WEntity Entity = new WEntity();
    WEntityAnimal EntityAnimal = new WEntityAnimal();
    WEntityClientPlayer EntityClientPlayer = new WEntityClientPlayer();
    WEntityLivingBase EntityLivingBase = new WEntityLivingBase();
    WEntityMob EntityMob = new WEntityMob();
    WEntityPlayer EntityPlayer = new WEntityPlayer();
    WEntityVillager EntityVillager = new WEntityVillager();

    WItem Item = new WItem();
    WItemBlock ItemBlock = new WItemBlock();
    WItemPickaxe ItemPickaxe = new WItemPickaxe();
    WItemStack ItemStack = new WItemStack();
    WItemSword ItemSword = new WItemSword();
    WItemTool ItemTool = new WItemTool();

    WC2SHandshakePacket C2SHandShakePacket = new WC2SHandshakePacket();
    WC2SPingPacket C2SPingPacket = new WC2SPingPacket();
    WC2SServerQueryPacket C2SServerQueryPacket = new WC2SServerQueryPacket();
    WC2SEncryptionResponsePacket C2SEncryptionResponsePacket = new WC2SEncryptionResponsePacket();
    WC2SLoginStartPacket C2SLoginStartPacket = new WC2SLoginStartPacket();

    WConfirmTransactionPacket ConfirmTransactionPacket = new WConfirmTransactionPacket();
    WHeldItemChangePacket HeldItemChangePacket = new WHeldItemChangePacket();
    WPlayerBlockPlacementPacket PlayerBlockPlacementPacket = new WPlayerBlockPlacementPacket();
    WPlayerDiggingPacket PlayerDiggingPacket = new WPlayerDiggingPacket();
    WPlayerPacket PlayerPacket = new WPlayerPacket();
    WPlayerPacket.WPlayerLookPacket PlayerLookPacket = new WPlayerPacket.WPlayerLookPacket();
    WPlayerPacket.WPlayerPositionPacket PlayerPositionPacket = new WPlayerPacket.WPlayerPositionPacket();
    WPlayerPacket.WPlayerPosLookPacket PlayerPosLookPacket = new WPlayerPacket.WPlayerPosLookPacket();
    WUseEntityPacket UseEntityPacket = new WUseEntityPacket();

    WEntityPacket EntityPacket = new WEntityPacket();
    WEntityPacket.WEntityLookPacket EntityLookPacket = new WEntityPacket.WEntityLookPacket();
    WEntityPacket.WEntityLookMovePacket EntityLookMovePacket = new WEntityPacket.WEntityLookMovePacket();
    WEntityPacket.WEntityRelativeMovePacket EntityRelativeMovePacket = new WEntityPacket.WEntityRelativeMovePacket();
    WEntityTeleportPacket EntityTeleportPacket = new WEntityTeleportPacket();
    WEntityVelocityPacket EntityVelocityPacket = new WEntityVelocityPacket();

    WPacket Packet = new WPacket();

    WNetHandlerPlayClient NetHandlerPlayClient = new WNetHandlerPlayClient();

    WInventoryPlayer InventoryPlayer = new WInventoryPlayer();

    WWorld World = new WWorld();
}
