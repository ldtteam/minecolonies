package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.event.ColonyDeletedEvent;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.api.util.MessageUtils;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Message for deleting an owned colony
 */
public class ColonyDeleteOwnMessage implements IMessage
{
    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {

    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {

    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.level, player);
        if (colony != null)
        {
            IColonyManager.getInstance().deleteColonyByDimension(colony.getID(), false, colony.getDimension());
            MessageUtils.format(MESSAGE_INFO_COLONY_DESTROY_SUCCESS).sendTo(player);

            if (isLogicalServer)
            {
                try
                {
                    MinecraftForge.EVENT_BUS.post(new ColonyDeletedEvent(colony));
                }
                catch (final Exception e)
                {
                    Log.getLogger().error("Error during ColonyDeletedEvent", e);
                }
            }
        }
        else
        {
            MessageUtils.format(MESSAGE_INFO_COLONY_NOT_FOUND).sendTo(player);
        }
    }
}
