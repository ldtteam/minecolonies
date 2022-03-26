package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to be cleared. Has his own structure for the building.
 */
public class WorkOrderBuildingRemove extends WorkOrderBuilding
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildingRemove()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to remove.
     * @param level    the level it should have.
     */
    public WorkOrderBuildingRemove(@NotNull final IBuilding building, final int level)
    {
        super(building, level);
    }

    @Override
    protected @NotNull WorkOrderType getType() {
        return WorkOrderType.REMOVE;
    }

    @Override
    public boolean isCleared()
    {
        return false;
    }
}
