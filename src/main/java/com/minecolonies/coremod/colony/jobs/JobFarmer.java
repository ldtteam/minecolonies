package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.Model;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.api.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the farmer, handles his fields.
 */
public class JobFarmer extends AbstractJob
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobFarmer(final CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Farmer";
    }

    @NotNull
    @Override
    public Model getModel()
    {
        return Model.FARMER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobFarmer> generateAI()
    {
        return new EntityAIWorkFarmer(this);
    }
}
