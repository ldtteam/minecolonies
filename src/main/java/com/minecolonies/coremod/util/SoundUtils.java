package com.minecolonies.coremod.util;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.sounds.CitizenSounds;
import com.minecolonies.coremod.sounds.FishermanSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Utilities for playing sounds.
 */
public final class SoundUtils
{
    /**
     * Get a random between 1 and 100.
     */
    private static final int ONE_HUNDRED = 100;

    /**
     * Standard pitch value.
     */
    private static final double PITCH = 0.9D;

    /**
     * Random object.
     */
    private static final Random rand = new Random();

    /**
     * Volume to play at.
     */
    private static final double VOLUME = 0.5D;

    /**
     * in average 1 minute to the next sound which are 20 ticks the second * 60
     * seconds * 1 minute.
     */
    private static final int CHANCE_TO_PLAY_SOUND = 20 * 60 * 2;

    /**
     * Private constructor to hide the implicit public one.
     */
    private SoundUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Plays a random sound for a certain citizen.
     *
     * @param worldIn the world to play the sound in.
     * @param citizen the citizen to play the sound for.
     */
    public static void playRandomSound(@NotNull final World worldIn, @NotNull final EntityCitizen citizen)
    {
        if (1 >= rand.nextInt(CHANCE_TO_PLAY_SOUND))
        {
            String prefix = "";

            if (citizen.getWorkBuilding() != null)
            {
                prefix = citizen.getWorkBuilding().getJobName();
            }

            switch (prefix)
            {
                case "Fisherman":
                    FishermanSounds.playFishermanSound(worldIn, citizen.getPosition(), citizen.isFemale());
                    break;
                default:
                    CitizenSounds.playCitizenSounds(worldIn, citizen.getPosition(), citizen.isFemale());
                    break;
            }
        }
    }

    /**
     * Play a sound at a certain position.
     *
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param event    sound to play.
     */
    public static void playSoundAtCitizen(@NotNull final World worldIn, @NotNull final BlockPos position, @NotNull final SoundEvent event)
    {
        worldIn.playSound((EntityPlayer) null,
          position,
          event,
          SoundCategory.NEUTRAL,
          (float) VOLUME,
          (float) PITCH);
    }

    /**
     * Plays a sound with a certain chance at a certain position.
     *
     * @param worldIn  the world to play the sound in.
     * @param position position to play the sound at.
     * @param event    sound to play.
     * @param chance   chance in percent.
     */
    public static void playSoundAtCitizenWithChance(@NotNull final World worldIn, @NotNull final BlockPos position, @Nullable final SoundEvent event, final int chance)
    {
        if (event == null)
        {
            return;
        }

        if (chance > rand.nextInt(ONE_HUNDRED))
        {
            worldIn.playSound((EntityPlayer) null,
              position,
              event,
              SoundCategory.NEUTRAL,
              (float) VOLUME,
              (float) PITCH);
        }
    }
}
