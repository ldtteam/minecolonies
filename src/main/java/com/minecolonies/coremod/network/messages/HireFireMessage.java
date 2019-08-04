package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class HireFireMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * The buildings position.
     */
    private BlockPos buildingId;

    /**
     * If hiring (true) else firing.
     */
    private boolean hire;

    /**
     * The citizen to hire/fire.
     */
    private int citizenID;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public HireFireMessage()
    {
        super();
    }

    /**
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building  view of the building to read data from
     * @param hire      hire or fire the citizens
     * @param citizenID the id of the citizen to fill the job.
     */
    public HireFireMessage(@NotNull final AbstractBuildingView building, final boolean hire, final int citizenID)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.hire = hire;
        this.citizenID = citizenID;
        this.dimension = building.getColony().getDimension();
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        hire = buf.readBoolean();
        citizenID = buf.readInt();
        dimension = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeBoolean(hire);
        buf.writeInt(citizenID);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final HireFireMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final ICitizenData citizen = colony.getCitizenManager().getCitizen(message.citizenID);
            citizen.setPaused(false);
            if (message.hire)
            {

                colony.getBuildingManager().getBuilding(message.buildingId).assignCitizen(citizen);
            }
            else
            {
                colony.getBuildingManager().getBuilding(message.buildingId).removeCitizen(citizen);
            }
        }
    }
}
