package com.minecolonies.network.packets;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.Vec3Utils;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

public class OpenInventoryPacket extends AbstractPacket
{
    private final int INVENTORY_NULL = -1, INVENTORY_CITIZEN = 0, INVENTORY_CHEST = 1;
    private IInventory inventory;
    private String     name;

    private int inventoryType;

    private int  entityID;
    private Vec3 tePos;

    public OpenInventoryPacket(){}

    public OpenInventoryPacket(IInventory iinventory, String iinventoryName, int entityID)
    {
        inventory = iinventory;
        name = iinventoryName;
        this.entityID = entityID;
    }

    public OpenInventoryPacket(IInventory iinventory, String iinventoryName, Vec3 pos)
    {
        inventory = iinventory;
        name = iinventoryName;
        this.tePos = pos;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        if(inventory instanceof InventoryCitizen)
        {
            buffer.writeInt(INVENTORY_CITIZEN);
            ByteBufUtils.writeUTF8String(buffer, name);
            buffer.writeInt(entityID);
        }
        else if(inventory instanceof TileEntityChest)
        {
            buffer.writeInt(INVENTORY_CHEST);
            ByteBufUtils.writeUTF8String(buffer, name);
            NBTTagCompound compound = new NBTTagCompound();
            Vec3Utils.writeVecToNBT(compound, "pos", tePos);
            ByteBufUtils.writeTag(buffer, compound);
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
                name = ByteBufUtils.readUTF8String(buffer);
                entityID = buffer.readInt();
                break;
            case INVENTORY_CHEST:
                name = ByteBufUtils.readUTF8String(buffer);
                NBTTagCompound compound = ByteBufUtils.readTag(buffer);
                tePos = Vec3Utils.readVecFromNBT(compound, "pos");
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
                InventoryCitizen citizenInventory = ((EntityCitizen) player.worldObj.getEntityByID(entityID)).getInventory();
                if(!StringUtils.isNullOrEmpty(name)) citizenInventory.func_110133_a(name);
                player.displayGUIChest(citizenInventory);
                break;
            case INVENTORY_CHEST:
                TileEntityChest chest = (TileEntityChest) Vec3Utils.getTileEntityFromVec(player.worldObj, tePos);
                if(!StringUtils.isNullOrEmpty(name)) chest.func_145976_a(name);
                player.displayGUIChest(chest);
                break;
        }
    }
}
