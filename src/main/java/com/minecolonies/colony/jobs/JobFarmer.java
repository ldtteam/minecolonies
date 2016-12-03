package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.farmer.EntityAIWorkFarmer;
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
        return "com.minecolonies.job.Farmer";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FARMER;
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
