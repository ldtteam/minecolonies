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
 * Save Schematic Message.
 */
public class SaveSchematicMessage implements IMessage, IMessageHandler<SaveSchematicMessage, IMessage>
{
    private NBTTagCompound nbttagcompound;
    private String         filename;

    /**
     * Public standard constructor.
     */
    public SaveSchematicMessage()
    {
        super();
    }

    /**
     * Send a schematic compound to the client.
     *
     * @param nbttagcompound the stream.
     * @param name name of the shcematic ex: stone/builder1.
     */
    public SaveSchematicMessage(final NBTTagCompound nbttagcompound, final String filename)
    {
        this.filename = filename;
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
            try (ByteBufInputStream stream = new ByteBufInputStream(buffer);)
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
        filename = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbttagcompound);
        ByteBufUtils.writeUTF8String(buf, filename);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SaveSchematicMessage message, final MessageContext ctx)
    {
        if (message.nbttagcompound != null)
        {
            ClientStructureWrapper.handleSaveSchematicMessage(message.nbttagcompound, message.filename);
        }
        return null;
    }
}
