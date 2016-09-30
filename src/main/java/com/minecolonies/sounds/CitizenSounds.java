package com.minecolonies.sounds;

import com.minecolonies.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class used to store the basic citizen sounds.
 */
public class CitizenSounds
{
    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 50;

    /**
     * The citizen sound events for the females.
     */
    public static class Female
    {
        public static SoundEvent say;
    }

    /**
     * The citizen sound events for the males.
     */
    public static class Male
    {
        public static SoundEvent say;
    }

    /**
     * Plays fisherman sounds.
     * @param worldIn the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    public static void playCitizenSounds(World worldIn, BlockPos position, boolean isFemale)
    {
        if(isFemale)
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.say, PHRASE_CHANCE);
        }
        else
        {
            SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.say, PHRASE_CHANCE);
        }
    }

}
