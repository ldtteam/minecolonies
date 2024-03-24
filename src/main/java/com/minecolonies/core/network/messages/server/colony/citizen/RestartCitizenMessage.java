package com.minecolonies.core.network.messages.server.colony.citizen;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_CITIZEN_RESTART_SCHEDULED;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class RestartCitizenMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "restart_citizen", RestartCitizenMessage::new);

    /**
     * The citizen to restart.
     */
    private final int citizenID;

    /**
     * Creates object for the player to restart a citizen (instead of the fire/hire solution).
     *
     * @param building  view of the building to read data from
     * @param citizenID the id of the citizen to fill the job.
     */
    public RestartCitizenMessage(@NotNull final AbstractBuildingView building, final int citizenID)
    {
        super(TYPE, building.getColony());
        this.citizenID = citizenID;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected RestartCitizenMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        citizenID = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeInt(citizenID);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCivilian(citizenID);

        // Restart also worker building and AI
        citizen.scheduleRestart(player);
        MessageUtils.format(MESSAGE_CITIZEN_RESTART_SCHEDULED, citizen.getName()).sendTo(player);
    }
}
