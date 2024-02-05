package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.HUT_BLOCK_MISSING_COLONY;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.TOOL_PERMISSION_SCEPTER_PERMISSION_DENY;

public abstract class AbstractColonyServerMessage implements IMessage
{
    /**
     * The dimensionId this message originates from
     */
    private ResourceKey<Level> dimensionId;

    /**
     * The colonyId this message originates from
     */
    private int colonyId;

    /**
     * Empty standard constructor.
     */
    public AbstractColonyServerMessage() {}

    /**
     * Network message for executing things on colonies on the server
     *
     * @param colony The colony we're executing on
     */
    public AbstractColonyServerMessage(final IColony colony)
    {
        this(colony.getDimension(), colony.getID());
    }

    /**
     * Network message for executing things on colonies on the server
     *
     * @param dimensionId The dimension of the colony
     * @param colonyId    The colony ID
     */
    public AbstractColonyServerMessage(final ResourceKey<Level> dimensionId, final int colonyId)
    {
        this.dimensionId = dimensionId;
        this.colonyId = colonyId;
    }

    @Nullable
    public Action permissionNeeded()
    {
        return Action.MANAGE_HUTS;
    }

    public boolean ownerOnly()
    {
        return false;
    }

    protected abstract void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony);

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    protected abstract void toBytesOverride(final FriendlyByteBuf buf);

    protected void toBytesAbstractOverride(final FriendlyByteBuf buf) {}

    @Override
    public final void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(dimensionId.location().toString());
        buf.writeInt(colonyId);
        toBytesAbstractOverride(buf);
        toBytesOverride(buf);
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected abstract void fromBytesOverride(final FriendlyByteBuf buf);

    protected void fromBytesAbstractOverride(final FriendlyByteBuf buf) {}

    @Override
    public final void fromBytes(final FriendlyByteBuf buf)
    {
        this.dimensionId = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        this.colonyId = buf.readInt();
        fromBytesAbstractOverride(buf);
        fromBytesOverride(buf);
    }

    @Override
    public final LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public final void onExecute(final net.neoforged.neoforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimensionId);
        if (colony != null)
        {
            if (!ownerOnly() && permissionNeeded() != null && !colony.getPermissions().hasPermission(player, permissionNeeded()))
            {
                if (player == null)
                {
                    return;
                }

                MessageUtils.format(TOOL_PERMISSION_SCEPTER_PERMISSION_DENY).sendTo(player);
                return;
            }
            else if (ownerOnly() && (player == null || colony.getPermissions().getOwner().equals(player.getUUID())))
            {
                if (player == null)
                {
                    return;
                }

                MessageUtils.format(TOOL_PERMISSION_SCEPTER_PERMISSION_DENY).sendTo(player);
                return;
            }

            onExecute(ctxIn, isLogicalServer, colony);
        }
        else
        {
            MessageUtils.format(HUT_BLOCK_MISSING_COLONY, this.getClass().getSimpleName()).sendTo(player);
        }
    }
}
