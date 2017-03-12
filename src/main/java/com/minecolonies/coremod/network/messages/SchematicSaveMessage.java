package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
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
    private String  filename;

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
    public SchematicSaveMessage(final byte[] bytes, final String filename)
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

    private void handleSaveSchematicMessage(final byte[] bytes, final String name)
    {
        final File schematicsFolder = Structure.getCachedSchematicsFolder();

        final String md5 = Structure.calculateMD5(bytes);

        if (md5 != null)
        {
            final File schematicFile = new File(schematicsFolder.toPath() + "/" + md5 + ".nbt");
            checkDirectory(schematicFile.getParentFile());
            try (OutputStream outputstream = new FileOutputStream(schematicFile))
            {
                outputstream.write(bytes);
                Structures.addMD5ToCache(md5);
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("Exception while trying to save a schematic.", e);
                return;
            }
        }
        else
        {
           Log.getLogger().info("ClientStructureWrapper.handleSaveSchematicMessage: Could not calculate the MD5 hash");
           return;
        }

        //Let the gui know we just save a schematic
        ColonyManager.setSchematicDownloaded(true);
    }

    private static void checkDirectory(@NotNull final File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
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
            Log.getLogger().error("Received empty schematic file for " + message.filename);
        }
        else
        {
            Log.getLogger().info("Received Schematic file for " + message.filename);
            //ClientStructureWrapper.handleSaveSchematicMessage(message.bytes, message.filename);
            handleSaveSchematicMessage(message.bytes, message.filename);
        }
        return null;
    }
}
