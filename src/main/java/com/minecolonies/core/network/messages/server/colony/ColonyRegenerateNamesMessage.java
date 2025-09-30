package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to regenerate all citizen names in a colony.
 */
public class ColonyRegenerateNamesMessage extends AbstractColonyServerMessage
{
    /**
     * Default constructor
     **/
    public ColonyRegenerateNamesMessage()
    {
        super();
    }

    /**
     * Regenerate all names in the colony.
     *
     * @param colony the colony to regenerate names for
     */
    public ColonyRegenerateNamesMessage(final IColony colony)
    {
        super(colony);
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.regenerateAllNames();
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
    }
}
