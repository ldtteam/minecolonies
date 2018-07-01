package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveCitizenMessage extends AbstractMessage<ColonyViewRemoveCitizenMessage, IMessage>
{
    private int colonyId;
    private int citizenId;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewRemoveCitizenMessage()
    {
        super();
    }

    /**
     * Creates an object for the remove message for citizen.
     *
     * @param colony  Colony the citizen is in.
     * @param citizen Citizen ID.
     */
    public ColonyViewRemoveCitizenMessage(@NotNull final Colony colony, final int citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
    }

    @Override
    protected void messageOnClientThread(final ColonyViewRemoveCitizenMessage message, final MessageContext ctx)
    {
        ColonyManager.handleColonyViewRemoveCitizenMessage(message.colonyId, message.citizenId);
    }
}
