package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.buildings.IBuilding;

import java.util.List;

/**
 * Interface for workers that have an additional workstations besides their hut.
 */
public interface IHasExternalWorkStation
{
    /**
     * Get a list of potential external workstations.
     * @return list of stations the citizen is working at.
     */
    List<IBuilding> getWorkStations();
}
