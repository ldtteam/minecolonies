package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.HappinessData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

public class HappinessDataMessage extends AbstractMessage<HappinessDataMessage, IMessage>
{
    private int           colonyId;
    private HappinessData happinessData;

    public HappinessDataMessage()
    {
        super();
    }

    public HappinessDataMessage(@NotNull final Colony colony, @NotNull final HappinessData happinessData)
    {
        this.colonyId = colony.getID();
        this.happinessData = happinessData;
    }

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

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        happinessData.toBytes(byteBuf);
    }

    @Override
    protected void messageOnClientThread(final HappinessDataMessage message, final MessageContext ctx)
    {
        ColonyManager.handleHappinessDataMessage(message.colonyId,message.happinessData);
    }
}
