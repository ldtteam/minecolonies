package com.minecolonies.core.debug.messages;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.debug.DebugPlayerManager;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message to toggle pathfinding debug for a specific citizen
 */
public class DebugEnablePathfindingMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "debug_togglepathfinding", DebugEnablePathfindingMessage::new);

    /**
     * Citizen id
     */
    private int id;

    /**
     * Whether to enable or disable pathfinding tracking
     */
    private boolean enable = false;

    public DebugEnablePathfindingMessage(final ICitizenDataView citizen, final boolean enable)
    {
        super(TYPE, citizen.getColony());
        this.id = citizen.getId();
        this.enable = enable;
    }

    protected DebugEnablePathfindingMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.id = buf.readInt();
        this.enable = buf.readBoolean();
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(id);
        buf.writeBoolean(enable);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        if (player == null || !DebugPlayerManager.hasDebugEnabled(player))
        {
            return;
        }

        final ICitizenData citizen = colony.getCitizenManager().getCivilian(id);
        if (citizen == null || !citizen.getEntity().isPresent())
        {
            return;
        }

        if (enable)
        {
            PathfindingUtils.trackingMap.put(player.getUUID(), citizen.getUUID());
        }
        else
        {
            PathfindingUtils.trackingMap.remove(player.getUUID());
        }
    }
}
