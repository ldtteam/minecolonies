package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete.
 * Has his own structure for the building.
 */
public class WorkOrderBuildBuilding extends WorkOrderBuild
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildBuilding()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to build.
     * @param newLevel    the level it should have.
     */
    public WorkOrderBuildBuilding(@NotNull final AbstractBuilding building, final int newLevel)
    {
        super(building, newLevel);
    }
}
