package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

/**
 * Recalls the citizen to the hut
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class RecallCitizenMessage implements IMessage, IMessageHandler<RecallCitizenMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;

    public RecallCitizenMessage(){}

    public RecallCitizenMessage(BuildingWorker.View building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
    }

    @Override
    public IMessage onMessage(RecallCitizenMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            BuildingWorker building = colony.getBuilding(message.buildingId, BuildingWorker.class);
            if (building != null)
            {
                ChunkCoordinates loc = building.getLocation();
                EntityCitizen citizen = building.getWorkerEntity();
                if(citizen != null)
                {
                    citizen.setLocationAndAngles(loc.posX, loc.posY + 1, loc.posZ, citizen.rotationYaw, citizen.rotationPitch);//May need a different spot if this isn't safe
                }
            }
        }

        return null;
    }
}
