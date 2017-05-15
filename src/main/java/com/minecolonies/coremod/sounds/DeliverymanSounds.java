package com.minecolonies.coremod.sounds;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Class containing the fisherman sounds.
 */
public final class DeliverymanSounds
{
    /**
     * Number of different sounds in this class.
     */
    private static final int    NUMBER_OF_SOUNDS   = 3;
    /**
     * Chance to say a phrase.
     */
    private static final int    PHRASE_CHANCE      = 25;
    /**
     * Chance to play a basic sound.
     */
    private static final int    BASIC_SOUND_CHANCE = 100;
    /**
     * Random generator.
     */
    private static final Random rand               = new Random();

    /**
     * Containing the female fisherman sounds.
     */
    public static final class Female
    {
        public static final SoundEvent generalPhrases = ModSoundEvents.getSoundID("mob.deliveryman.female.generalPhrases");
        public static final SoundEvent noises         = ModSoundEvents.getSoundID("mob.deliveryman.female.noise");
        public static final SoundEvent hostile        = ModSoundEvents.getSoundID("mob.deliveryman.female.hostile");
        public static final SoundEvent offToBed       = ModSoundEvents.getSoundID("mob.deliveryman.female.offToBed");
        public static final SoundEvent badWeather     = ModSoundEvents.getSoundID("mob.deliveryman.female.badWeather");
        public static final SoundEvent saturationVeryLow     = ModSoundEvents.getSoundID("mob.deliveryman.female.saturationVeryLow");
        public static final SoundEvent saturationLow     = ModSoundEvents.getSoundID("mob.deliveryman.female.saturationLow");
        public static final SoundEvent saturationHigh     = ModSoundEvents.getSoundID("mob.deliveryman.female.saturationHigh");

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
    private DeliverymanSounds()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Plays fisherman sounds.
     * We ignore the warning since the case integers counts for the amount of different sound events and we don't have to name them.
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     * @param saturation the saturation.
     */
    @SuppressWarnings("squid:S109")
    public static void playDmanSounds(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        //While there are no male sounds
        if(!isFemale)
        {
            return;
        }
        //Leaving it as switch-case we may add further random sound categories here (Whistling, singing, etc).
        switch (rand.nextInt(NUMBER_OF_SOUNDS + 1))
        {
            case 1:
                final SoundEvent noises = DeliverymanSounds.Female.noises;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, noises, BASIC_SOUND_CHANCE);
                break;
            case 2:
                playSaturationSound(worldIn, position, isFemale, saturation);
                break;
            default:
                final SoundEvent generalPhrases = DeliverymanSounds.Female.generalPhrases;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, generalPhrases, PHRASE_CHANCE);
                break;
        }
    }

    /**
     * Play the saturation sound depending on the saturation.
     * @param worldIn world to play it in.
     * @param position position to play it at.
     * @param isFemale the gender.
     * @param saturation the saturation.
     */
    public static void playSaturationSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        //While there are no male sounds
        if(!isFemale)
        {
            return;
        }

        final SoundEvent saturationFeedback;
        if(saturation < EntityCitizen.LOW_SATURATION)
        {
            saturationFeedback = Female.saturationVeryLow;
        }
        else if(saturation < EntityCitizen.AVERAGE_SATURATION)
        {
            saturationFeedback = Female.saturationLow;
        }
        else
        {
            saturationFeedback = Female.saturationHigh;
        }
        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, saturationFeedback, PHRASE_CHANCE);
    }
}
