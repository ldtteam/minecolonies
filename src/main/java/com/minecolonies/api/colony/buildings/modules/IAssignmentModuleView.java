package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Assignment module view interface.
 */
public interface IAssignmentModuleView extends IBuildingModuleView
{
    /**
     * Get the list of assigned people.
     * @return the list
     */
    List<Integer> getAssignedCitizens();

    /**
     * Add a citizen.
     * @param id the citizen id.
     */
    void addCitizen(final @NotNull ICitizenDataView id);

    /**
     * Remove a citizen.
     * @param id the citizen id/
     */
    void removeCitizen(final @NotNull ICitizenDataView id);

    /**
     * Get the hiring mode.
     * @return the set mode.
     */
    HiringMode getHiringMode();

    /**
     * Set the hiring mode.
     * @param hiringMode the set mode.
     */
    void setHiringMode(final HiringMode hiringMode);

    /**
     * Check if citizens can be assigned.
     * @param data the data to check.
     * @return true if so.
     */
    boolean canAssign(ICitizenDataView data);

    /**
     * Get the max number of inhabitants
     *
     * @return max inhabitants
     */
    int getMaxInhabitants();

    /**
     * Check if the module is full.
     * @return true if so.
     */
    boolean isFull();

    /**
     * Get the job entry of the citizen to be assigned.
     * @return the entry.
     */
    JobEntry getJobEntry();
}
