package com.minecolonies.network.packets;

import com.minecolonies.tileentities.TileEntityHut;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage implements IMessage
{
    private int x, y, z, mode;

    public static final int BUILD  = 0;
    public static final int REPAIR = 1;


    public BuildRequestMessage(){}

    public BuildRequestMessage(int x, int y, int z, int mode)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mode = mode;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(mode);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        mode = buf.readInt();
    }

    public static class Handler implements IMessageHandler<BuildRequestMessage, IMessage>
    {
        @Override
        public IMessage onMessage(BuildRequestMessage message, MessageContext ctx)
        {
            TileEntityHut tileEntity = (TileEntityHut) ctx.getServerHandler().playerEntity.getEntityWorld()
                    .getTileEntity(message.x, message.y, message.z);

            if(tileEntity != null)
            {
                switch(message.mode)
                {
                    case BUILD:
                        tileEntity.requestBuilding();
                        break;
                    case REPAIR:
                        tileEntity.requestRepair();
                        break;
                }
            }

            return null;
        }
    }
}
