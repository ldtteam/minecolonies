package com.minecolonies.network.packets;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StringUtils;

public class OpenInventoryPacket extends AbstractPacket
{
    private final int INVENTORY_NULL = -1, INVENTORY_CITIZEN = 0, INVENTORY_CHEST = 1;
    private IInventory inventory;
    private String name;

    private int inventoryType;
    private int entityId;
    private int[] pos = new int[3];

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
            buffer.writeInt(inventoryType = INVENTORY_CITIZEN);
            buffer.writeInt(entityId = ((InventoryCitizen) inventory).citizen.getEntityId());

            if(StringUtils.isNullOrEmpty(name)) name = "";
            ByteBufUtils.writeUTF8String(buffer, name);
        }
        else if(inventory instanceof TileEntityChest)
        {
            TileEntityChest chest = (TileEntityChest) inventory;
            buffer.writeInt(inventoryType = INVENTORY_CHEST);
            buffer.writeInt(pos[0] = chest.xCoord);
            buffer.writeInt(pos[1] = chest.yCoord);
            buffer.writeInt(pos[2] = chest.zCoord);

            if(StringUtils.isNullOrEmpty(name)) name = "";
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
                pos = new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()};
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
                TileEntityChest chest = (TileEntityChest) player.worldObj.getTileEntity(pos[0], pos[1], pos[2]);
                if(!StringUtils.isNullOrEmpty(name)) chest.func_145976_a(name);
                player.displayGUIChest(chest);
                break;
        }
    }
}
