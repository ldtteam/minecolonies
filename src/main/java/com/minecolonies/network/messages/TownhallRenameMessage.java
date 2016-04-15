package com.minecolonies.network.messages;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class TownhallRenameMessage implements IMessage, IMessageHandler<TownhallRenameMessage, IMessage>
{
    private int    colonyId;
    private String name;

    public TownhallRenameMessage(){}

    /**
     * Object creation for the town hall rename message
     *
     * @param colony    Colony the rename is going to occur in
     * @param name      New name of the town hall
     */
    public TownhallRenameMessage(ColonyView colony, String name)
    {
        this.colonyId = colony.getID();
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public IMessage onMessage(TownhallRenameMessage message, MessageContext ctx)
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
