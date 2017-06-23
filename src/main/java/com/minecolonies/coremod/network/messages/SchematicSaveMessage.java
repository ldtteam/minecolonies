package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.util.ClientStructureWrapper;
import com.minecolonies.structures.helpers.Structure;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Save Schematic Message.
 */
public class SchematicSaveMessage implements IMessage, IMessageHandler<SchematicSaveMessage, IMessage>
{
    private byte[] data = null;
    private static final int MAX_TOTAL_SIZE = 32_767;

    /**
     * Public standard constructor.
     */
    public SchematicSaveMessage()
    {
        super();
    }

    /**
     * Send a schematic to the client.
     *
     * @param data byte array of the schematic.
     */
    public SchematicSaveMessage(final byte[] data)
    {
        this.data = data.clone();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final int length = buf.readInt();
        final byte[] compressedData = new byte[length];
        buf.readBytes(compressedData);
        data = Structure.uncompress(compressedData);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        final byte[] compressedData = Structure.compress(data);
        if (compressedData != null)
        {
            buf.capacity(compressedData.length + buf.writerIndex());
            final int maxSize = MAX_TOTAL_SIZE - Integer.SIZE / Byte.SIZE;
            if (compressedData.length > maxSize)
            {
                buf.writeInt(0);
                if (MineColonies.isClient())
                {
                    ClientStructureWrapper.sendMessageSchematicTooBig(maxSize);
                }
                else
                {
                    Log.getLogger().error("SchematicSaveMessage: schematic size too big, can not be bigger than " + maxSize + " bytes");
                }
            }
            else
            {
                buf.writeInt(compressedData.length);
                buf.writeBytes(compressedData);
            }
        }
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SchematicSaveMessage message, final MessageContext ctx)
    {
        if (!MineColonies.isClient() && !Configurations.gameplay.allowPlayerSchematics)
        {
            Log.getLogger().info("SchematicSaveMessage: custom schematic is not allowed on this server.");
            if (ctx.side.isServer())
            {
                ctx.getServerHandler().player.sendMessage(new TextComponentString("The server does not allow custom schematic!"));
            }
            return null;
        }

        final boolean schematicSent;
        if (message.data == null)
        {
            Log.getLogger().error("Received empty schematic file");
            schematicSent = false;
        }
        else
        {
            schematicSent = Structures.handleSaveSchematicMessage(message.data);
        }

        if (ctx.side.isServer())
        {
            if (schematicSent)
            {
                ctx.getServerHandler().player.sendMessage(new TextComponentString("Schematic successfully sent!"));
            }
            else
            {
                ctx.getServerHandler().player.sendMessage(new TextComponentString("Failed to send the Schematic!"));
            }
        }
        return null;
    }
}
