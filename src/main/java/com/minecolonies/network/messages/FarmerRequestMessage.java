package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Ray
 */
public class FarmerRequestMessage implements IMessage, IMessageHandler<FarmerRequestMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;
    private int              mode;
    private char             type;

    public FarmerRequestMessage(){}

    public FarmerRequestMessage(Building.View building,char type, int mode)
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
    public IMessage onMessage(FarmerRequestMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony == null)
        {
            return null;
        }

        Building building = colony.getBuilding(message.buildingId);
        if (building == null)
        {
            return null;
        }
            if(building instanceof BuildingFarmer)
            {
                ((BuildingFarmer)building).set(message.type,message.mode);
            }
        return null;
    }
}
