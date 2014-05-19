package com.minecolonies.network.packets;

import com.minecolonies.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

public class TileEntityPacket extends AbstractPacket
{
    private int x, y, z;
    private NBTTagCompound data;

    public TileEntityPacket() {}

    public TileEntityPacket(int x, int y, int z, NBTTagCompound data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
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
            packetBuffer.writeNBTTagCompoundToBuffer(data);
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
            data = packetBuffer.readNBTTagCompoundFromBuffer();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        TileEntity tileEntity = player.getEntityWorld().getTileEntity(x, y, z);

        if (tileEntity != null)
        {
            tileEntity.readFromNBT(data);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        TileEntity tileEntity = player.getEntityWorld().getTileEntity(x, y, z);

        if (tileEntity != null)
        {
            tileEntity.readFromNBT(data);
        }
    }
}
