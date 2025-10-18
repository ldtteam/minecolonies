package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to regenerate civilian names with mismatched name packs.
 */
public class ColonyRegenerateNamesMessage extends AbstractColonyServerMessage
{
    /**
     * Default constructor.
     */
    public ColonyRegenerateNamesMessage()
    {
        super();
    }

    /**
     * Constructs a message to regenerate names.
     *
     * @param colony the colony to regenerate names for.
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
