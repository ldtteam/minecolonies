package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StringUtils;

public class OpenInventoryMessage implements IMessage, IMessageHandler<OpenInventoryMessage, IMessage>
{
    private static final int INVENTORY_NULL = -1, INVENTORY_CITIZEN = 0, INVENTORY_CHEST = 1;
    private String     name;

    private int inventoryType;

    private int              entityID;
    private ChunkCoordinates tePos;

    public OpenInventoryMessage(){}

    public OpenInventoryMessage(CitizenData.View citizen)
    {
        inventoryType = INVENTORY_CITIZEN;
        name = citizen.getName();
        this.entityID = citizen.getEntityId();
    }

    public OpenInventoryMessage(Building.View building)
    {
        inventoryType = INVENTORY_CHEST;
        name = ""; //builderHut.getName();
        tePos = building.getLocation();
    }


    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(inventoryType);
        ByteBufUtils.writeUTF8String(buf, name);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                buf.writeInt(entityID);
                break;
            case INVENTORY_CHEST:
                ChunkCoordUtils.writeToByteBuf(buf, tePos);
                break;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        inventoryType = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        switch(inventoryType)
        {
            case INVENTORY_CITIZEN:
                entityID = buf.readInt();
                break;
            case INVENTORY_CHEST:
                tePos = ChunkCoordUtils.readFromByteBuf(buf);
                break;
        }
    }

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
