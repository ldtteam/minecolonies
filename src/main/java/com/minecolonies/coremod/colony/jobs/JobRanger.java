package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIRanger;

/**
 * The Ranger's Job class
 *
 * @author Asherslab
 */
public class JobRanger extends AbstractJobGuard
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
    public AbstractEntityAIGuard generateGuardAI()
    {
        return new EntityAIRanger(this);
    }

    /**
     * Gets the name of our ranger.
     *
     * @return The name.
     */
    @Override
    public String getName()
    {
        return DESC;
    }

    /**
     * Gets the {@link BipedModelType} to use for our ranger.
     *
     * @return The model to use.
     */
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.ARCHER_GUARD;
    }
}
