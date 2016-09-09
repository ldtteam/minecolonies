package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewCitizenViewMessage implements IMessage, IMessageHandler<ColonyViewCitizenViewMessage, IMessage>
{
    private int     colonyId;
    private int     citizenId;
    private ByteBuf citizenBuffer;

    public ColonyViewCitizenViewMessage() {}

    /**
     * Updates a {@link com.minecolonies.colony.CitizenDataView} of the citizens.
     *
     * @param colony  Colony of the citizen
     * @param citizen Citizen data of the citizen to update view
     */
    public ColonyViewCitizenViewMessage(@NotNull Colony colony, @NotNull CitizenData citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen.getId();
        this.citizenBuffer = Unpooled.buffer();
        citizen.serializeViewNetworkData(citizenBuffer);
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        this.citizenBuffer = Unpooled.buffer();
        buf.readBytes(citizenBuffer, buf.readableBytes());
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        buf.writeBytes(citizenBuffer);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull ColonyViewCitizenViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewCitizensMessage(message.colonyId, message.citizenId, message.citizenBuffer);
    }
}
