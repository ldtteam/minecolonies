package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
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
    public JobArcherTraining(final CitizenData entity)
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
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.ARCHER_GUARD;
    }

    @Override
    public AbstractAISkeleton<? extends AbstractJob> generateAI()
    {
        return new EntityAIArcherTraining(this);
    }
}
