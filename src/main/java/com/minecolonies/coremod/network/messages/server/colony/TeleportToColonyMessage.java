package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for trying to teleport to a friends colony.
 */
public class TeleportToColonyMessage extends AbstractColonyServerMessage
{
    public TeleportToColonyMessage()
    {
        super();
    }

    public TeleportToColonyMessage(final int dimensionId, final int colonyId)
    {
        super(dimensionId, colonyId);
    }

    @Nullable
    @Override
    public Action permissionNeeded()
    {
        return null;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (ctxIn.getSender() == null)
        {
            return;
        }

        if (colony.getPermissions().getRank(ctxIn.getSender().getUniqueID()) != Rank.NEUTRAL)
        {
            TeleportHelper.colonyTeleport(ctxIn.getSender(), colony);
        }
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }
}
