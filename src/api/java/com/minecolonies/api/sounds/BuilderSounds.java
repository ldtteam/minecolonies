package com.minecolonies.api.sounds;

import com.minecolonies.api.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.AVERAGE_SATURATION;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;

/**
 * Class containing the builder sounds.
 */
public final class BuilderSounds extends AbstractWorkerSounds
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
     * Containing the female builder sounds.
     */
    public static final class Female
    {
        public static final SoundEvent generalPhrases    = ModSoundEvents.getSoundID("mob.builder.female.generalphrases");
        public static final SoundEvent noises            = ModSoundEvents.getSoundID("mob.builder.female.noise");
        public static final SoundEvent hostile           = ModSoundEvents.getSoundID("mob.builder.female.hostile");
        public static final SoundEvent offToBed          = ModSoundEvents.getSoundID("mob.builder.female.offtobed");
        public static final SoundEvent badWeather        = ModSoundEvents.getSoundID("mob.builder.female.badweather");
        public static final SoundEvent saturationVeryLow = ModSoundEvents.getSoundID("mob.builder.female.saturationverylow");
        public static final SoundEvent saturationLow     = ModSoundEvents.getSoundID("mob.builder.female.saturationlow");
        public static final SoundEvent greeting          = ModSoundEvents.getSoundID("mob.builder.female.greeting");
        public static final SoundEvent farewell          = ModSoundEvents.getSoundID("mob.builder.female.farewell");
        public static final SoundEvent interaction       = ModSoundEvents.getSoundID("mob.builder.female.interaction");
        public static final SoundEvent complete          = ModSoundEvents.getSoundID("mob.builder.female.complete");

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
     * Containing the male builder sounds.
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
     * Plays builder sounds.
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
                final SoundEvent noises = BuilderSounds.Female.noises;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, noises, getBasicSoundChance());
                break;
            case 2:
                playSaturationSound(worldIn, position, isFemale, saturation);
                break;
            default:
                final SoundEvent generalPhrases = BuilderSounds.Female.generalPhrases;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, generalPhrases, getPhraseChance());
                break;
        }
    }

    @Override
    public String getWorkerString()
    {
        return "Builder";
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

        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, BuilderSounds.Female.interaction, getBasicSoundChance());
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
            return;
        }
        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, saturationFeedback, getBasicSoundChance());
    }
}
