package com.minecolonies.network.packets;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StringUtils;

public class OpenInventoryMessage implements IMessage
{
    private static final int INVENTORY_NULL = -1, INVENTORY_CITIZEN = 0, INVENTORY_CHEST = 1;
    private IInventory inventory;
    private String     name;

    private int inventoryType;

    private int              entityID;
    private ChunkCoordinates tePos;

    public OpenInventoryMessage(){}

    public OpenInventoryMessage(IInventory iinventory, String iinventoryName, int entityID)
    {
        inventory = iinventory;
        name = iinventoryName;
        this.entityID = entityID;
    }

    public OpenInventoryMessage(IInventory iinventory, String iinventoryName, ChunkCoordinates pos)
    {
        inventory = iinventory;
        name = iinventoryName;
        tePos = pos;
    }


    @Override
    public void toBytes(ByteBuf buf)
    {
        if(inventory instanceof InventoryCitizen)
        {
            buf.writeInt(INVENTORY_CITIZEN);
            ByteBufUtils.writeUTF8String(buf, name);
            buf.writeInt(entityID);
        }
        else if(inventory instanceof TileEntityChest)
        {
            buf.writeInt(INVENTORY_CHEST);
            ByteBufUtils.writeUTF8String(buf, name);
            NBTTagCompound compound = new NBTTagCompound();
            ChunkCoordUtils.writeToNBT(compound, "pos", tePos);
            ByteBufUtils.writeTag(buf, compound);
        }
        else
        {
            buf.writeInt(INVENTORY_NULL);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        inventoryType = buf.readInt();
        switch(inventoryType)
        {
            case INVENTORY_CITIZEN:
                name = ByteBufUtils.readUTF8String(buf);
                entityID = buf.readInt();
                break;
            case INVENTORY_CHEST:
                name = ByteBufUtils.readUTF8String(buf);
                NBTTagCompound compound = ByteBufUtils.readTag(buf);
                tePos = ChunkCoordUtils.readFromNBT(compound, "pos");
                break;
        }
    }

    public static class Handler implements IMessageHandler<OpenInventoryMessage, IMessage>
    {
        @Override
        public IMessage onMessage(OpenInventoryMessage message, MessageContext ctx)
        {
            EntityPlayer player = ctx.getServerHandler().playerEntity;

            switch(message.inventoryType)
            {
                case INVENTORY_CITIZEN:
                    InventoryCitizen citizenInventory = ((EntityCitizen) player.worldObj.getEntityByID(message.entityID)).getInventory();
                    if(!StringUtils.isNullOrEmpty(message.name)) citizenInventory.func_110133_a(message.name);
                    player.displayGUIChest(citizenInventory);
                    break;
                case INVENTORY_CHEST:
                    TileEntityChest chest = (TileEntityChest) ChunkCoordUtils.getTileEntity(player.worldObj, message.tePos);
                    if(!StringUtils.isNullOrEmpty(message.name)) chest.func_145976_a(message.name);
                    player.displayGUIChest(chest);
                    break;
            }

            return null;
        }
    }
}
