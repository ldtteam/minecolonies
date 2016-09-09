package com.minecolonies.network.messages;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TownHallRenameMessage implements IMessage, IMessageHandler<TownHallRenameMessage, IMessage>
{
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
        this.name = name;
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

    @Nullable
    @Override
    public IMessage onMessage(@NotNull TownHallRenameMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);

        if (colony != null)
        {
            colony.setName(message.name);
            MineColonies.getNetwork().sendToAll(message);
        }

        return null;
    }
}
