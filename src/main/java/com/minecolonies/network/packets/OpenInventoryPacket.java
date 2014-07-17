package com.minecolonies.network.packets;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.Vec3Utils;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

public class OpenInventoryPacket extends AbstractPacket
{
    private final int INVENTORY_NULL = -1, INVENTORY_CITIZEN = 0, INVENTORY_CHEST = 1;
    private IInventory inventory;
    private String     name;

    private int inventoryType;
    private int entityId;
    private Vec3 pos;

    public OpenInventoryPacket(){}

    public OpenInventoryPacket(IInventory iinventory)
    {
        this(iinventory, "");
    }

    public OpenInventoryPacket(IInventory iinventory, String iinventoryName)
    {
        inventory = iinventory;
        name = iinventoryName;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        if(inventory instanceof InventoryCitizen)
        {
            buffer.writeInt(INVENTORY_CITIZEN);
            buffer.writeInt(((InventoryCitizen) inventory).citizen.getEntityId());
            ByteBufUtils.writeUTF8String(buffer, name);
        }
        else if(inventory instanceof TileEntityChest)
        {
            TileEntityChest chest = (TileEntityChest) inventory;
            buffer.writeInt(INVENTORY_CHEST);
            buffer.writeInt(chest.xCoord);
            buffer.writeInt(chest.yCoord);
            buffer.writeInt(chest.zCoord);
            ByteBufUtils.writeUTF8String(buffer, name);
        }
        else
        {
            buffer.writeInt(INVENTORY_NULL);
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        inventoryType = buffer.readInt();
        switch(inventoryType)
        {
            case INVENTORY_CITIZEN:
                entityId = buffer.readInt();
                name = ByteBufUtils.readUTF8String(buffer);
                break;
            case INVENTORY_CHEST:
                pos = Vec3.createVectorHelper(buffer.readInt(), buffer.readInt(), buffer.readInt());
                name = ByteBufUtils.readUTF8String(buffer);
                break;
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player){}

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        switch(inventoryType)
        {
            case INVENTORY_CITIZEN:
                InventoryCitizen citizenInventory = ((EntityCitizen) player.worldObj.getEntityByID(entityId)).getInventory();
                if(!StringUtils.isNullOrEmpty(name)) citizenInventory.func_110133_a(name);
                player.displayGUIChest(citizenInventory);
                break;
            case INVENTORY_CHEST:
                TileEntityChest chest = (TileEntityChest) Vec3Utils.getTileEntityFromVec(player.worldObj, pos);
                if(!StringUtils.isNullOrEmpty(name)) chest.func_145976_a(name);
                player.displayGUIChest(chest);
                break;
        }
    }
}
