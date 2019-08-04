package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle automatic or manual housing allocation.
 */
public class ToggleMoveInMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int     colonyId;
    /**
     * Toggle the housing allocation to true or false.
     */
    private boolean toggle;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public ToggleMoveInMessage()
    {
        super();
    }

    /**
     * Creates object for the player to turn manual housing allocation or or off.
     *
     * @param colony view of the colony to read data from.
     * @param toggle toggle the housing to manually or automatically.
     */
    public ToggleMoveInMessage(@NotNull final IColonyView colony, final boolean toggle)
    {
        super();
        this.colonyId = colony.getID();
        this.toggle = toggle;
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
        toggle = buf.readBoolean();
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
        buf.writeBoolean(toggle);
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
    public void messageOnServerThread(final ToggleMoveInMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            colony.setMoveIn(message.toggle);
        }
    }
}
