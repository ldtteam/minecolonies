package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for trying to teleport to a friends colony.
 */
public class TeleportToColonyMessage implements IMessage
{
    /**
     * Id of the colony to teleport to.
     */
    int colonyID = 0;

    /**
     * The dimenson of the target colony
     */
    int dimension = 0;

    public TeleportToColonyMessage()
    {
        super();
    }

    public TeleportToColonyMessage(final int colonyID, final int dimension)
    {
        this.colonyID = colonyID;
        this.dimension = dimension;
    }

    /**
     * Writes message data to buffer.
     *
     * @param buf network data byte buffer
     */
    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(colonyID);
        buf.writeInt(dimension);
    }

    /**
     * Reads message data from buffer.
     *
     * @param buf network data byte buffer
     */
    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        colonyID = buf.readInt();
        dimension = buf.readInt();
    }

    /**
     * Which sides is message able to be executed at.
     *
     * @return CLIENT or SERVER or null (for both)
     */
    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    /**
     * Executes message action.
     *
     * @param ctxIn           network context of incoming message
     * @param isLogicalServer whether message arrived at logical server side
     */
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

        if (colony == null)
        {
            return;
        }

        if (colony.getPermissions().getRank(ctxIn.getSender().getUniqueID()) != Rank.NEUTRAL)
        {
            TeleportHelper.colonyTeleport(ctxIn.getSender(), colony);
        }
    }
}
