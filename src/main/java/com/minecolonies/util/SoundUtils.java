package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Utilities for playing sounds.
 */
public final class SoundUtils
{
    protected static Random rand = new Random();

    private static final double STATIC_PITCH_VALUE = 0.9D;

    private static final double VOLUME = 0.2D;

    /**
     * Private constructor to hide the implicit public one
     */
    private SoundUtils()
    {
    }

    public static void playRandomSound(World worldIn, EntityCitizen citizen)
    {
        String prefix;

        if(citizen.isFemale())
        {
            prefix= "female";
        }
        ModSoundEvents.fisherman.bla;

        playSoundAtCitizen(worldIn, citizen, ModSoundEvents.femaleFishermanCallingItADay);
    }

    public static void playSoundAtCitizen(World worldIn, EntityCitizen citizen, SoundEvent event)
    {
        worldIn.playSound((EntityPlayer) null,
                citizen.getPosition(),
                event,
                SoundCategory.NEUTRAL,
                (float) VOLUME,
                (float) ((rand.nextGaussian() * 0.7D + 1.0D) * 2.0D));
    }
}
