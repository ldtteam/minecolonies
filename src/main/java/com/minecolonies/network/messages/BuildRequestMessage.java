package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

import java.util.UUID;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage implements IMessage
{
    private UUID             colonyId;
    private ChunkCoordinates buildingId;
    private int              mode;


    public static final int BUILD  = 0;
    public static final int REPAIR = 1;


    public BuildRequestMessage(){}

    public BuildRequestMessage(Building.View building, int mode)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mode = mode;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(colonyId.getMostSignificantBits());
        buf.writeLong(colonyId.getLeastSignificantBits());
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = new UUID(buf.readLong(), buf.readLong());
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        mode = buf.readInt();
    }

    public static class Handler implements IMessageHandler<BuildRequestMessage, IMessage>
    {
        @Override
        public IMessage onMessage(BuildRequestMessage message, MessageContext ctx)
        {
            Colony colony = ColonyManager.getColonyById(message.colonyId);
            if (colony == null)
            {
                return null;
            }

            Building building = colony.getBuilding(message.buildingId);
            if (building == null)
            {
                return null;
            }

//            switch(message.mode)
//            {
//                case BUILD:
//                    tileEntity.requestBuilding();
//                    break;
//                case REPAIR:
//                    tileEntity.requestRepair();
//                    break;
//            }

            return null;
        }
    }
}
