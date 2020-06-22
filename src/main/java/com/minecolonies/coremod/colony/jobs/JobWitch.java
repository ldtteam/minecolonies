package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIWitch;

//TODO
public class JobWitch extends AbstractJobGuard<JobWitch>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobWitch(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    protected AbstractEntityAIGuard<JobWitch, ? extends AbstractBuildingGuards> generateGuardAI()
    {
        return new EntityAIWitch(this);
    }

    /**
     * The {@link JobEntry} for this job.
     *
     * @return The {@link JobEntry}.
     */
    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.witch;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @Override
    public String getName()
    {
        return null;
    }
}
