package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.util.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import com.minecolonies.structures.helpers.Structure;

/**
 * Save Schematic Message.
 */
public class SchematicSaveMessage implements IMessage, IMessageHandler<SchematicSaveMessage, IMessage>
{
    private byte [] bytes;

    /**
     * Public standard constructor.
     */
    public SchematicSaveMessage()
    {
        super();
    }

    /**
     * Send a schematic compound to the client.
     *
     * @param bytes byte array of the schematic.
     * @param name name of the schematic ex: huts/stone/builder1.
     */
    public SchematicSaveMessage(final byte[] bytes)
    {
        this.bytes = bytes;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final int length = buf.readInt();
        bytes = new byte [length];
        buf.readBytes(bytes);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        final int MAX_TOTAL_SIZE = 32767;
        final int MAX_SIZE = MAX_TOTAL_SIZE - Integer.SIZE / Byte.SIZE;
        if (bytes.length > MAX_SIZE)
        {
            buf.writeInt(0);
            Log.getLogger().error("SchematicSaveMessage: schematic size too big, can not be bigger than " + MAX_SIZE + " bytes");
            if (MineColonies.isClient())
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.coremod.network.messages.schematicsavemessage.toobig", MAX_SIZE);
            }
        }
        else
        {
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        }
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final SchematicSaveMessage message, final MessageContext ctx)
    {
        if (!MineColonies.isClient() && !Configurations.allowPlayerSchematics)
        {
            Log.getLogger().info("SchematicSaveMessage: custom schematic is not allowed on this server()");
            return null;
        }

        if (message.bytes == null)
        {
            Log.getLogger().error("Received empty schematic file");
        }
        else
        {
            Structures.handleSaveSchematicMessage(message.bytes);
        }
        return null;
    }
}
