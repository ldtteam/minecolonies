package com.minecolonies.coremod.sounds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * I worker sounds interface which contains all methods worker sounds should have.
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
     * Plays the sounds for a certain worker.
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     * @param saturation the saturation.
     */
    public abstract void playSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation);

    /**
     * Get the string describing the worker of the sound event.
     * @return a string describing the worker.
     */
    public abstract String getWorkerString();

    /**
     * Get phrase chance.
     * @return the chance to play a phrase.
     */
    public int getPhraseChance()
    {
        return PHRASE_CHANCE;
    }

    /**
     * Get basic sound chance.
     * @return the chance to play a basic sound/noise.
     */
    public int getBasicSoundChance()
    {
        return BASIC_SOUND_CHANCE;
    }
}
