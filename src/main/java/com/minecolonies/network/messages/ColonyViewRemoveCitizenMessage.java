package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveCitizenMessage implements IMessage, IMessageHandler<ColonyViewRemoveCitizenMessage, IMessage>
{
    private int colonyId;
    private int citizenId;

    public ColonyViewRemoveCitizenMessage() {}

    /**
     * Creates an object for the remove message for citizen
     *
     * @param colony  Colony the citizen is in
     * @param citizen Citizen ID
     */
    public ColonyViewRemoveCitizenMessage(@Nonnull Colony colony, int citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
    }

    @Nullable
    @Override
    public IMessage onMessage(@Nonnull ColonyViewRemoveCitizenMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveCitizenMessage(message.colonyId, message.citizenId);
    }
}
