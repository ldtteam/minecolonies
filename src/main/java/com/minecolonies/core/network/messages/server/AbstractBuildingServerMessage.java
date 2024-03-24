package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public abstract class AbstractBuildingServerMessage<T extends IBuilding> extends AbstractColonyServerMessage
{
    /**
     * The buildingID this message originates from
     */
    private final BlockPos buildingId;

    /**
     * Network message for executing things on buildings on the server
     *
     * @param building the building we're executing on.
     */
    public AbstractBuildingServerMessage(final PlayMessageType<?> type, final IBuildingView building)
    {
        this(type, building.getColony().getDimension(), building.getColony().getID(), building.getID());
    }

    /**
     * Network message for executing things on buildings on the server
     *
     * @param buildingId  the ID of the building we're executing on.
     * @param colonyId    the ID of the colony we're executing on.
     * @param dimensionId the ID of the dimension we're executing on.
     */
    public AbstractBuildingServerMessage(final PlayMessageType<?> type, final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId)
    {
        super(type, dimensionId, colonyId);
        this.buildingId = buildingId;
    }

    protected abstract void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final T building);

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeBlockPos(buildingId);
    }

    protected AbstractBuildingServerMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.buildingId = buf.readBlockPos();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
        if (building == null)
        {
            return;
        }

        try
        {
            onExecute(ctxIn, player, colony, (T) building);
        }
        catch (ClassCastException e)
        {
            Log.getLogger().warn("onExecute called with wrong type: ", e);
        }
    }
}
