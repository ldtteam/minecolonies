package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class PauseCitizenMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int colonyId;
    private int colonyDim;

    /**
     * The citizen to pause.
     */
    private int citizenID;

    /**
     * Empty public constructor.
     */
    public PauseCitizenMessage()
    {
        super();
    }

    /**
     * Creates object for the player to switch pause state of a citizen.
     *
     * @param building  view of the building to read data from
     * @param citizenID the id of the citizen to fill the job.
     */
    public PauseCitizenMessage(@NotNull final AbstractBuildingView building, final int citizenID)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.colonyDim = building.getColony().getDimension();
        this.citizenID = citizenID;
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
        colonyDim = buf.readInt();
        citizenID = buf.readInt();
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
        buf.writeInt(colonyDim);
        buf.writeInt(citizenID);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, colonyDim);

        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final ICitizenData citizen = colony.getCitizenManager().getCitizen(citizenID);
            citizen.setPaused(!citizen.isPaused());
        }
    }
}
