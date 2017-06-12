package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.util.ClientStructureWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage implements IMessage, IMessageHandler<SaveScanMessage, IMessage>
{
    private NBTTagCompound nbttagcompound;
    private long currentMillis;

    /**
     * Public standard constructor.
     */
    public SaveScanMessage()
    {
        super();
    }

    /**
     * Send a scan compound to the client.
     *
     * @param nbttagcompound the stream.
     * @param currentMillis  long describing the current millis at create time.
     */
    public SaveScanMessage(final NBTTagCompound nbttagcompound, final long currentMillis)
    {
        this.currentMillis = currentMillis;
        this.nbttagcompound = nbttagcompound;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        int i = buffer.readerIndex();
        byte b0 = buffer.readByte();
        if (b0 != 0)
        {
            buffer.readerIndex(i);
            try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
            {
                nbttagcompound = CompressedStreamTools.read(stream, NBTSizeTracker.INFINITE);
            }
            catch (RuntimeException e)
            {
                Log.getLogger().info("Structure too big to be processed", e);
            }
            catch (IOException e)
            {
                Log.getLogger().info("Problem at retrieving structure on server.", e);
            }
        }
        currentMillis = buf.readLong();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbttagcompound);
        buf.writeLong(currentMillis);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SaveScanMessage message, final MessageContext ctx)
    {
        ClientStructureWrapper.handleSaveScanMessage(message.nbttagcompound, message.currentMillis);
        return null;
    }
}
