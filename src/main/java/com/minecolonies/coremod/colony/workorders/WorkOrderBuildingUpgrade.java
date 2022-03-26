package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete. Has his own structure for the building.
 */
public class WorkOrderBuildingUpgrade extends WorkOrderBuilding
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildingUpgrade()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to build.
     * @param level the level to upgrade the building to
     */
    public WorkOrderBuildingUpgrade(@NotNull final IBuilding building, final int level)
    {
        super(building, level);
    }

    @Override
    @NotNull
    protected WorkOrderType getType() {
        return WorkOrderType.UPGRADE;
    }
}
