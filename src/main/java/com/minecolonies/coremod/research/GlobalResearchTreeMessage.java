package com.minecolonies.coremod.research;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IGlobalResearchTree;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
    private PacketBuffer treeBuffer;

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
    public GlobalResearchTreeMessage(final PacketBuffer buf)
    {
        this.treeBuffer = new PacketBuffer(buf.copy());
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        treeBuffer = new PacketBuffer(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
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
