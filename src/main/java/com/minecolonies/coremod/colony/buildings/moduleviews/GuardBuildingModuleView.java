package com.minecolonies.coremod.colony.buildings.moduleviews;

/**
 * AbstractBuilding View for clients.
 */
public class GuardBuildingModuleView extends WorkerBuildingModuleView
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
