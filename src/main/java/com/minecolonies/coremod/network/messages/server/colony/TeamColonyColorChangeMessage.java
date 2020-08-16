package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages changing the team color of the colony.
 */
public class TeamColonyColorChangeMessage extends AbstractColonyServerMessage
{
    /**
     * The color to set.
     */
    private final int colorOrdinal;

    /**
     * Empty public constructor.
     */
    public TeamColonyColorChangeMessage(final PacketBuffer buf)
    {
        super(buf);
        colorOrdinal = buf.readInt();
    }

    /**
     * Creates object for the player to handle the color
     *
     * @param colorOrdinal the color to set.
     * @param building     view of the building to read data from
     */
    public TeamColonyColorChangeMessage(final int colorOrdinal, @NotNull final IBuildingView building)
    {
        super(building.getColony());
        this.colorOrdinal = colorOrdinal;
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colorOrdinal);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.setColonyColor(TextFormatting.values()[colorOrdinal]);
    }
}
