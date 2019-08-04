package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.trainingcamps.EntityAIArcherTraining;

/**
 * The Archers's Training Job class
 */
public class JobArcherTraining extends AbstractJob
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobArcherTraining(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "ArcherTraining";
    }

    @Override
    public String getExperienceTag()
    {
        return JobRanger.DESC;
    }

    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.ARCHER_GUARD;
    }

    @Override
    public AbstractAISkeleton<? extends IJob> generateAI()
    {
        return new EntityAIArcherTraining(this);
    }
}
