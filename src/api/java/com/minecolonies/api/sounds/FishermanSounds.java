package com.minecolonies.api.sounds;

import com.minecolonies.api.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Class containing the fisherman sounds.
 */
public final class FishermanSounds extends AbstractWorkerSounds
{
    /**
     * Number of different sounds in this class.
     */
    private static final int NUMBER_OF_SOUNDS = 2;

    /**
     * Random generator.
     */
    private static final Random rand = new Random();

    /**
     * Containing the female fisherman sounds.
     */
    public static final class Female
    {
        public static final SoundEvent generalPhrases = ModSoundEvents.getSoundID("mob.fisherman.female.generalphrases");
        public static final SoundEvent noises         = ModSoundEvents.getSoundID("mob.fisherman.female.noise");
        public static final SoundEvent iGotOne        = ModSoundEvents.getSoundID("mob.fisherman.female.igotone");
        public static final SoundEvent needFishingRod = ModSoundEvents.getSoundID("mob.fisherman.female.needfishingrod");
        public static final SoundEvent offToBed       = ModSoundEvents.getSoundID("mob.fisherman.female.offtobed");
        public static final SoundEvent badWeather     = ModSoundEvents.getSoundID("mob.fisherman.female.badweather");

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
        public static final SoundEvent generalPhrases = ModSoundEvents.getSoundID("mob.fisherman.male.generalphrases");
        public static final SoundEvent noises         = ModSoundEvents.getSoundID("mob.fisherman.male.noise");
        public static final SoundEvent iGotOne        = ModSoundEvents.getSoundID("mob.fisherman.male.igotone");
        public static final SoundEvent needFishingRod = ModSoundEvents.getSoundID("mob.fisherman.male.needfishingrod");
        public static final SoundEvent offToBed       = ModSoundEvents.getSoundID("mob.fisherman.male.offtobed");
        public static final SoundEvent badWeather     = ModSoundEvents.getSoundID("mob.fisherman.male.badweather");

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
     *
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    @Override
    public void playSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        //Leaving it as switch-case we may add further random sound categories here (Whistling, singing, etc).
        switch (rand.nextInt(NUMBER_OF_SOUNDS + 1))
        {
            case 1:
                final SoundEvent generalPhrases = isFemale ? FishermanSounds.Female.generalPhrases : FishermanSounds.Male.generalPhrases;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, generalPhrases, getPhraseChance());
                break;
            case 2:
                final SoundEvent noises = isFemale ? FishermanSounds.Female.noises : FishermanSounds.Male.noises;
                SoundUtils.playSoundAtCitizenWithChance(worldIn, position, noises, getBasicSoundChance());
                break;
            default:
                break;
        }
    }

    @Override
    public String getWorkerString()
    {
        return "Fisherman";
    }

    @Override
    public void playInteractionSound(final World world, final BlockPos position, final boolean female)
    {
        /**
         * Do nothing, we have nothing for this worker.
         */
    }
}
