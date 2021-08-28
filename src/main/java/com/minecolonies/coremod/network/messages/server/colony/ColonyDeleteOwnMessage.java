package com.minecolonies.coremod.network.messages.server.colony;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

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
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.colony.delete.success");
        }
        else
        {
            LanguageHandler.sendPlayerMessage(
              player,
              "com.minecolonies.command.nocolony", this.getClass().getSimpleName()
            );
        }
    }
}
