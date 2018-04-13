package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

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
    public WorkOrderBuildRemoval(@NotNull final AbstractBuilding building, final int newLevel)
    {
        super(building, newLevel);
    }

    @Override
    public boolean isCleared()
    {
        return false;
    }

    @Override
    public boolean isValid(@NotNull final Colony colony)
    {
        return colony.getBuildingManager().getBuilding(buildingLocation) == null;
    }
}
