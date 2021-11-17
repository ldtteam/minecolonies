package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.Skill;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IBuildingWorkerView extends IBuildingView
{
    /**
     * Returns the id of the worker.
     *
     * @return 0 if there is no worker else the correct citizen id.
     */
    List<Integer> getWorkerId();

    /**
     * Sets the id of the worker.
     *
     * @param workerId the id to set.
     */
    void addWorkerId(int workerId);

    /**
     * Primary skill getter.
     *
     * @return the primary skill.
     */
    @NotNull
    Skill getPrimarySkill();

    /**
     * Secondary skill getter.
     *
     * @return the secondary skill.
     */
    @NotNull
    Skill getSecondarySkill();

    /**
     * Remove a worker from the list.
     *
     * @param id the id to remove.
     */
    void removeWorkerId(int id);

    /**
     * Check if it has enough worker.
     *
     * @return true if so.
     */
    boolean hasEnoughWorkers();

    /**
     * Set the hiring mode and sync to the server.
     *
     * @param hiringMode the mode to set.
     */
    void setHiringMode(HiringMode hiringMode);

    /**
     * Get the name of the job.
     *
     * @return job name.
     */
    String getJobName();

    /**
     * Get the name of the job.
     *
     * @return job name.
     */
    String getJobDisplayName();
}
