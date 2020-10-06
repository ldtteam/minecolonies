package com.minecolonies.coremod.network.messages.splitting;

import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.NetworkChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class SplitPacketMessage implements IMessage
{
    private int     communicationId = -1;
    private int     packetIndex     = -1;
    private boolean terminator      = false;
    private int innerMessageId = -1;
    private byte[]  payload;

    public SplitPacketMessage()
    {
    }

    public SplitPacketMessage(final int communicationId, final int innerMessageId, final byte[] payload, final int packetIndex, final boolean terminator)
    {
        this.innerMessageId = innerMessageId;
        this.payload = payload;
        this.terminator = terminator;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeVarInt(this.communicationId);
        buf.writeVarInt(this.packetIndex);
        buf.writeBoolean(this.terminator);
        buf.writeVarInt(this.innerMessageId);
        buf.writeBytes(this.payload);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.communicationId = buf.readVarInt();
        this.packetIndex = buf.readVarInt();
        this.terminator = buf.readBoolean();
        this.innerMessageId = buf.readVarInt();
        this.payload = buf.readByteArray();
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        try
        {
            synchronized (Network.getNetwork().getMessageCache()) {
                Network.getNetwork().getMessageCache().get(this.communicationId, Maps::newConcurrentMap).put(this.packetIndex, this.payload);
            }

            if (!this.terminator)
            {
                return;
            }

            //No need to sync again, since we are now the last packet to arrive.
            final byte[] packetData = Network.getNetwork().getMessageCache().get(this.communicationId, Maps::newConcurrentMap).entrySet()
              .stream()
              .sorted(Map.Entry.comparingByKey())
              .map(Map.Entry::getValue)
              .reduce(new byte[0], Bytes::concat);

            final NetworkChannel.NetworkingMessageEntry<?> messageEntry = Network.getNetwork().getMessagesTypes().get(this.innerMessageId);
            final IMessage message = messageEntry.getCreator().get();

            final ByteBuf buffer = Unpooled.wrappedBuffer(packetData);
            message.fromBytes(new PacketBuffer(buffer));
            buffer.release();

            message.onExecute(ctxIn, isLogicalServer);

            final LogicalSide packetOrigin = ctxIn.getDirection().getOriginationSide();
            if (message.getExecutionSide() != null && packetOrigin.equals(message.getExecutionSide()))
            {
                Log.getLogger().warn("Receving {} at wrong side!", message.getClass().getName());
                return;
            }
            // boolean param MUST equals true if packet arrived at logical server
            ctxIn.enqueueWork(() -> message.onExecute(ctxIn, packetOrigin.equals(LogicalSide.CLIENT)));
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error("Failed to handle split packet.", e);
        }
    }
}
