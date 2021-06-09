package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.undertaker.EntityAIWorkUndertaker;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the undertaker, handles the graves of dead citizen.
 */
public class JobUndertaker extends AbstractJobCrafter<EntityAIWorkUndertaker, JobUndertaker>
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobUndertaker(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.undertaker;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.undertaker";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.UNDERTAKER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkUndertaker generateAI()
    {
        return new EntityAIWorkUndertaker(this);
    }
}
