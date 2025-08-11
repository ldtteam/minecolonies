package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for trying to teleport to a friends colony.
 */
public class TeleportToColonyMessage extends AbstractColonyServerMessage
{
    private BlockPos pos;
    public TeleportToColonyMessage()
    {
        super();
    }

    public TeleportToColonyMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos pos)
    {
        super(dimensionId, colonyId);
        this.pos = pos;
    }

    @Nullable
    @Override
    public Action permissionNeeded()
    {
        return null;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (ctxIn.getSender() == null)
        {
            return;
        }

        if (colony.getPermissions().getRank(ctxIn.getSender().getUUID()) != colony.getPermissions().getRankNeutral())
        {
            TeleportHelper.colonyTeleport(ctxIn.getSender(), colony, pos);
        }
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        this.pos =buf.readBlockPos();
    }
}
