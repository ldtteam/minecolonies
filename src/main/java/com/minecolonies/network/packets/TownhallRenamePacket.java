package com.minecolonies.network.packets;

import com.minecolonies.MineColonies;
import com.minecolonies.tileentities.TileEntityTownHall;
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
        PacketBuffer packetBuffer = new PacketBuffer(buffer);

        packetBuffer.writeInt(x);
        packetBuffer.writeInt(y);
        packetBuffer.writeInt(z);
        try
        {
            packetBuffer.writeStringToBuffer(name);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        PacketBuffer packetBuffer = new PacketBuffer(buffer);

        x = packetBuffer.readInt();
        y = packetBuffer.readInt();
        z = packetBuffer.readInt();
        try
        {
            name = packetBuffer.readStringFromBuffer(128);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
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
