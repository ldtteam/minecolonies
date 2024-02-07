package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to trigger a response handler close on the server side.
 */
public class InteractionClose extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "interaction_close", InteractionClose::new);

    /**
     * Id of the citizen.
     */
    private final int citizenId;

    /**
     * The key of the handler to trigger.
     */
    private final Component key;

    /**
     * Trigger the server response handler.
     *
     * @param colonyId  the colony id.
     * @param citizenId the citizen id.
     * @param dimension the dimension the colony and citizen are in.
     * @param key       the key of the handler.
     */
    public InteractionClose(
      final int colonyId,
      final int citizenId,
      final ResourceKey<Level> dimension,
      @NotNull final Component key)
    {
        super(TYPE, dimension, colonyId);
        this.citizenId = citizenId;
        this.key = key;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected InteractionClose(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.citizenId = buf.readInt();
        this.key = buf.readComponent();
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
        buf.writeInt(this.citizenId);
        buf.writeComponent(key);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData == null)
        {
            citizenData = colony.getVisitorManager().getVisitor(citizenId);
        }

        if (citizenData != null && player != null)
        {
            citizenData.onInteractionClosed(key, player);
        }
    }
}


