package com.minecolonies.sounds;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.SoundUtils;
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
     * Private constructor to hide the implicit public one.
     */
    private CitizenSounds()
    {
        /*
         * Intentionally left empty.
         */
    }
    /**
     * The citizen sound events for the females.
     */
    public static final class Female
    {
        public static final String say = Constants.MOD_ID + ":mob.citizen.female.say";


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
        public static final String say = Constants.MOD_ID + ":mob.citizen.male.say";

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
     * Plays citizen sounds.
     * @param worldIn the world to play the sound in.
     * @param citizen the citizen to play the sound at.
     * @param isFemale the gender.
     */
    public static void playCitizenSounds(World worldIn, EntityCitizen citizen, boolean isFemale)
    {
        if(isFemale)
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, citizen, Female.say, PHRASE_CHANCE);
        }
        else
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, citizen, Male.say, PHRASE_CHANCE);
        }
    }

}
