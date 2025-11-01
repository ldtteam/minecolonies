package com.minecolonies.core.entity.ai.minimal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Similar to LookAtEntityGoal, just adds movement flags
 */
public class LookAtEntityInteractGoal extends LookAtEntityGoal
{
    public LookAtEntityInteractGoal(final Mob mob, final Class<? extends LivingEntity> lookAtType, final float lookDistance, final float probability)
    {
        super(mob, lookAtType, lookDistance, probability);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}
