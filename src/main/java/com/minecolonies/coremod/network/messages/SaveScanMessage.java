package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.util.ClientStructureWrapper;
import com.minecolonies.coremod.util.Log;
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
        this.storeLocation = storeAt;
        this.nbttagcompound = nbttagcompound;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        storeLocation = ByteBufUtils.readUTF8String(buf);

        try (ByteBufInputStream stream = new ByteBufInputStream(buf);)
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

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, storeLocation);
        ByteBufUtils.writeTag(buf, nbttagcompound);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SaveScanMessage message, final MessageContext ctx)
    {
        ClientStructureWrapper.handleSaveScanMessage(message.nbttagcompound, message.storeLocation);
        return null;
    }
}
