package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;

/**
 * Classes providing building modules.
 */
public interface IBuildingModuleProvider
{
    /**
     * Register the building modules.
     * @param building the building the modules will get added to.
     */
    void registerBuildingModules(final IBuilding building);
}
