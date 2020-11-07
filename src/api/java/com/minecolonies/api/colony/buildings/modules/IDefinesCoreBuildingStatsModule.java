package com.minecolonies.api.colony.buildings.modules;

/**
 * Interface describing core building stats.
 * The first core stats module that is found in the building will define the values.
 */
public interface IDefinesCoreBuildingStatsModule extends IBuildingModule
{
    /**
     * Get the max building level a module allows.
     * By default this is 5.
     * @return the max level.
     */
    default int getMaxBuildingLevel()
    {
        return 5;
    }

    /**
     * Get the max number of inhabitants this module allows.
     * @return the modules max number of assigned citizens.
     */
    int getMaxInhabitants();
}
