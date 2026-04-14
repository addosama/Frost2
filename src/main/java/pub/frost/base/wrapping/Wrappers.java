package pub.frost.base.wrapping;

import pub.frost.wrappers.shared.block.*;
import pub.frost.wrappers.shared.client.*;
import pub.frost.wrappers.shared.entity.*;
import pub.frost.wrappers.shared.item.*;
import pub.frost.wrappers.shared.network.packet.impl.c2s.*;
import pub.frost.wrappers.shared.player.*;
import pub.frost.wrappers.shared.world.*;

public interface Wrappers {
    WBlock Block = new WBlock();
    WIBlockState IBlockState = new WIBlockState();

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

    WInventoryPlayer InventoryPlayer = new WInventoryPlayer();

    WWorld World = new WWorld();
}
