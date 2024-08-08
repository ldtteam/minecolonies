package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to trigger a response handler on the server side.
 */
public class InteractionResponse extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "interaction_response", InteractionResponse::new);

    /**
     * Id of the citizen.
     */
    private final int citizenId;

    /**
     * The key of the handler to trigger.
     */
    private final Component key;

    /**
     * The chosen response.
     */
    private final int responseId;

    /**
     * Trigger the server response handler.
     *
     * @param colonyId   the colony id.
     * @param citizenId  the citizen id.
     * @param dimension  the dimension the colony and citizen are in.
     * @param key        the key of the handler.
     * @param responseId the response to trigger.
     */
    public InteractionResponse(
      final int colonyId,
      final int citizenId,
      final ResourceKey<Level> dimension,
      @NotNull final Component key,
      final int responseId)
    {
        super(TYPE, dimension, colonyId);
        this.citizenId = citizenId;
        this.key = key;
        this.responseId = responseId;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected InteractionResponse(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.citizenId = buf.readInt();
        this.key = Utils.deserializeCodecMess(ComponentSerialization.STREAM_CODEC, buf);
        this.responseId = buf.readInt();
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
        buf.writeInt(this.citizenId);
        Utils.serializeCodecMess(ComponentSerialization.STREAM_CODEC, buf, key);
        buf.writeInt(responseId);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData == null)
        {
            citizenData = colony.getVisitorManager().getVisitor(citizenId);
        }

        if (citizenData != null && player != null)
        {
            citizenData.onResponseTriggered(key, responseId, player);
        }
    }
}


