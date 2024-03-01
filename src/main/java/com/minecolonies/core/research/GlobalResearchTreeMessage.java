package com.minecolonies.core.research;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * The message used to synchronize global research trees from a server to a remote client.
 */
public class GlobalResearchTreeMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "global_research_tree", GlobalResearchTreeMessage::new, true, false);

    /**
     * The buffer with the data.
     */
    private final FriendlyByteBuf treeBuffer;

    /**
     * Add or Update a GlobalResearchTree on the client.
     *
     * @param buf               the bytebuffer.
     */
    public GlobalResearchTreeMessage(final FriendlyByteBuf buf)
    {
        super(TYPE);
        this.treeBuffer = new FriendlyByteBuf(buf.copy());
    }

    protected GlobalResearchTreeMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        treeBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()));
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeByteArray(treeBuffer.array());
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        IGlobalResearchTree.getInstance().handleGlobalResearchTreeMessage(treeBuffer);
    }
}
