package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.buildings.IBuilding;

import java.util.List;

/**
 * Interface for workers that have an additional workstations besides their hut (e.g., the Quarrier)
 * This allows to treat the workstation as an expanded inventory for example.
 */
public interface IJobWithExternalWorkStations
{
    /**
     * Get a list of potential external workstations.
     * @return list of stations the citizen is working at.
     */
    List<IBuilding> getWorkStations();
}
