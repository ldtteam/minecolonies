package com.minecolonies.core.network.messages.server.colony.building.builder;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class BuilderSelectWorkOrderMessage extends AbstractBuildingServerMessage<BuildingBuilder>
{
    private int workOrder;

    /**
     * Empty standard constructor.
     */
    public BuilderSelectWorkOrderMessage()
    {
        super();
    }

    /**
     * Creates a new BuilderSetManualModeMessage.
     *
     * @param building View of the building to read data from.
     * @param workOrder workorder id.
     */
    public BuilderSelectWorkOrderMessage(@NotNull final IBuildingView building, final int workOrder)
    {
        super(building);
        this.workOrder = workOrder;
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        workOrder = buf.readInt();
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(workOrder);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingBuilder building)
    {
        building.setWorkOrder(workOrder);
    }
}
