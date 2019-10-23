package com.minecolonies.api.sounds;

import com.minecolonies.api.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.AVERAGE_SATURATION;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;

/**
 * Class containing the fisherman sounds.
 */
public final class FarmerSounds extends AbstractWorkerSounds
{
    /**
     * Number of different sounds in this class.
     */
    private static final int NUMBER_OF_SOUNDS = 5;

    /**
     * Random generator.
     */
    private static final Random rand = new Random();

    /**
     * Containing the female fisherman sounds.
     */
    public static final class Female
    {
        public static final SoundEvent generalPhrases    = ModSoundEvents.getSoundID("mob.farmer.female.generalphrases");
        public static final SoundEvent noises            = ModSoundEvents.getSoundID("mob.farmer.female.noise");
        public static final SoundEvent hostile           = ModSoundEvents.getSoundID("mob.farmer.female.hostile");
        public static final SoundEvent offToBed          = ModSoundEvents.getSoundID("mob.farmer.female.offtobed");
        public static final SoundEvent badWeather        = ModSoundEvents.getSoundID("mob.farmer.female.badweather");
        public static final SoundEvent saturationVeryLow = ModSoundEvents.getSoundID("mob.farmer.female.saturationverylow");
        public static final SoundEvent saturationLow     = ModSoundEvents.getSoundID("mob.farmer.female.saturationlow");
        public static final SoundEvent saturationHigh    = ModSoundEvents.getSoundID("mob.farmer.female.saturationhigh");
        public static final SoundEvent greeting          = ModSoundEvents.getSoundID("mob.farmer.female.greeting");
        public static final SoundEvent farewell          = ModSoundEvents.getSoundID("mob.farmer.female.farewell");
        public static final SoundEvent interaction       = ModSoundEvents.getSoundID("mob.farmer.female.interaction");

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
     * Plays Farmer sounds.
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
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, CitizenSounds.Male.say, getPhraseChance());
            return;
        }
        //Leaving it as switch-case we may add further random sound categories here (Whistling, singing, etc).
        switch (rand.nextInt(NUMBER_OF_SOUNDS + 1))
        {
            case 1:
                final SoundEvent noises = Female.noises;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, noises, getBasicSoundChance());
                break;
            case 2:
                playSaturationSound(worldIn, position, isFemale, saturation);
                break;
            case 3:
                final SoundEvent greeting = Female.greeting;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, greeting, getBasicSoundChance() * 2);
                break;
            case 4:
                final SoundEvent farewell = Female.farewell;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, farewell, getBasicSoundChance());
                break;
            default:
                final SoundEvent generalPhrases = Female.generalPhrases;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, generalPhrases, getPhraseChance());
                break;
        }
    }

    @Override
    public String getWorkerString()
    {
        return "Farmer";
    }

    /**
     * Play interaction sound.
     *
     * @param worldIn  world to play it in.
     * @param position position to play it at.
     * @param isFemale the gender.
     */
    public void playInteractionSound(final World worldIn, final BlockPos position, final boolean isFemale)
    {
        //While there are no male sounds
        if (!isFemale)
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, CitizenSounds.Male.say, getPhraseChance());
            return;
        }

        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.interaction, getBasicSoundChance());
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
        if (saturation < LOW_SATURATION)
        {
            saturationFeedback = Female.saturationVeryLow;
        }
        else if (saturation < AVERAGE_SATURATION)
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
