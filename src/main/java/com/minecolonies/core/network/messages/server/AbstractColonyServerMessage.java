package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.HUT_BLOCK_MISSING_COLONY;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.TOOL_PERMISSION_SCEPTER_PERMISSION_DENY;

public abstract class AbstractColonyServerMessage extends AbstractServerPlayMessage
{
    /**
     * The dimensionId this message originates from
     */
    private final ResourceKey<Level> dimensionId;

    /**
     * The colonyId this message originates from
     */
    private final int colonyId;

    /**
     * Network message for executing things on colonies on the server
     *
     * @param colony The colony we're executing on
     */
    public AbstractColonyServerMessage(final PlayMessageType<?> type, final IColony colony)
    {
        this(type, colony.getDimension(), colony.getID());
    }

    /**
     * Network message for executing things on colonies on the server
     *
     * @param dimensionId The dimension of the colony
     * @param colonyId    The colony ID
     */
    public AbstractColonyServerMessage(final PlayMessageType<?> type, final ResourceKey<Level> dimensionId, final int colonyId)
    {
        super(type);
        this.dimensionId = dimensionId;
        this.colonyId = colonyId;
    }

    @Nullable
    protected Action permissionNeeded()
    {
        return Action.MANAGE_HUTS;
    }

    public boolean ownerOnly()
    {
        return false;
    }

    protected abstract void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony);

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(dimensionId.location().toString());
        buf.writeInt(colonyId);
    }

    protected AbstractColonyServerMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.dimensionId = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        this.colonyId = buf.readInt();
    }

    @Override
    public final void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimensionId);
        if (colony != null)
        {
            if (!ownerOnly() && permissionNeeded() != null && !colony.getPermissions().hasPermission(player, permissionNeeded()))
            {
                MessageUtils.format(TOOL_PERMISSION_SCEPTER_PERMISSION_DENY).sendTo(player);
                return;
            }
            else if (ownerOnly() && !colony.getPermissions().getOwner().equals(player.getUUID()))
            {
                MessageUtils.format(TOOL_PERMISSION_SCEPTER_PERMISSION_DENY).sendTo(player);
                return;
            }

            onExecute(ctxIn, player, colony);
        }
        else
        {
            MessageUtils.format(HUT_BLOCK_MISSING_COLONY, this.getClass().getSimpleName()).sendTo(player);
        }
    }
}
