package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle the help messages.
 */
public class ToggleHelpMessage extends AbstractColonyServerMessage
{
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
        super(colony);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.getProgressManager().togglePrintProgress();
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
