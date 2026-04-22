package pub.frost.base.wrapping;

import pub.frost.wrappers.shared.block.*;
import pub.frost.wrappers.shared.client.*;
import pub.frost.wrappers.shared.client.screen.WGuiChat;
import pub.frost.wrappers.shared.client.settings.WGameSettings;
import pub.frost.wrappers.shared.entity.*;
import pub.frost.wrappers.shared.item.*;
import pub.frost.wrappers.shared.network.WNetHandlerPlayClient;
import pub.frost.wrappers.shared.network.packet.WPacket;
import pub.frost.wrappers.shared.network.packet.impl.c2s.*;
import pub.frost.wrappers.shared.network.packet.impl.s2c.WEntityPacket;
import pub.frost.wrappers.shared.network.packet.impl.s2c.WEntityTeleportPacket;
import pub.frost.wrappers.shared.player.*;
import pub.frost.wrappers.shared.world.*;

public interface Wrappers {
    WBlock Block = new WBlock();
    WIBlockState IBlockState = new WIBlockState();

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

    WPacket Packet = new WPacket();

    WNetHandlerPlayClient NetHandlerPlayClient = new WNetHandlerPlayClient();

    WInventoryPlayer InventoryPlayer = new WInventoryPlayer();

    WWorld World = new WWorld();
}
