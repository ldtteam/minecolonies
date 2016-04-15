package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.util.ChunkCoordUtils;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

/**
 * Changes crop percentage
 * Created: May 26, 2014
 *
 * @author Ray
 */
public class FarmerCropTypeMessage implements IMessage, IMessageHandler<FarmerCropTypeMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;

    private int              wheat;
    private int              potato;
    private int              carrot;
    private int              melon;
    private int              pumpkin;

    public FarmerCropTypeMessage(){}

    /**
     * Object for the crop type message.
     * Used to change crop percentages
     * Reads objects from {@link com.minecolonies.colony.buildings.BuildingFarmer.View}
     *
     * @param building      The view of the {@link BuildingFarmer}
     */
    public FarmerCropTypeMessage(BuildingFarmer.View building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();

        this.wheat = building.wheat;
        this.potato = building.potato;
        this.carrot = building.carrot;
        this.melon = building.melon;
        this.pumpkin = building.pumpkin;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);

        buf.writeInt(wheat);
        buf.writeInt(potato);
        buf.writeInt(carrot);
        buf.writeInt(melon);
        buf.writeInt(pumpkin);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);

        wheat = buf.readInt();
        potato = buf.readInt();
        carrot = buf.readInt();
        melon = buf.readInt();
        pumpkin = buf.readInt();
    }

    @Override
    public IMessage onMessage(FarmerCropTypeMessage message, MessageContext ctx)
    {
        if(validatePacket())
        {
            Colony colony = ColonyManager.getColony(message.colonyId);
            if (colony != null)
            {
                BuildingFarmer building = colony.getBuilding(message.buildingId, BuildingFarmer.class);
                if (building != null)
                {
                    building.wheat = wheat;
                    building.potato = potato;
                    building.carrot = carrot;
                    building.melon = melon;
                    building.pumpkin = pumpkin;
                }
            }
        }
        return null;
    }

    private boolean validatePacket()
    {
        return wheat+potato+carrot+melon+pumpkin == 100;
    }
}
