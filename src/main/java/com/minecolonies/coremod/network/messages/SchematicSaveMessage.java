package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.structures.helpers.Structure;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.minecolonies.api.util.constant.Constants.MAX_AMOUNT_OF_PIECES;
import static com.minecolonies.api.util.constant.Constants.MAX_MESSAGE_SIZE;

/**
 * Save Schematic Message.
 */
public class SchematicSaveMessage extends AbstractMessage<SchematicSaveMessage, IMessage>
{
    /**
     * The schematic data.
     */
    private              byte[] data           = null;

    /**
     * The amount of pieces.
     */
    private int pieces;

    /**
     * The current piece.
     */
    private int piece;

    /**
     * The UUID.
     */
    private UUID id;

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
    /**
     * Send a schematic between client and server or server and client.
     * @param data the schematic.
     * @param id the unique id.
     * @param pieces the amount of pieces.
     * @param piece the current piece.
     */
    public SchematicSaveMessage(final byte[] data, final UUID id, final int pieces, final int piece)
    {
        super();
        this.data = data.clone();
        this.id = id;
        this.pieces = pieces;
        this.piece = piece;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final int length = buf.readInt();
        final byte[] compressedData = new byte[length];
        buf.readBytes(compressedData);
        data = Structure.uncompress(compressedData);
        pieces = buf.readInt();
        piece = buf.readInt();
        id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        final byte[] compressedData = Structure.compress(data);
        if (compressedData != null)
        {
            buf.capacity(compressedData.length + buf.writerIndex());
            buf.writeInt(compressedData.length);
            buf.writeBytes(compressedData);
            buf.writeInt(pieces);
            buf.writeInt(piece);
            ByteBufUtils.writeUTF8String(buf, id.toString());
        }
    }

    @Override
    public void messageOnServerThread(final SchematicSaveMessage message, final EntityPlayerMP player)
    {
        if (!MineColonies.isClient() && !Configurations.gameplay.allowPlayerSchematics)
        {
            Log.getLogger().info("SchematicSaveMessage: custom schematic is not allowed on this server.");
            player.sendMessage(new TextComponentString("The server does not allow custom schematic!"));
            return;
        }

        if(message.pieces > MAX_AMOUNT_OF_PIECES)
        {
            Log.getLogger().error("Schematic has more than 10 pieces, discarding.");
            player.sendMessage(new TextComponentString("Schematic has more than 10 pieces, that's too big!"));
            return;
        }

        final boolean schematicSent;
        if (message.data == null)
        {
            Log.getLogger().error("Received empty schematic file");
            schematicSent = false;
        }
        else
        {
            schematicSent = Structures.handleSaveSchematicMessage(message.data, message.id, message.pieces, message.piece);
        }

        if (schematicSent)
        {
            player.sendMessage(new TextComponentString("Schematic successfully sent!"));
        }
        else
        {
            player.sendMessage(new TextComponentString("Failed to send the Schematic!"));
        }
    }

    @Override
    protected void messageOnClientThread(final SchematicSaveMessage message, final MessageContext ctx)
    {
        if (!MineColonies.isClient() && !Configurations.gameplay.allowPlayerSchematics)
        {
            Log.getLogger().info("SchematicSaveMessage: custom schematic is not allowed on this server.");
        }

        if (message.data == null)
        {
            Log.getLogger().error("Received empty schematic file");
        }
        else
        {
            Structures.handleSaveSchematicMessage(message.data);
        }
    }
}
