package com.minecolonies.api.sounds;

import net.minecraft.util.SoundEvent;

/**
 * Created by Asher on 12/6/17.
 */
public final class BarbarianSounds
{
    public static final SoundEvent barbarianHurt  = ModSoundEvents.getSoundID("mob.barbarian.hurt");
    public static final SoundEvent barbarianDeath = ModSoundEvents.getSoundID("mob.barbarian.death");
    public static final SoundEvent barbarianSay   = ModSoundEvents.getSoundID("mob.barbarian.say");

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianSounds()
    {
    }
}
