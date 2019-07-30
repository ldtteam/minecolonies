package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.IColonyManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewCitizenViewMessage extends AbstractMessage<ColonyViewCitizenViewMessage, IMessage>
{
    private int     colonyId;
    private int     citizenId;
    private ByteBuf citizenBuffer;

    /**
     * The dimension the citizen is in.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewCitizenViewMessage()
    {
        super();
    }

    /**
     * Updates a {@link com.minecolonies.coremod.colony.CitizenDataView} of the citizens.
     *
     * @param colony  Colony of the citizen
     * @param citizen Citizen data of the citizen to update view
     */
    public ColonyViewCitizenViewMessage(@NotNull final Colony colony, @NotNull final ICitizenData citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen.getId();
        this.citizenBuffer = Unpooled.buffer();
        this.dimension = citizen.getColony().getDimension();
        citizen.serializeViewNetworkData(citizenBuffer);
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        dimension = buf.readInt();
        this.citizenBuffer = buf.retain();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        buf.writeInt(dimension);
        buf.writeBytes(citizenBuffer);
    }

    @Override
    protected void messageOnClientThread(final ColonyViewCitizenViewMessage message, final MessageContext ctx)
    {
        IColonyManager.getInstance().handleColonyViewCitizensMessage(message.colonyId, message.citizenId, message.citizenBuffer, message.dimension);
    }
}
