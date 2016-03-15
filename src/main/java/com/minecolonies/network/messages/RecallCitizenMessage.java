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
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.awt.geom.Point2D;

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
                    World world = colony.getWorld();
                    ChunkCoordinates spawnPoint = Utils.scanForBlockNearPoint(world, loc.posX, loc.posY+1, loc.posZ, 1, 0, 1, 2, Blocks.air, Blocks.snow_layer);

                    //Search a close Block next to the spawnPoint to teleport in between these two blocks
                    Point2D.Double point = Utils.getClearSpace(world, spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ);

                    double x = point.getX();
                    double z = point.getY();
                    citizen.setLocationAndAngles(x, spawnPoint.posY, z, citizen.rotationYaw, citizen.rotationPitch);
                }
            }
        }

        return null;
    }
}
