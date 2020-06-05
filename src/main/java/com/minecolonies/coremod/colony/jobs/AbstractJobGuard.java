package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import net.minecraft.util.DamageSource;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_SLEEP;

/**
 * Abstract Class for Guard Jobs.
 */
public abstract class AbstractJobGuard<J extends AbstractJobGuard<J>> extends AbstractJob<AbstractEntityAIGuard<J, ? extends AbstractBuildingGuards>, J>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobGuard(final ICitizenData entity)
    {
        super(entity);
    }

    protected abstract AbstractEntityAIGuard<J, ? extends AbstractBuildingGuards> generateGuardAI();

    @Override
    public AbstractEntityAIGuard<J, ? extends AbstractBuildingGuards> generateAI()
    {
        return generateGuardAI();
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
    }

    @Override
    public boolean allowsAvoidance()
    {
        return false;
    }

    /**
     * Whether the guard is asleep.
     *
     * @return true if sleeping
     */
    public boolean isAsleep()
    {
        return getWorkerAI() != null && getWorkerAI().getState() == GUARD_SLEEP;
    }
}
