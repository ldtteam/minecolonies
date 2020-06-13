package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to be cleared.
 * Has his own structure for the building.
 */
public class WorkOrderBuildRemoval extends WorkOrderBuild
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildRemoval()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to remove.
     * @param level    the level it should have.
     */
    public WorkOrderBuildRemoval(@NotNull final IBuilding building, final int level)
    {
        super(building, level);
    }

    @Override
    public boolean isCleared()
    {
        return false;
    }

    @Override
    public boolean isValid(@NotNull final IColony colony)
    {
        return super.isValid(colony);
    }
}
