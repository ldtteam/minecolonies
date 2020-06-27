package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
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
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.archer;
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
    public EntityAIArcherTraining generateAI()
    {
        return new EntityAIArcherTraining(this);
    }
}
