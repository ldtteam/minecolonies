package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenInventoryMessage implements IMessage, IMessageHandler<OpenInventoryMessage, IMessage>
{
    private static final int              IpumpkinNVENTORY_NULL     = -1;
    private static final int              INVENTORY_CITIZEN         = 0;
    private static final int              INVENTORY_CHEST           = 1;
    private              String           name;

    private              int              inventoryType;

    private              int              entityID;
    private              BlockPos         tePos;

    public OpenInventoryMessage(){}

    /**
     * Creates an open inventory message for a citizen
     *
     * @param citizen       {@link com.minecolonies.colony.CitizenData.View}
     */
    public OpenInventoryMessage(CitizenData.View citizen)
    {
        inventoryType = INVENTORY_CITIZEN;
        name = citizen.getName();
        this.entityID = citizen.getEntityId();
    }

    /**
     * Creates an open inventory message for a building
     *
     * @param building       {@link AbstractBuilding.View}
     */
    public OpenInventoryMessage(AbstractBuilding.View building)
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
                BlockPosUtil.writeToByteBuf(buf, tePos);
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
                tePos = BlockPosUtil.readFromByteBuf(buf);
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
                InventoryCitizen citizenInventory = ((EntityCitizen) player.worldObj.getEntityByID(message.entityID)).getInventoryCitizen();
                if(!StringUtils.isNullOrEmpty(message.name))
                    citizenInventory.setCustomName(message.name);   //SetInventoryName
                player.displayGUIChest(citizenInventory);
                break;
            case INVENTORY_CHEST:
                TileEntityChest chest = (TileEntityChest) BlockPosUtil.getTileEntity(player.worldObj, message.tePos);
                if(!StringUtils.isNullOrEmpty(message.name))
                    chest.setCustomName(message.name);              //SetInventoryName
                player.displayGUIChest(chest);
                break;
        }

        return null;
    }
}
