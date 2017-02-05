package com.minecolonies.coremod.network.messages;

import com.minecolonies.blockout.Log;
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
    private String         storeLocation;

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
     * @param storeAt        string describing where to store the scan.
     */
    public SaveScanMessage(final NBTTagCompound nbttagcompound, final String storeAt)
    {
        this.nbttagcompound = nbttagcompound;
        this.storeLocation = storeAt;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final long nbtSizeTrackerMax = 2_000_971_52L;
        final PacketBuffer pb = new PacketBuffer(buf);
        final ByteBufInputStream stream = new ByteBufInputStream(pb);

        try
        {
            nbttagcompound = CompressedStreamTools.read(stream, new NBTSizeTracker(nbtSizeTrackerMax));
        }
        catch (RuntimeException e)
        {
            Log.getLogger().info("Structure to big to be processed", e);
        }
        catch (IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch (IOException e)
            {
                Log.getLogger().info("Problem at retrieving structure on server.", e);
            }
        }

        storeLocation = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbttagcompound);
        ByteBufUtils.writeUTF8String(buf, storeLocation);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SaveScanMessage message, final MessageContext ctx)
    {
        ClientStructureWrapper.handleSaveScanMessage(message.nbttagcompound, message.storeLocation);
        return null;
    }
}
