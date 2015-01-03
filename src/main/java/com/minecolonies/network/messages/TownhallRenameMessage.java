package com.minecolonies.network.messages;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.network.PacketUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class TownhallRenameMessage implements IMessage, IMessageHandler<TownhallRenameMessage, IMessage>
{
    private UUID colonyId;
    private String name;

    public TownhallRenameMessage(){}

    public TownhallRenameMessage(UUID colony, String name)
    {
        colonyId = colony;
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, colonyId);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = PacketUtils.readUUID(buf);
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public IMessage onMessage(TownhallRenameMessage message, MessageContext ctx)
    {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        Colony colony = ColonyManager.getColonyById(message.colonyId);

        if (colony != null)
        {
            colony.setName(message.name);
            MineColonies.network.sendToAll(message);
        }

        return null;
    }
}
