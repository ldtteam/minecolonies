package com.minecolonies.sounds;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.SoundUtils;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Class containing the fisherman sounds.
 */
public final class FishermanSounds
{
    /**
     * Random generator.
     */
    private static Random rand = new Random();

    /**
     * Number of different sounds in this class.
     */
    private static final int NUMBER_OF_SOUNDS = 2;

    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 25;

    /**
     * Chance to play a basic sound.
     */
    private static final int BASIC_SOUND_CHANCE = 100;

    /**
     * Private constructor to hide the implicit public one.
     */
    private FishermanSounds()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Containing the female fisherman sounds.
     */
    public static final class Female
    {
        public static final String generalPhrases = Constants.MOD_ID + ":mob.fisherman.female.generalPhrases";
        public static final String noises = Constants.MOD_ID + ":mob.fisherman.female.noise";
        public static final String iGotOne = Constants.MOD_ID + ":mob.fisherman.female.iGotOne";
        public static final String needFishingRod = Constants.MOD_ID + ":mob.fisherman.female.needFishingRod";
        public static final String offToBed = Constants.MOD_ID + ":mob.fisherman.female.offToBed";
        public static final String badWeather = Constants.MOD_ID + ":mob.fisherman.female.badWeather";

        /**
         * Private constructor to hide the implicit public one.
         */
        private Female()
        {
            /*
              Intentionally left empty.
             */
        }
    }

    /**
     * Containing the male fisherman sounds.
     */
    public static final class Male
    {
        public static final String generalPhrases = Constants.MOD_ID + ":mob.fisherman.male.generalPhrases";
        public static final String noises = Constants.MOD_ID + ":mob.fisherman.male.noise";
        public static final String iGotOne = Constants.MOD_ID + ":mob.fisherman.male.iGotOne";
        public static final String needFishingRod = Constants.MOD_ID + ":mob.fisherman.male.needFishingRod";
        public static final String offToBed = Constants.MOD_ID + ":mob.fisherman.male.offToBed";
        public static final String badWeather = Constants.MOD_ID + ":mob.fisherman.male.badWeather";

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
     * Plays fisherman sounds.
     * @param worldIn the world to play the sound in.
     * @param citizen the entity to play the sound at.
     * @param isFemale the gender.
     */
    public static void playFishermanSound(World worldIn, EntityCitizen citizen, boolean isFemale)
    {
        //Leaving it as switch-case we may add further random sound categories here (Whistling, singing, etc).
        switch (rand.nextInt(NUMBER_OF_SOUNDS + 1))
        {
            case 1:
                final String generalPhrases = isFemale ? FishermanSounds.Female.generalPhrases : FishermanSounds.Male.generalPhrases;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, citizen, generalPhrases, PHRASE_CHANCE);
                break;
            case 2:
                final String noises = isFemale ? FishermanSounds.Female.noises : FishermanSounds.Male.noises;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, citizen, noises, BASIC_SOUND_CHANCE);
                break;
        }
    }
}
