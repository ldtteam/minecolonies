package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.views.IBuildingView;

/**
 * Abstract class for all modules. Has base methods for all the necessary methods that have to be called from the building.
 */
public abstract class AbstractBuildingModuleView implements IBuildingModuleView
{
    /**
     * The building this module belongs to.
     */
    protected IBuildingView buildingView;

    @Override
    public IBuildingModuleView setBuildingView(final IBuildingView buildingView)
    {
        this.buildingView = buildingView;
        return this;
    }
}
