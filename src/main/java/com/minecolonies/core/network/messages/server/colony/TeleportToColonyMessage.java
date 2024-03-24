package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Message for trying to teleport to a friends colony.
 */
public class TeleportToColonyMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "teleport_to_colony", TeleportToColonyMessage::new);

    public TeleportToColonyMessage(final ResourceKey<Level> dimensionId, final int colonyId)
    {
        super(TYPE, dimensionId, colonyId);
    }

    protected TeleportToColonyMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Nullable
    @Override
    protected Action permissionNeeded()
    {
        return null;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        if (colony.getPermissions().getRank(player.getUUID()) != colony.getPermissions().getRankNeutral())
        {
            TeleportHelper.colonyTeleport(player, colony);
        }
    }
}
