package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.jobs.IJob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICitizenJobHandler
{
    /**
     * Set Model depending on job.
     * 
     * @param job the new job.
     */
    void setModelDependingOnJob(@Nullable IJob<?> job);

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    void onJobChanged(@Nullable IJob<?> job);

    /**
     * Get the job of the citizen.
     *
     * @param type of the type.
     * @param <J>  wildcard.
     * @return the job.
     */
    @Nullable
    <J extends IJob<?>> J getColonyJob(@NotNull Class<J> type);

    /**
     * Gets the job of the entity.
     * 
     * @return the job or els enull.
     */
    @Nullable
    IJob<?> getColonyJob();

    /**
     * Method to check if the citizen job allows to run the avoidance task.
     * 
     * @return true if so.
     */
    boolean shouldRunAvoidance();
}
