package com.minecolonies.coremod.sounds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * I workerbuildings sounds interface which contains all methods workerbuildings sounds should have.
 */
public abstract class AbstractWorkerSounds
{
    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 25;

    /**
     * Chance to play a basic sound.
     */
    private static final int BASIC_SOUND_CHANCE = 100;

    /**
     * Plays the sounds for a certain workerbuildings.
     *
     * @param worldIn    the world to play the sound in.
     * @param position   the position to play the sound at.
     * @param isFemale   the gender.
     * @param saturation the saturation.
     */
    public abstract void playSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation);

    /**
     * Get the string describing the workerbuildings of the sound event.
     *
     * @return a string describing the workerbuildings.
     */
    public abstract String getWorkerString();

    /**
     * Get phrase chance.
     *
     * @return the chance to play a phrase.
     */
    public int getPhraseChance()
    {
        return PHRASE_CHANCE;
    }

    /**
     * Get basic sound chance.
     *
     * @return the chance to play a basic sound/noise.
     */
    public int getBasicSoundChance()
    {
        return BASIC_SOUND_CHANCE;
    }

    /**
     * Play an interaction sound for a certain workerbuildings.
     *
     * @param world    the world.
     * @param position the positon.
     * @param female   if female.
     */
    public abstract void playInteractionSound(final World world, final BlockPos position, final boolean female);
}
