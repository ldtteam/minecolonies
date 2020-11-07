package com.minecolonies.api.colony.buildings.modules;

/**
 * Interface describing core building stats.
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
