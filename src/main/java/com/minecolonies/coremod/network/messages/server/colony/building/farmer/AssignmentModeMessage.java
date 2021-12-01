package com.minecolonies.coremod.network.messages.server.colony.building.farmer;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.modules.FarmerFieldModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the assignmentMode of the fields of the farmer.
 */
public class AssignmentModeMessage extends AbstractBuildingServerMessage<BuildingFarmer>
{
    private boolean assignmentMode;

    /**
     * Empty standard constructor.
     */
    public AssignmentModeMessage()
    {
        super();
    }

    /**
     * Creates object for the assignmentMode
     *
     * @param assignmentMode assignmentMode of the particular farmer.
     * @param building       the building we're executing on.
     */
    public AssignmentModeMessage(@NotNull final IBuildingView building, final boolean assignmentMode)
    {
        super(building);
        this.assignmentMode = assignmentMode;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        assignmentMode = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeBoolean(assignmentMode);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingFarmer building)
    {
        building.getFirstOptionalModuleOccurance(FarmerFieldModule.class).ifPresent(m -> m.setAssignManually(assignmentMode));
    }
}
