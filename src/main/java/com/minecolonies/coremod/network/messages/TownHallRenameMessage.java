package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.IColonyView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class TownHallRenameMessage extends AbstractMessage<TownHallRenameMessage, IMessage>
{
    private static final int MAX_NAME_LENGTH  = 25;
    private static final int SUBSTRING_LENGTH = MAX_NAME_LENGTH - 1;
    private int    colonyId;
    private String name;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public TownHallRenameMessage()
    {
        super();
    }

    /**
     * Object creation for the town hall rename message.
     *
     * @param colony Colony the rename is going to occur in.
     * @param name   New name of the town hall.
     */
    public TownHallRenameMessage(@NotNull final IColonyView colony, final String name)
    {
        super();
        this.colonyId = colony.getID();
        this.name = (name.length() <= MAX_NAME_LENGTH) ? name : name.substring(0, SUBSTRING_LENGTH);
        this.dimension = colony.getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final TownHallRenameMessage message, final PlayerEntityMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }
            message.name = (message.name.length() <= MAX_NAME_LENGTH) ? message.name : message.name.substring(0, SUBSTRING_LENGTH);
            colony.setName(message.name);
            MineColonies.getNetwork().sendToAll(message);
        }
    }
}
