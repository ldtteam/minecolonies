package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.baker.EntityAIWorkBaker;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_JOB_BAKER;

/**
 * The fisherman's job class.
 * implements some useful things for him.
 */
public class JobBaker extends AbstractJob<EntityAIWorkBaker, JobBaker>
{
    /**
     * Initializes the job class.
     *
     * @param entity The entity which will use this job class.
     */
    public JobBaker(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.baker;
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
        return COM_MINECOLONIES_COREMOD_JOB_BAKER;
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
        return BipedModelType.BAKER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkBaker generateAI()
    {
        return new EntityAIWorkBaker(this);
    }
}

