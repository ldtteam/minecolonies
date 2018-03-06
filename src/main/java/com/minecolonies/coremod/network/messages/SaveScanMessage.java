package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.util.ClientStructureWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage extends AbstractMessage<SaveScanMessage, IMessage>
{
    private static final String TAG_MILLIS = "millies";
    private static final String TAG_SCHEMATIC = "schematic";

    private NBTTagCompound nbttagcompound;
    private long           currentMillis;

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
        final PacketBuffer buffer = new PacketBuffer(buf);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final NBTTagCompound wrapperCompound = CompressedStreamTools.readCompressed(stream);
            nbttagcompound = wrapperCompound.getCompoundTag(TAG_SCHEMATIC);
            currentMillis = wrapperCompound.getLong(TAG_MILLIS);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        final NBTTagCompound wrapperCompound = new NBTTagCompound();
        wrapperCompound.setLong(TAG_MILLIS, currentMillis);
        wrapperCompound.setTag(TAG_SCHEMATIC, nbttagcompound);

        final PacketBuffer buffer = new PacketBuffer(buf);
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            CompressedStreamTools.writeCompressed(wrapperCompound, stream);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    protected void messageOnClientThread(final SaveScanMessage message, final MessageContext ctx)
    {
        if (message.nbttagcompound != null)
        {
            ClientStructureWrapper.handleSaveScanMessage(message.nbttagcompound, message.currentMillis);
        }
    }
}
