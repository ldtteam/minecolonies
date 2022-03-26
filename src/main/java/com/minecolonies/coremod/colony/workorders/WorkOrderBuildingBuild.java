package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete. Has his own structure for the building.
 */
public class WorkOrderBuildingBuild extends WorkOrderBuilding
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildingBuild()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to build.
     */
    public WorkOrderBuildingBuild(@NotNull final IBuilding building)
    {
        super(building, 1);
    }

    @Override
    @NotNull
    protected WorkOrderType getType() {
        return WorkOrderType.BUILD;
    }
}
