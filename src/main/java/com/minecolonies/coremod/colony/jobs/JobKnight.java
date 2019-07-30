package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.BipedModelType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIKnight;

/**
 * The Knight's job class
 *
 * @author Asherslab
 */
public class JobKnight extends AbstractJobGuard
{
    /**
     * Desc of knight job.
     */
    public static final String DESC = "com.minecolonies.coremod.job.Knight";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobKnight(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generates the {@link AbstractEntityAIGuard} job for our knight.
     *
     * @return The AI.
     */
    @Override
    public AbstractEntityAIGuard generateGuardAI()
    {
        return new EntityAIKnight(this);
    }

    /**
     * Gets the name of our knight.
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
        return BipedModelType.KNIGHT_GUARD;
    }
}
