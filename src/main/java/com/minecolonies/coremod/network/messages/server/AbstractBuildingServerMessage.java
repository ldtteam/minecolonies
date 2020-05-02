package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class AbstractBuildingServerMessage<T extends IBuilding> extends AbstractColonyServerMessage
{
    /**
     * The buildingID this message originates from
     */
    private BlockPos buildingId;

    /**
     * Empty standard constructor.
     */
    public AbstractBuildingServerMessage()
    {
    }

    /**
     * Network message for executing things on buildings on the server
     *
     * @param building the building we're executing on.
     */
    public AbstractBuildingServerMessage(IBuildingView building)
    {
        this(building.getColony().getDimension(), building.getColony().getID(), building.getID());
    }

    /**
     * Network message for executing things on buildings on the server
     *
     * @param buildingId the ID of the building we're executing on.
     */
    public AbstractBuildingServerMessage(final int dimensionId, final int colonyId, final BlockPos buildingId)
    {
        super(dimensionId, colonyId);
        this.buildingId = buildingId;
    }

    public boolean errorIfCastFails()
    {
        return true;
    }

    protected abstract void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final T building);
    @Override
    protected final void toBytesAbstractOverride(final PacketBuffer buf)
    {
        buf.writeBlockPos(buildingId);
    }

    @Override
    protected final void fromBytesAbstractOverride(final PacketBuffer buf)
    {
        this.buildingId = buf.readBlockPos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
        if (building == null) return;

        try
        {
            onExecute(ctxIn, isLogicalServer, colony, (T) building);
        }
        catch (ClassCastException e)
        {
            if (errorIfCastFails())
            {
                Log.getLogger().warn("onExecute called with wrong type: ", e);
            }
        }
    }
}
