package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle automatic or manual job allocation.
 */
public class ToggleJobMessage extends AbstractColonyServerMessage
{
    /**
     * Toggle the job allocation to true or false.
     */
    private final boolean toggle;

    /**
     * Empty public constructor.
     */
    public ToggleJobMessage(final PacketBuffer buf)
    {
        super(buf);
        this.toggle = buf.readBoolean();
    }

    /**
     * Creates object for the player to turn manual allocation or or off.
     *
     * @param colony view of the colony to read data from.
     * @param toggle toggle the job to manually or automatically.
     */
    public ToggleJobMessage(@NotNull final IColonyView colony, final boolean toggle)
    {
        super(colony);
        this.toggle = toggle;
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeBoolean(toggle);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.setManualHiring(toggle);
    }
}
