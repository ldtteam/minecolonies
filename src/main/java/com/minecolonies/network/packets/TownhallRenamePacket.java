package com.minecolonies.network.packets;

import com.minecolonies.MineColonies;
import com.minecolonies.tileentities.TileEntityTownHall;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class TownhallRenamePacket extends AbstractPacket
{
    private int x, y, z;
    private String name;

    public TownhallRenamePacket(){}

    public TownhallRenamePacket(int x, int y, int z, String name)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        ByteBufUtils.writeUTF8String(buffer, name);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        name = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        TileEntityTownHall townhall = (TileEntityTownHall) player.getEntityWorld().getTileEntity(x, y, z);

        if(townhall != null)
        {
            townhall.setCityName(name);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        TileEntityTownHall townhall = (TileEntityTownHall) player.getEntityWorld().getTileEntity(x, y, z);

        if(townhall != null)
        {
            townhall.setCityName(name);
            MineColonies.packetPipeline.sendToAll(this);
        }
    }
}
