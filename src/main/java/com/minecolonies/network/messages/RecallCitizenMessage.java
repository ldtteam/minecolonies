package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

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

    /**
     * Object creation for the recall
     *
     * @param building      View of the building the citizen is working in
     */
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
                    World world = colony.getWorld();
                    ChunkCoordinates spawnPoint = Utils.scanForBlockNearPoint(world, new ChunkCoordinates(loc.posX, loc.posY+1, loc.posZ), 1, 0, 1, 2, Blocks.air, Blocks.snow_layer, Blocks.tallgrass, Blocks.red_flower, Blocks.yellow_flower);

                    citizen.setLocationAndAngles(spawnPoint.posX+0.5, spawnPoint.posY, spawnPoint.posZ+0.5, citizen.rotationYaw, citizen.rotationPitch);
                }
            }
        }

        return null;
    }
}
