package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.farmer.EntityAIWorkFarmer;

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
    public JobFarmer(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.job.Farmer";
    }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FARMER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkFarmer(this);
    }
}
