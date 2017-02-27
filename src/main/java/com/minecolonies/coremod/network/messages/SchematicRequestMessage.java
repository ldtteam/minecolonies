package com.minecolonies.coremod.network.messages;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.network.messages.SaveSchematicMessage;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import com.minecolonies.structures.helpers.StructureProxy;

import java.io.InputStream;

import com.minecolonies.structures.helpers.Structure;

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
     * @param building AbstractBuilding of the request.
     * @param mode     Mode of the request, 1 is repair, 0 is build.
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
            Log.getLogger().info("Request Schematic file for " + message.filename);
            byte [] schematic = Structure.getStreamAsByteArray(stream);
            MineColonies.getNetwork().sendTo(new SaveSchematicMessage(schematic, message.filename), player);
        }
    }
}
