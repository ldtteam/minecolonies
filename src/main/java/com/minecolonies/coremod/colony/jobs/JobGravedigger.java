package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import com.minecolonies.coremod.entity.ai.citizen.gravedigger.EntityAIWorkGravedigger;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the gravedigger, handles the graves of dead citizen.
 */
public class JobGravedigger extends AbstractJobCrafter<EntityAIWorkGravedigger, JobGravedigger>
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobGravedigger(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.gravedigger;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.gravedigger";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.GRAVEDIGGER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkGravedigger generateAI()
    {
        return new EntityAIWorkGravedigger(this);
    }
}
