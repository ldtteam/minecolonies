package com.minecolonies.coremod.network.messages.server.colony.building.farmer;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the assignmentMode of the fields of the farmer.
 */
public class AssignmentModeMessage extends AbstractBuildingServerMessage<BuildingFarmer>
{
    private boolean  assignmentMode;

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
     */
    public AssignmentModeMessage(@NotNull final BuildingFarmer.View building, final boolean assignmentMode)
    {
        super(building);
        this.assignmentMode = assignmentMode;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        assignmentMode = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBoolean(assignmentMode);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingFarmer building)
    {
        building.setAssignManually(assignmentMode);
    }
}
