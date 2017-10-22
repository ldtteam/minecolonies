package com.minecolonies.coremod.sounds;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Class containing the baker sounds.
 */
public final class BakerSounds extends AbstractWorkerSounds
{
    /**
     * Number of different sounds in this class.
     */
    private static final int NUMBER_OF_SOUNDS = 3;

    /**
     * Random generator.
     */
    private static final Random rand = new Random();

    /**
     * Containing the female baker sounds.
     */
    public static final class Female
    {
        public static final SoundEvent generalPhrases    = ModSoundEvents.getSoundID("mob.baker.female.generalPhrases");
        public static final SoundEvent noises            = ModSoundEvents.getSoundID("mob.baker.female.noise");
        public static final SoundEvent hostile           = ModSoundEvents.getSoundID("mob.baker.female.hostile");
        public static final SoundEvent offToBed          = ModSoundEvents.getSoundID("mob.baker.female.offToBed");
        public static final SoundEvent badWeather        = ModSoundEvents.getSoundID("mob.baker.female.badWeather");
        public static final SoundEvent saturationVeryLow = ModSoundEvents.getSoundID("mob.baker.female.saturationVeryLow");
        public static final SoundEvent saturationLow     = ModSoundEvents.getSoundID("mob.baker.female.saturationLow");
        public static final SoundEvent saturationHigh    = ModSoundEvents.getSoundID("mob.baker.female.saturationHigh");
        public static final SoundEvent greeting          = ModSoundEvents.getSoundID("mob.baker.female.greeting");
        public static final SoundEvent farewell          = ModSoundEvents.getSoundID("mob.baker.female.farewell");
        public static final SoundEvent interaction       = ModSoundEvents.getSoundID("mob.baker.female.interaction");

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
     * Containing the male baker sounds.
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
     * Plays baker sounds.
     * Suppressing Sonar Rule squid:S109
     * This rule wants to prevent magic numbers
     * But in this case the rule does not apply because its not a magic number its a % chance.
     * For every sound category we have 1 increasing number.
     *
     * @param worldIn    the world to play the sound in.
     * @param position   the position to play the sound at.
     * @param isFemale   the gender.
     * @param saturation the saturation.
     */
    @SuppressWarnings("squid:S109")
    @Override
    public void playSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        //While there are no male sounds
        if (!isFemale)
        {
            return;
        }
        //Leaving it as switch-case we may add further random sound categories here (Whistling, singing, etc).
        switch (rand.nextInt(NUMBER_OF_SOUNDS + 1))
        {
            case 1:
                final SoundEvent noises = BakerSounds.Female.noises;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, noises, getBasicSoundChance());
                break;
            case 2:
                playSaturationSound(worldIn, position, isFemale, saturation);
                break;
            default:
                final SoundEvent generalPhrases = BakerSounds.Female.generalPhrases;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, generalPhrases, getPhraseChance());
                break;
        }
    }

    @Override
    public String getWorkerString()
    {
        return "Baker";
    }

    /**
     * Play interaction sound.
     *
     * @param worldIn  world to play it in.
     * @param position position to play it at.
     * @param isFemale the gender.
     */
    @Override
    public void playInteractionSound(final World worldIn, final BlockPos position, final boolean isFemale)
    {
        //While there are no male sounds
        if (!isFemale)
        {
            return;
        }

        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, BakerSounds.Female.interaction, getBasicSoundChance());
    }

    /**
     * Play the saturation sound depending on the saturation.
     *
     * @param worldIn    world to play it in.
     * @param position   position to play it at.
     * @param isFemale   the gender.
     * @param saturation the saturation.
     */
    public void playSaturationSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        //While there are no male sounds
        if (!isFemale)
        {
            return;
        }

        final SoundEvent saturationFeedback;
        if (saturation < EntityCitizen.LOW_SATURATION)
        {
            saturationFeedback = Female.saturationVeryLow;
        }
        else if (saturation < EntityCitizen.AVERAGE_SATURATION)
        {
            saturationFeedback = Female.saturationLow;
        }
        else
        {
            saturationFeedback = Female.saturationHigh;
        }
        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, saturationFeedback, getBasicSoundChance());
    }
}
