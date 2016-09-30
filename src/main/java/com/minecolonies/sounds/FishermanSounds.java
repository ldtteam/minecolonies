package com.minecolonies.sounds;

import com.minecolonies.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Class containing the fisherman sounds.
 */
public class FishermanSounds
{
    /**
     * Random generator.
     */
    private static Random rand = new Random();

    /**
     * Number of different sounds in this class.
     */
    private static final int NUMBER_OF_SOUNDS = 19;

    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 25;

    /**
     * Chance to play a basic sound.
     */
    private static final int BASIC_SOUND_CHANCE = 100;

    /**
     * Containing the female fisherman sounds.
     */
    public static class Female
    {
        public static SoundEvent callingItADay;
        public static SoundEvent cheers;
        public static SoundEvent coughs;
        public static SoundEvent weWillGo;
        public static SoundEvent goodDayToYou;
        public static SoundEvent grumbles;
        public static SoundEvent haveAGoodDay;
        public static SoundEvent hey;
        public static SoundEvent ho_hum;
        public static SoundEvent iGotOne;
        public static SoundEvent lousyWheather;
        public static SoundEvent lovelyDay;
        public static SoundEvent needFishingRod;
        public static SoundEvent notBitingToday;
        public static SoundEvent offToBed; //todo
        public static SoundEvent offToFish;
        public static SoundEvent rowYourBoat;
        public static SoundEvent sighs;
        public static SoundEvent sniffles;
        public static SoundEvent theresFishToCatch;
        public static SoundEvent workWorkWork;
        public static SoundEvent yawns;
    }

    /**
     * Containing the male fisherman sounds.
     */
    public static class Male
    {

    }

    /**
     * Plays fisherman sounds.
     * @param worldIn the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    public static void playFishermanSound(World worldIn, BlockPos position, boolean isFemale)
    {
        if(isFemale)
        {
            switch(rand.nextInt(NUMBER_OF_SOUNDS))
            {
                case 1:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, FishermanSounds.Female.callingItADay, PHRASE_CHANCE);
                    break;
                case 2:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.cheers, BASIC_SOUND_CHANCE);
                    break;
                case 3:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.coughs, BASIC_SOUND_CHANCE);
                    break;
                case 4:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.weWillGo, PHRASE_CHANCE);
                    break;
                case 5:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.goodDayToYou, PHRASE_CHANCE);
                    break;
                case 6:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.grumbles, BASIC_SOUND_CHANCE);
                    break;
                case 7:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.haveAGoodDay, PHRASE_CHANCE);
                    break;
                case 8:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.hey, BASIC_SOUND_CHANCE);
                    break;
                case 9:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.ho_hum, BASIC_SOUND_CHANCE);
                    break;
                case 10:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.lousyWheather, PHRASE_CHANCE);
                    break;
                case 11:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.lovelyDay, PHRASE_CHANCE);
                    break;
                case 12:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.notBitingToday, PHRASE_CHANCE);
                    break;
                case 13:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.offToFish, BASIC_SOUND_CHANCE);
                    break;
                case 14:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.rowYourBoat, PHRASE_CHANCE);
                    break;
                case 15:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.sighs, BASIC_SOUND_CHANCE);
                    break;
                case 16:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.sniffles, BASIC_SOUND_CHANCE);
                    break;
                case 17:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.theresFishToCatch, PHRASE_CHANCE);
                    break;
                case 18:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.workWorkWork, PHRASE_CHANCE);
                    break;
                case 19:
                    SoundUtils.playSoundAtCitizenWithChance(worldIn, position, Female.yawns, BASIC_SOUND_CHANCE);
                    break;
            }

            return;
        }

        //Following the male sounds as soon as they are uploaded.

    }
}
