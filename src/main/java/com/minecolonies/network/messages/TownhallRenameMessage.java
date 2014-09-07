package com.minecolonies.network.packets;

import com.minecolonies.MineColonies;
import com.minecolonies.tileentities.TileEntityTownHall;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class TownhallRenameMessage implements IMessage
{
    private int x, y, z;
    private String name;

    public TownhallRenameMessage(){}

    public TownhallRenameMessage(int x, int y, int z, String name)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    public static class HandlerServer implements IMessageHandler<TownhallRenameMessage, IMessage>
    {
        @Override
        public IMessage onMessage(TownhallRenameMessage message, MessageContext ctx)
        {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            TileEntityTownHall townhall = (TileEntityTownHall) player.getEntityWorld().getTileEntity(message.x, message.y, message.z);

            if(townhall != null)
            {
                townhall.setCityName(message.name);
                MineColonies.network.sendToAll(message);
            }

            return null;
        }
    }


    public static class HandlerClient implements IMessageHandler<TownhallRenameMessage, IMessage>
    {
        @Override
        public IMessage onMessage(TownhallRenameMessage message, MessageContext ctx)
        {
            TileEntityTownHall townhall = (TileEntityTownHall) Minecraft.getMinecraft().thePlayer.getEntityWorld().getTileEntity(message.x, message.y, message.z);

            if(townhall != null)
            {
                townhall.setCityName(message.name);
            }

            return null;
        }
    }
}
