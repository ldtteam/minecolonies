package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.fletcher.EntityAIWorkFletcher;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Fletcher job.
 */
public class JobFletcher extends AbstractJobCrafter<EntityAIWorkFletcher, JobFletcher>
{
    /**
     * Instantiates the job for the Fletcher.
     *
     * @param entity the citizen who becomes a Fletcher
     */
    public JobFletcher(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.fletcher;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.fletcher";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkFletcher generateAI()
    {
        return new EntityAIWorkFletcher(this);
    }

    @NotNull
    @Override
    public IModelType getModel()
    {
        return BipedModelType.FLETCHER;
    }
}
