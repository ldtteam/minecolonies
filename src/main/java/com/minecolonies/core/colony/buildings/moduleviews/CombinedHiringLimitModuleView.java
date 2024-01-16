package com.minecolonies.core.colony.buildings.moduleviews;

/**
 * AbstractBuilding View for clients where the max is per building not per module.
 */
public class CombinedHiringLimitModuleView extends WorkerBuildingModuleView
{
    /**
     * Check if the module is full.
     * @return true if so.
     */
    public boolean isFull()
    {
        return buildingView.getAllAssignedCitizens().size() >= getMaxInhabitants();
    }
}
