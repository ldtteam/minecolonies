package com.minecolonies.api.sounds;

import com.minecolonies.api.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class used to store the basic citizen sounds.
 */
public final class CitizenSounds extends AbstractWorkerSounds
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
     * Plays citizen sounds.
     *
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    @Override
    public void playSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        if (isFemale)
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.say, getPhraseChance());
        }
        else
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.say, getPhraseChance());
        }
    }

    @Override
    public String getWorkerString()
    {
        return "";
    }

    @Override
    public int getPhraseChance()
    {
        return PHRASE_CHANCE;
    }

    @Override
    public void playInteractionSound(final World world, final BlockPos position, final boolean female)
    {
        /**
         * Do nothing we don't have this implemented for this here yet.
         */
    }
}
