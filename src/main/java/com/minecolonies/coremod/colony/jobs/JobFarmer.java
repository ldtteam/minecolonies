package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the farmer, handles his fields.
 */
public class JobFarmer extends AbstractJob<EntityAIWorkFarmer, JobFarmer>
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobFarmer(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.farmer;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Farmer";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.FARMER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkFarmer generateAI()
    {
        return new EntityAIWorkFarmer(this);
    }
}
