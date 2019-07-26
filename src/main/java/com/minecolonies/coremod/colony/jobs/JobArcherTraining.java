package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.BipedModelType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ICitizenData;
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
