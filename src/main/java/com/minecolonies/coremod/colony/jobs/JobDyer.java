package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.dyer.EntityAIWorkDyer;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the dyer job.
 */
public class JobDyer extends AbstractJobCrafter<EntityAIWorkDyer, JobDyer>
{
    /**
     * Instantiates the job for the Dyer.
     *
     * @param entity the citizen who becomes a dyer.
     */
    public JobDyer(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.dyer;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.dyer";
    }

    @NotNull
    @Override
    public IModelType getModel()
    {
        return BipedModelType.DYER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkDyer generateAI()
    {
        return new EntityAIWorkDyer(this);
    }
}
