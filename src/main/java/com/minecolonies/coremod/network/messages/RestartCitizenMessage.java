package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.entity.player.ServerPlayerEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class RestartCitizenMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int colonyId;
    private int colonyDim;

    /**
     * The citizen to restart.
     */
    private int citizenID;

    /**
     * Empty public constructor.
     */
    public RestartCitizenMessage()
    {
        super();
    }

    /**
     * Creates object for the player to restart a citizen (instead of the fire/hire solution).
     *
     * @param building  view of the building to read data from
     * @param citizenID the id of the citizen to fill the job.
     */
    public RestartCitizenMessage(@NotNull final AbstractBuildingView building, final int citizenID)
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

    @Override
    public void messageOnServerThread(final RestartCitizenMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.colonyDim);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final ICitizenData citizen = colony.getCitizenManager().getCitizen(message.citizenID);

            // Restart also worker building and AI
            citizen.scheduleRestart(player);
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.hiring.restartMessage", citizen.getName());
        }
    }
}
