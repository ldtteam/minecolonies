package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.PickUpPriorityState;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.Stash;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.colony.buildings.PickUpPriorityState.AUTOMATIC;

public class ChangePickUpPriorityStateMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The new delivery priority state of the building.
     */
    private PickUpPriorityState state = AUTOMATIC;

    /**
     * Empty public constructor.
     */
    public ChangePickUpPriorityStateMessage()
    {
        super();
    }

    /**
     * Creates message for player to change the priority of the delivery.
     *
     * @param building view of the building to read data from
     */
    public ChangePickUpPriorityStateMessage(@NotNull final IBuildingView building, final PickUpPriorityState state)
    {
        super(building);
        this.state = state;
    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {
        state = PickUpPriorityState.fromIntRepresentation(buf.readInt());
        if (state == null)
        {
            // This is just a sanity check. Since we are doing serialization ourselves, this should never happen.
            // But if it does happen, default to AUTOMATIC, just to be safe.
            state = AUTOMATIC;
        }
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {
        buf.writeInt(state.getIntRepresentation());
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building instanceof AbstractBuildingWorker || building instanceof Stash)
        {
            building.setPriorityState(state);
            building.markDirty();
        }
    }
}
