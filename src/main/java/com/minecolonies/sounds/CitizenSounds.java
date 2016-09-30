package com.minecolonies.sounds;

import com.minecolonies.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Class used to store the basic citizen sounds.
 */
public class CitizenSounds
{
    /**
     * Random generator.
     */
    private static Random rand = new Random();

    /**
     * Number of different sounds in this class.
     */
    private static final int NUMBER_OF_SOUNDS = 3;

    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 50;

    /**
     * Chance to play a basic sound.
     */
    private static final int BASIC_SOUND_CHANCE = 100;

    /**
     * The citizen sound events for the females.
     */
    public static class Female
    {
        public static SoundEvent say1;
        public static SoundEvent say2;
        public static SoundEvent say3;
    }

    /**
     * The citizen sound events for the males.
     */
    public static class Male
    {
        public static SoundEvent say1;
        public static SoundEvent say2;
        public static SoundEvent say3;
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
            switch (rand.nextInt(NUMBER_OF_SOUNDS+1))
            {
                case 1:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.say1, PHRASE_CHANCE);
                    break;
                case 2:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.say2, PHRASE_CHANCE);
                    break;
                case 3:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.say3, PHRASE_CHANCE);
                    break;
            }
        }
        else
        {
            switch (rand.nextInt(NUMBER_OF_SOUNDS+1))
            {
                case 1:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.say1, PHRASE_CHANCE);
                    break;
                case 2:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.say2, PHRASE_CHANCE);
                    break;
                case 3:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Male.say3, PHRASE_CHANCE);
                    break;
            }
        }
    }

}
