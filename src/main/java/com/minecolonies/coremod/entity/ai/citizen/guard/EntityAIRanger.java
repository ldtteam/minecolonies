package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.util.AIState;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

public class EntityAIRanger extends AbstractEntityAIGuardNew
{
    /**
     * This guard's minimum distance for attack.
     */
    private static final double MAX_DISTANCE_FOR_ATTACK = 5;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIRanger(@NotNull final AbstractJobGuard job)
    {
        super(job);
        toolsNeeded.add(ToolType.BOW);
    }

    @Override
    protected AIState decide()
    {
        final AIState superState = super.decide();

        if (superState != DECIDE)
        {
            return superState;
        }

        System.out.println("RANGER ATTACK: " + target);
        target.attackEntityFrom(new DamageSource(worker.getName()), 10);

        return DECIDE;
    }
}
