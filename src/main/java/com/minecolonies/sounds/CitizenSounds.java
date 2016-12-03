package com.minecolonies.sounds;

import com.minecolonies.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class used to store the basic citizen sounds.
 */
public final class CitizenSounds
{
    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 50;

    /**
     * The citizen sound events for the females.
     */
    public static final class Female
    {
        public static final SoundEvent say = ModSoundEvents.getSoundID("mob.citizen.female.say");

        /**
         * Private constructor to hide the implicit public one.
         */
        private Female()
        {
            /*
             * Intentionally left empty.
             */
        }
    }

    /**
     * The citizen sound events for the males.
     */
    public static final class Male
    {
        public static final SoundEvent say = ModSoundEvents.getSoundID("mob.citizen.male.say");

        /**
         * Private constructor to hide the implicit public one.
         */
        private Male()
        {
           /*
            * Intentionally left empty.
            */
        }
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private CitizenSounds()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Plays citizen sounds.
     *
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    public static void playCitizenSounds(final World worldIn, final BlockPos position, final boolean isFemale)
    {
        if (isFemale)
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.say, PHRASE_CHANCE);
        }
        else
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.say, PHRASE_CHANCE);
        }
    }
}
