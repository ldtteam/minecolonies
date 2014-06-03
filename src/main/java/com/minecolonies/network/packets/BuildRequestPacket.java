package com.minecolonies.network.packets;

import com.minecolonies.tileentities.TileEntityHut;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestPacket extends AbstractPacket
{
    private int x, y, z, mode;

    public static final int BUILD  = 0;
    public static final int REPAIR = 1;


    public BuildRequestPacket(){}

    public BuildRequestPacket(int x, int y, int z, int mode)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mode = mode;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(mode);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        mode = buffer.readInt();
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
            switch(mode)
            {
                case BUILD:
                    tileEntity.requestBuilding();
                    break;
                case REPAIR:
                    tileEntity.requestRepair();
                    break;
            }
        }
    }
}
