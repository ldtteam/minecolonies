package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Message to set the colony name style.
 */
public class ColonyNameStyleMessage extends AbstractColonyServerMessage
{
    /**
     * The chosen style.
     */
    private String style;

    /**
     * Default constructor
     **/
    public ColonyNameStyleMessage()
    {
        super();
    }

    /**
     * Change the colony name style from the client to the serverside.
     *
     * @param colony      the colony the player changed the style in.
     * @param style the list of patterns they set in the banner picker
     */
    public ColonyNameStyleMessage(final IColony colony, final String style)
    {
        super(colony);
        this.style = style;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setNameStyle(style);
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeUtf(style);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        this.style = buf.readUtf(32767);
    }
}
