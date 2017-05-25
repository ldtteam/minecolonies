package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.structures.helpers.Structure;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * Request a schematic from the server.
 * Created: Feb 07, 2017
 *
 * @author xavier
 */
public class SchematicRequestMessage extends AbstractMessage<SchematicRequestMessage, IMessage>
{

    private String filename;

    /**
     * Empty constructor used when registering the message.
     */
    public SchematicRequestMessage()
    {
        super();
    }

    /**
     * Creates a Schematic request message.
     *
     * @param filename of the structure based on schematics folder
     *                 Ex: schematics/stone/Builder1.nbt
     */
    public SchematicRequestMessage(final String filename)
    {
        super();
        this.filename = filename;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        filename = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, filename);
    }

    @Override
    public void messageOnServerThread(final SchematicRequestMessage message, final EntityPlayerMP player)
    {
        final InputStream stream = Structure.getStream(message.filename);

        if (stream == null)
        {
            Log.getLogger().error("SchematicRequestMessage: file \"" + message.filename + "\" not found");
        }
        else
        {
            Log.getLogger().info("Request: player " + player.getName() + " is requesting schematic " + message.filename);
            byte[] schematic = Structure.getStreamAsByteArray(stream);
            MineColonies.getNetwork().sendTo(new SchematicSaveMessage(schematic), player);
        }
    }
}
