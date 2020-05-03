package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractColonyServerMessage implements IMessage
{
    /**
     * The dimensionId this message originates from
     */
    private int dimensionId;

    /**
     * The colonyId this message originates from
     */
    private int colonyId;

    /**
     * Empty standard constructor.
     */
    public AbstractColonyServerMessage()
    {
    }

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
    public AbstractColonyServerMessage(final int dimensionId, final int colonyId)
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

    protected abstract void toBytesOverride(final PacketBuffer buf);
    protected void toBytesAbstractOverride(final PacketBuffer buf) {}

    @Override
    public final void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(dimensionId);
        buf.writeInt(colonyId);
        toBytesAbstractOverride(buf);
        toBytesOverride(buf);
    }

    protected abstract void fromBytesOverride(final PacketBuffer buf);
    protected void fromBytesAbstractOverride(final PacketBuffer buf) {}

    @Override
    public final void fromBytes(final PacketBuffer buf)
    {
        this.dimensionId = buf.readInt();
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
    public final void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayerEntity player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimensionId);
        if (colony != null)
        {
            if (!ownerOnly() && permissionNeeded() != null && !colony.getPermissions().hasPermission(player, permissionNeeded()))
            {
                if (player == null) return;

                LanguageHandler.sendPlayerMessage(
                  player,
                  "com.minecolonies.coremod.item.permissionscepter.permission.deny"
                );
                return;
            }
            else if (ownerOnly() && (player == null || colony.getPermissions().getOwner().equals(player.getUniqueID())))
            {
                if (player == null) return;

                LanguageHandler.sendPlayerMessage(
                  player,
                  "com.minecolonies.coremod.item.permissionscepter.permission.deny"
                );
                return;
            }

            onExecute(ctxIn, isLogicalServer, colony);
        }
    }
}
