package com.minecolonies.coremod.network.messages.server.colony;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for deleting an owned colony
 */
public class ColonyDeleteOwnMessage extends AbstractColonyServerMessage
{

    @Nullable
    @Override
    public Action permissionNeeded()
    {
        return null;
    }

    @Override
    public boolean ownerOnly()
    {
        return true;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ServerPlayerEntity player = ctxIn.getSender();
        if (player == null) return;

        IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), false, player.world);
        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.colony.delete.success");
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }
}
