package com.minecolonies.core.research;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IGlobalResearchTree;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The message used to synchronize global research trees from a server to a remote client.
 */
public class GlobalResearchTreeMessage implements IMessage
{

    /**
     * The buffer with the data.
     */
    private FriendlyByteBuf treeBuffer;

    /**
     * Empty constructor used when registering the message
     */
    public GlobalResearchTreeMessage()
    {
        super();
    }

    /**
     * Add or Update a GlobalResearchTree on the client.
     *
     * @param buf               the bytebuffer.
     */
    public GlobalResearchTreeMessage(final FriendlyByteBuf buf)
    {
        this.treeBuffer = new FriendlyByteBuf(buf.copy());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        treeBuffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBytes(treeBuffer);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (Minecraft.getInstance().level != null)
        {
            IGlobalResearchTree.getInstance().handleGlobalResearchTreeMessage(treeBuffer);
        }
        treeBuffer.release();
    }
}
