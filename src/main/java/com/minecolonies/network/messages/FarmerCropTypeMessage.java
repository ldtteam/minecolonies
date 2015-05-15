package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
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
    private int              mode;
    private char             type;

    public FarmerCropTypeMessage(){}

    public FarmerCropTypeMessage(Building.View building, char type, int mode)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.type = type;
        this.mode = mode;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
        buf.writeChar(type);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        mode = buf.readInt();
        type = buf.readChar();
    }

    @Override
    public IMessage onMessage(FarmerCropTypeMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            BuildingFarmer building = colony.getBuilding(message.buildingId, BuildingFarmer.class);
            if (building != null)
            {
                building.set(message.type, message.mode);
            }
        }
        return null;
    }
}
