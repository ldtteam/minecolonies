package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.smelter.EntityAIWorkSmelter;
import org.jetbrains.annotations.NotNull;

/**
 * The smelter job class.
 */
public class JobSmelter extends AbstractJob<EntityAIWorkSmelter, JobSmelter>
{
    /**
     * Create a smelter job.
     *
     * @param entity the lumberjack.
     */
    public JobSmelter(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.smelter;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.smelter";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.SMELTER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkSmelter generateAI()
    {
        return new EntityAIWorkSmelter(this);
    }
}
