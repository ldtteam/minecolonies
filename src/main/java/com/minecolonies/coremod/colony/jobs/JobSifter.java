package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.sifter.EntityAIWorkSifter;
import org.jetbrains.annotations.NotNull;

/**
 * The sifter job class.
 */
public class JobSifter extends AbstractJobCrafter<EntityAIWorkSifter, JobSifter>
{
    /**
     * Create a sifter job.
     *
     * @param entity the lumberjack.
     */
    public JobSifter(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.sifter;
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
        return "com.minecolonies.coremod.job.sifter";
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
    public EntityAIWorkSifter generateAI()
    {
        return new EntityAIWorkSifter(this);
    }
}
