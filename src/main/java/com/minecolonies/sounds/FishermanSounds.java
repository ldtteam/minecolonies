package com.minecolonies.sounds;

import com.minecolonies.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
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
        public static final SoundEvent generalPhrases = ModSoundEvents.registerSound("mob.fisherman.female.generalPhrases");
        public static final SoundEvent noises = ModSoundEvents.registerSound("mob.fisherman.female.noise");
        public static final SoundEvent iGotOne = ModSoundEvents.registerSound("mob.fisherman.female.iGotOne");
        public static final SoundEvent needFishingRod = ModSoundEvents.registerSound("mob.fisherman.female.needFishingRod");
        public static final SoundEvent offToBed = ModSoundEvents.registerSound("mob.fisherman.female.offToBed");
        public static final SoundEvent badWeather = ModSoundEvents.registerSound("mob.fisherman.female.badWeather");

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
        public static final SoundEvent generalPhrases = ModSoundEvents.registerSound("mob.fisherman.male.generalPhrases");
        public static final SoundEvent noises = ModSoundEvents.registerSound("mob.fisherman.male.noise");
        public static final SoundEvent iGotOne = ModSoundEvents.registerSound("mob.fisherman.male.iGotOne");
        public static final SoundEvent needFishingRod = ModSoundEvents.registerSound("mob.fisherman.male.needFishingRod");
        public static final SoundEvent offToBed = ModSoundEvents.registerSound("mob.fisherman.male.offToBed");
        public static final SoundEvent badWeather = ModSoundEvents.registerSound("mob.fisherman.male.badWeather");

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
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    public static void playFishermanSound(World worldIn, BlockPos position, boolean isFemale)
    {
        if(isFemale)
        {
            //Leaving it as switch-case we may add further random sound categories here (Whistling, singing, etc).
            switch(rand.nextInt(NUMBER_OF_SOUNDS+1))
            {
                case 1:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.generalPhrases, PHRASE_CHANCE);
                    break;
                case 2:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.noises, BASIC_SOUND_CHANCE);
                    break;
            }

            return;
        }

        switch(rand.nextInt(NUMBER_OF_SOUNDS+1))
        {
            case 1:
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.generalPhrases, PHRASE_CHANCE);
                break;
            case 2:
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.noises, BASIC_SOUND_CHANCE);
                break;
        }
    }
}
