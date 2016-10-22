package com.minecolonies.network.messages;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

public class TownHallRenameMessage extends AbstractMessage<TownHallRenameMessage, IMessage>
{
    private static final int MAX_NAME_LENGTH  = 25;
    private static final int SUBSTRING_LENGTH = MAX_NAME_LENGTH - 1;
    private int    colonyId;
    private String name;

    public TownHallRenameMessage() {}

    /**
     * Object creation for the town hall rename message
     *
     * @param colony Colony the rename is going to occur in
     * @param name   New name of the town hall
     */
    public TownHallRenameMessage(@NotNull ColonyView colony, String name)
    {
        this.colonyId = colony.getID();
        this.name = (name.length() <= MAX_NAME_LENGTH) ? name : name.substring(0, SUBSTRING_LENGTH);
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void messageOnServerThread(final TownHallRenameMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            message.name = (message.name.length() <= MAX_NAME_LENGTH) ? message.name : message.name.substring(0, SUBSTRING_LENGTH);
            colony.setName(message.name);
            MineColonies.getNetwork().sendToAll(message);
        }
    }
}
