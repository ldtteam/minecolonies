package com.minecolonies.network.packets;

import com.minecolonies.tileentities.TileEntityHut;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestPacket extends AbstractPacket
{
    private int x, y, z;

    public BuildRequestPacket(){}

    public BuildRequestPacket(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        PacketBuffer packetBuffer = new PacketBuffer(buffer);

        packetBuffer.writeInt(x);
        packetBuffer.writeInt(y);
        packetBuffer.writeInt(z);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        PacketBuffer packetBuffer = new PacketBuffer(buffer);

        x = packetBuffer.readInt();
        y = packetBuffer.readInt();
        z = packetBuffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {

    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        TileEntityHut tileEntity = (TileEntityHut) player.getEntityWorld().getTileEntity(x, y, z);

        if(tileEntity != null)
        {
            tileEntity.getTownHall().addHutForUpgrade(tileEntity.getName(), x, y, z);
        }
    }
}
