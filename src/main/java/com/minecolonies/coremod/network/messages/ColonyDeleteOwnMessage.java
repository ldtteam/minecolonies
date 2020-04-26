package com.minecolonies.coremod.network.messages;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for deleting an owned colony
 */
public class ColonyDeleteOwnMessage implements IMessage
{
    @Override
    public void toBytes(final PacketBuffer buf)
    {

    }

    @Override
    public void fromBytes(final PacketBuffer buf)
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
        ServerPlayerEntity player = ctxIn.getSender();

        if (player == null)
        {
            return;
        }

        // Colony
        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.world, player);

        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.colony.delete.notfound");
            return;
        }

        IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), false, player.world);
        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.colony.delete.success");
    }
}
