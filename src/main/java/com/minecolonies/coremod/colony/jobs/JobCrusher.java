package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.crusher.EntityAIWorkCrusher;
import org.jetbrains.annotations.NotNull;

/**
 * The crusher job class.
 */
public class JobCrusher extends AbstractJobCrafter
{
    /**
     * Create a crusher job.
     *
     * @param entity the lumberjack.
     */
    public JobCrusher(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.crusher;
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
        return "com.minecolonies.coremod.job.crusher";
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
    public AbstractAISkeleton<JobCrusher> generateAI()
    {
        return new EntityAIWorkCrusher(this);
    }
}
