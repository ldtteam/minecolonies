package com.minecolonies.api.entity.mobs;

import net.minecraft.sounds.SoundEvent;

/**
 * Used in by RaiderRangedAI to denote that the project has a custom firing sound
 */
public interface ICustomAttackSound
{
    /**
     * The custom sound event to be used instead of SoundEvents.SKELETON_SHOOT
     * @return The sound event to be played when used to attack
     */
    SoundEvent getAttackSound();
}
