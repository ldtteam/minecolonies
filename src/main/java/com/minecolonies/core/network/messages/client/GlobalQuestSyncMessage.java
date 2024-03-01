package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.datalistener.QuestJsonListener;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * The message used to synchronize global quest data from a server to a remote client.
 */
public class GlobalQuestSyncMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "global_quest_sync", GlobalQuestSyncMessage::new);

    /**
     * The buffer with the data.
     */
    private final FriendlyByteBuf questBuffer;

    /**
     * Add or Update QuestData on the client.
     *
     * @param buf the bytebuffer.
     */
    public GlobalQuestSyncMessage(final FriendlyByteBuf buf)
    {
        super(TYPE);
        this.questBuffer = new FriendlyByteBuf(buf.copy());
    }

    protected GlobalQuestSyncMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        questBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()));
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        questBuffer.resetReaderIndex();
        buf.writeByteArray(questBuffer.array());
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        QuestJsonListener.readGlobalQuestPackets(questBuffer);
        questBuffer.release();
    }
}
