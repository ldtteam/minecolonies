package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.network.PacketUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewMessage implements IMessage, IMessageHandler<ColonyViewMessage, IMessage>
{
    private UUID         colonyId;
    private boolean      isNewSubscription;
    private PacketBuffer colonyBuffer = new PacketBuffer(Unpooled.buffer());

    public ColonyViewMessage(){}

    public ColonyViewMessage(UUID colonyId, Colony colony, boolean isNewSubscription) throws IOException
    {
        this.colonyId = colonyId;
        this.isNewSubscription = isNewSubscription;
        ColonyView.serializeNetworkData(colony, isNewSubscription, colonyBuffer);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeBytes(colonyBuffer);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = PacketUtils.readUUID(buf);
        isNewSubscription = buf.readBoolean();
        buf.readBytes(colonyBuffer, buf.readableBytes());
    }

    @Override
    public IMessage onMessage(ColonyViewMessage message, MessageContext ctx)
    {
        try
        {
            return ColonyManager.handleColonyViewMessage(message.colonyId, message.colonyBuffer, message.isNewSubscription);
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
            return null;
        }
    }
}
