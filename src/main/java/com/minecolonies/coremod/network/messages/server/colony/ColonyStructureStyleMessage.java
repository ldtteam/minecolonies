package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to set the colony default structure style.
 */
public class ColonyStructureStyleMessage extends AbstractColonyServerMessage
{
    /**
     * The chosen pack.
     */
    private String pack;

    /**
     * Default constructor
     **/
    public ColonyStructureStyleMessage()
    {
        super();
    }

    /**
     * Change the colony default pack from the client to the serverside.
     *
     * @param colony the colony the player changed the pack in.
     * @param pack   the pack name,
     */
    public ColonyStructureStyleMessage(final IColony colony, final String pack)
    {
        super(colony);
        this.pack = pack;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setStructurePack(pack);
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeUtf(pack);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        this.pack = buf.readUtf(32767);
    }
}

