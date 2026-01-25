package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.datalistener.ExpeditionEncounterListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The message used to synchronize global expedition type data from a server to a remote client.
 */
public class GlobalExpeditionEncounterSyncMessage implements IMessage
{
    /**
     * The buffer with the data.
     */
    private FriendlyByteBuf buffer;

    /**
     * Empty constructor used when registering the message
     */
    public GlobalExpeditionEncounterSyncMessage()
    {
        super();
    }

    /**
     * Add or Update expedition type data on the client.
     *
     * @param buf the bytebuffer.
     */
    public GlobalExpeditionEncounterSyncMessage(final FriendlyByteBuf buf)
    {
        this.buffer = new FriendlyByteBuf(buf.copy());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        buffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buffer.resetReaderIndex();
        buf.writeBytes(buffer);
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
            ExpeditionEncounterListener.readGlobalExpeditionEncounterPackets(buffer);
        }
        buffer.release();
    }
}
