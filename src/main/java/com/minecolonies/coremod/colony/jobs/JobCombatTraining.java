package com.minecolonies.coremod.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.trainingcamps.EntityAICombatTraining;

/**
 * The Knight's Training Job class
 */
public class JobCombatTraining extends AbstractJob<EntityAICombatTraining, JobCombatTraining>
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
    public ResourceLocation getModel()
    {
        return ModModelTypes.KNIGHT_GUARD_ID;
    }

    @Override
    public EntityAICombatTraining generateAI()
    {
        return new EntityAICombatTraining(this);
    }
}
