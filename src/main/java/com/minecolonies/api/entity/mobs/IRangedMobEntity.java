package com.minecolonies.api.entity.mobs;

import net.minecraft.world.entity.monster.Enemy;

/**
 * Indicates the mob utilizes ranged weaponry
 */
public interface IRangedMobEntity extends Enemy
{
    /**
     * Modifier to ranged attack delays
     *
     * @return higher = longer delay, shorter = less delay
     */
    default double getAttackDelayModifier()
    {
        return 1;
    }

    /**
     * If the shooter penetrates fluids.
     * @return true if so.
     */
    default boolean penetrateFluids() { return true; }
}
