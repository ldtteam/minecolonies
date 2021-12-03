package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.trainingcamps.EntityAIArcherTraining;

/**
 * The Archers's Training Job class
 */
public class JobArcherTraining extends AbstractJob<EntityAIArcherTraining, JobArcherTraining>
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
    public IModelType getModel()
    {
        return ModModelTypes.ARCHER_GUARD;
    }

    @Override
    public EntityAIArcherTraining generateAI()
    {
        return new EntityAIArcherTraining(this);
    }
}
