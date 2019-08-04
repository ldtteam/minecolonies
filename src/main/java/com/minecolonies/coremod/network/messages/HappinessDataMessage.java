package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.HappinessData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.client.Minecraft;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class handling the messages about updating happiness
 */
public class HappinessDataMessage implements IMessage
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
    public void fromBytes(final PacketBuffer byteBuf)
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
    public void toBytes(final PacketBuffer byteBuf)
    {
        byteBuf.writeInt(colonyId);
        happinessData.toBytes(byteBuf);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        IColonyManager.getInstance().handleHappinessDataMessage(colonyId, happinessData, Minecraft.getInstance().world.getDimension().getType().getId());
    }
}
