package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
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
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobRanger(final CitizenData entity)
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
        return "com.minecolonies.coremod.job.Ranger";
    }

    /**
     * Gets the {@link RenderBipedCitizen.Model} to use for our ranger.
     *
     * @return The model to use.
     */
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.ARCHER_GUARD;
    }
}
