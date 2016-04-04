package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewCitizenViewMessage implements IMessage, IMessageHandler<ColonyViewCitizenViewMessage, IMessage>
{
    private int     colonyId;
    private int     citizenId;
    private ByteBuf citizenBuffer;

    public ColonyViewCitizenViewMessage(){}

    /**
     * Updates a {@link com.minecolonies.colony.CitizenData.View} of the citizens
     *
     * @param colony     Colony of the citizen
     * @param citizen    Citizen data of the citizen to update view
     */
    public ColonyViewCitizenViewMessage(Colony colony, CitizenData citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen.getId();
        this.citizenBuffer = Unpooled.buffer();
        citizen.serializeViewNetworkData(citizenBuffer);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        buf.writeBytes(citizenBuffer);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        citizenBuffer = buf;
    }

    @Override
    public IMessage onMessage(ColonyViewCitizenViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewCitizensMessage(message.colonyId, message.citizenId, message.citizenBuffer);
    }
}
