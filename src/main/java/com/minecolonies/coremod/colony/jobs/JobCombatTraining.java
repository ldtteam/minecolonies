package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.trainingcamps.EntityAICombatTraining;

/**
 * The Knight's Training Job class
 */
public class JobCombatTraining extends AbstractJob
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobCombatTraining(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.combat;
    }

    @Override
    public String getName()
    {
        return "CombatTraining";
    }

    @Override
    public String getExperienceTag()
    {
        return JobKnight.DESC;
    }

    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.KNIGHT_GUARD;
    }

    @Override
    public AbstractAISkeleton<? extends IJob> generateAI()
    {
        return new EntityAICombatTraining(this);
    }
}
