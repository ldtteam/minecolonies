package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

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
    public void fromBytes(@NotNull final ByteBuf buf)
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
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(colonyDim);
        buf.writeInt(citizenID);
    }

    @Override
    public void messageOnServerThread(final PauseCitizenMessage message, final ServerPlayerEntity player)
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
            citizen.setPaused(!citizen.isPaused());
        }
    }
}
