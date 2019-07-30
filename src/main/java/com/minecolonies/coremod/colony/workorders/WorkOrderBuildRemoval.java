package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
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
     * @param building the building to build.
     * @param level    the level it should have.
     */
    public WorkOrderBuildRemoval(@NotNull final AbstractBuilding building, final int level)
    {
        super(building, level);
    }

    @Override
    public boolean isCleared()
    {
        return false;
    }

    @Override
    public boolean isValid(@NotNull final Colony colony)
    {
        return true;
    }
}
