package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.stonesmeltery.EntityAIWorkStoneSmeltery;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Stone Smeltery job.
 */
public class JobStoneSmeltery extends AbstractJobCrafter<EntityAIWorkStoneSmeltery, JobStoneSmeltery>
{
    /**
     * Instantiates the job for the Stone Smeltery.
     *
     * @param entity the citizen who becomes a Stone Smelter.
     */
    public JobStoneSmeltery(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.stoneSmeltery;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.StoneSmeltery";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkStoneSmeltery generateAI()
    {
        return new EntityAIWorkStoneSmeltery(this);
    }
}
