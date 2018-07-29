package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.entity.ai.util.AIState;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIRanger extends AbstractEntityAIGuard<JobRanger>
{
    /**
     * This guard's minimum distance for attack.
     */
    private static final double MAX_DISTANCE_FOR_ATTACK = 5;

    /**
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIRanger(@NotNull final JobRanger job)
    {
        super(job);
        toolsNeeded.add(ToolType.BOW);
    }

    @Override
    protected int getAttackRange()
    {
        return (int) MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    protected AIState decide()
    {
        final AIState superState = super.decide();

        if (superState != DECIDE || target == null)
        {
            return superState;
        }

        return GUARD_ATTACK_RANGED;
    }
}
