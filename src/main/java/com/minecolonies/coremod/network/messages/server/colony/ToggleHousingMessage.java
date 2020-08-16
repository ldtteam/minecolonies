package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle automatic or manual housing allocation.
 */
public class ToggleHousingMessage extends AbstractColonyServerMessage
{
    /**
     * Toggle the housing allocation to true or false.
     */
    private final boolean toggle;

    /**
     * Empty public constructor.
     */
    public ToggleHousingMessage(final PacketBuffer buf)
    {
        super(buf);
        this.toggle = buf.readBoolean();
    }

    /**
     * Creates object for the player to turn manual housing allocation or or off.
     *
     * @param colony view of the colony to read data from.
     * @param toggle toggle the housing to manually or automatically.
     */
    public ToggleHousingMessage(@NotNull final IColonyView colony, final boolean toggle)
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
        colony.setManualHousing(toggle);
    }
}
