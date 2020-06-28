package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.mechanic.EntityAIWorkMechanic;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Mechanic job.
 */
public class JobMechanic extends AbstractJobCrafter<EntityAIWorkMechanic, JobMechanic>
{
    /**
     * Instantiates the job for the Mechanic.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public JobMechanic(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.mechanic;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.mechanic";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkMechanic generateAI()
    {
        return new EntityAIWorkMechanic(this);
    }
}
