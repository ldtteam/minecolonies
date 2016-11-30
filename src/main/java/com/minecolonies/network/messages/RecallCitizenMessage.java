package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import com.minecolonies.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Recalls the citizen to the hut.
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class RecallCitizenMessage extends AbstractMessage<RecallCitizenMessage, IMessage>
{
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;
    private int      colonyId;
    private BlockPos buildingId;

    /**
     * Empty public constructor.
     */
    public RecallCitizenMessage()
    {
        super();
    }

    /**
     * Object creation for the recall.
     *
     * @param building View of the building the citizen is working in.
     */
    public RecallCitizenMessage(@NotNull final AbstractBuildingWorker.View building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void messageOnServerThread(final RecallCitizenMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingWorker building = colony.getBuilding(message.buildingId, AbstractBuildingWorker.class);
            if (building != null)
            {
                final BlockPos loc = building.getLocation();

                @Nullable final CitizenData citizenData = building.getWorker();

                if (citizenData != null)
                {
                    @Nullable EntityCitizen citizen = building.getWorkerEntity();
                    //Try to retrieve the citizen.
                    if (citizen == null)
                    {
                        Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                        colony.spawnCitizen(citizenData);
                    }

                    citizen = citizenData.getCitizenEntity();
                    if (citizen != null)
                    {
                        @Nullable final World world = colony.getWorld();
                        @Nullable final BlockPos spawnPoint =
                          Utils.scanForBlockNearPoint(world, loc, 1, 0, 1, 2, Blocks.AIR, Blocks.SNOW_LAYER, Blocks.TALLGRASS, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER);

                        citizen.setLocationAndAngles(
                          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
                          spawnPoint.getY(),
                          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
                          citizen.rotationYaw,
                          citizen.rotationPitch);
                        citizen.getNavigator().clearPathEntity();
                    }
                }
            }
        }
    }
}
