package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.stat.IStat;

/**
 * Interface describing core building stats.
 * The first core stats module that is found in the building will define the values.
 */
public interface IDefinesCoreBuildingStatsModule extends IBuildingModule
{
    /**
     * Get the max building level a module allows.
     * By default this is 5.
     * todo: Code this to be defined by the actually schematic availability.
     * @return the max level.
     */
    default IStat<Integer> getMaxBuildingLevel()
    {
        return (prev) -> prev == 0 ? 5 : prev;
    }

    /**
     * Get the max number of inhabitants this module allows.
     * @return the modules max number of assigned citizens.
     */
    IStat<Integer> getMaxInhabitants();
}
