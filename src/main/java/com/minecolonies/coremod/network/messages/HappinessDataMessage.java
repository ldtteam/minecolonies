package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.HappinessData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the messages about updating happiness
 */
public class HappinessDataMessage extends AbstractMessage<HappinessDataMessage, IMessage>
{
    /**
     * The id of the colony talking of
     */
    private int           colonyId;
    /**
     * The different values of the happiness
     */
    private HappinessData happinessData;

    /**
     * Need the default constructor
     */
    public HappinessDataMessage()
    {
        super();
    }

    /**
     * Constructor used to send a message
     *
     * @param colony        The colony the message will talk about
     * @param happinessData The data values for the happiness
     */
    public HappinessDataMessage(@NotNull final Colony colony, @NotNull final HappinessData happinessData)
    {
        this.colonyId = colony.getID();
        this.happinessData = happinessData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        colonyId = byteBuf.readInt();
        if (happinessData == null)
        {
            happinessData = new HappinessData();
        }
        happinessData.fromBytes(byteBuf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        happinessData.toBytes(byteBuf);
    }

    /**
     * {@inheritDoc}
     *
     * @param message the original message.
     * @param ctx     the context associated.
     */
    @Override
    protected void messageOnClientThread(final HappinessDataMessage message, final MessageContext ctx)
    {
        ColonyManager.handleHappinessDataMessage(message.colonyId, message.happinessData, Minecraft.getMinecraft().world.provider.getDimension());
    }
}
