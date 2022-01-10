package com.minecolonies.coremod.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIRanger;
import net.minecraft.resources.ResourceLocation;

/**
 * The Ranger's Job class
 *
 * @author Asherslab
 */
public class JobRanger extends AbstractJobGuard<JobRanger>
{
    /**
     * The name associated with the job.
     */
    public static final String DESC = "com.minecolonies.coremod.job.Ranger";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobRanger(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generates the {@link AbstractEntityAIGuard} job for our ranger.
     *
     * @return The AI.
     */
    @Override
    public EntityAIRanger generateGuardAI()
    {
        return new EntityAIRanger(this);
    }

    /**
     * Gets the {@link IModelType} to use for our ranger.
     *
     * @return The model to use.
     */
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.ARCHER_GUARD_ID;
    }
}
