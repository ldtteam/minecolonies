package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.JobWitch;
import org.jetbrains.annotations.NotNull;

//TODO
public class EntityAIWitch extends AbstractEntityAIGuard<JobWitch, AbstractBuildingGuards>
{
    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWitch(@NotNull final JobWitch job)
    {
        super(job);
    }

    /**
     * Get the Attack state to go to.
     *
     * @return the next attack state.
     */
    @Override
    public IAIState getAttackState()
    {
        return null;
    }

    /**
     * Move the guard into a good attacking position.
     */
    @Override
    public void moveInAttackPosition()
    {

    }

    /**
     * Check if the worker has his main weapon.
     *
     * @return true if so.
     */
    @Override
    public boolean hasMainWeapon()
    {
        return false;
    }

    /**
     * Wears the weapon of the guard.
     */
    @Override
    public void wearWeapon()
    {

    }

    /**
     * Method which calculates the possible attack range in Blocks.
     *
     * @return the calculated range.
     */
    @Override
    protected int getAttackRange()
    {
        return 0;
    }

    /**
     * Can be overridden in implementations to return the exact building type the worker expects.
     *
     * @return the building type associated with this AI's worker.
     */
    @Override
    public Class<AbstractBuildingGuards> getExpectedBuildingClass()
    {
        return null;
    }
}
