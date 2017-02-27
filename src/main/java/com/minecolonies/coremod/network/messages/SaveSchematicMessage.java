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
    private byte [] bytes;
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
     * @param bytes byte array of the schematic.
     * @param name name of the schematic ex: stone/builder1.
     */
    public SaveSchematicMessage(final byte[] bytes, final String filename)
    {
        this.filename = filename;
        this.bytes = bytes;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        filename = ByteBufUtils.readUTF8String(buf);
        final int length = buf.readInt();
        bytes = new byte [length];
        buf.readBytes(bytes);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, filename);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SaveSchematicMessage message, final MessageContext ctx)
    {
        if (message.bytes != null)
        {
            Log.getLogger().error("Received Schematic file for " + message.filename);
            ClientStructureWrapper.handleSaveSchematicMessage(message.bytes, message.filename);
        }
        return null;
    }
}
