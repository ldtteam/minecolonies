package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle the help messages.
 */
public class ToggleHelpMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public ToggleHelpMessage()
    {
        super();
    }

    /**
     * Creates object for the player to turn help messages or or off.
     *
     * @param colony view of the colony to read data from.
     */
    public ToggleHelpMessage(@NotNull final IColonyView colony)
    {
        super();
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
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
        buf.writeInt(dimension);
    }

    /**
     * Executes the message on the server thread.
     * Only if the player has the permission, toggle message.
     *
     * @param message the original message.
     * @param player  the player associated.
     */
    @Override
    public void messageOnServerThread(final ToggleHelpMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            colony.getProgressManager().togglePrintProgress();
        }
    }
}
