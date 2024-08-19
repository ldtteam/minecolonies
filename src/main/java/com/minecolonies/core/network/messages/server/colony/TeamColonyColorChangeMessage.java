package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages changing the team color of the colony.
 */
public class TeamColonyColorChangeMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "team_colony_color_change", TeamColonyColorChangeMessage::new);

    /**
     * The color to set.
     */
    private final int colorOrdinal;

    /**
     * Creates object for the player to handle the color
     *
     * @param colorOrdinal the color to set.
     * @param building     view of the building to read data from
     */
    public TeamColonyColorChangeMessage(final int colorOrdinal, @NotNull final IBuildingView building)
    {
        super(TYPE, building.getColony());
        this.colorOrdinal = colorOrdinal;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected TeamColonyColorChangeMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        colorOrdinal = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(colorOrdinal);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.setColonyColor(ChatFormatting.values()[colorOrdinal]);
        try
        {
            NeoForge.EVENT_BUS.post(new ColonyInformationChangedEvent(colony, ColonyInformationChangedEvent.Type.TEAM_COLOR));
        }
        catch (final Exception e)
        {
            Log.getLogger().error("Error during ColonyInformationChangedEvent", e);
        }
    }
}
