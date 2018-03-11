package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.sounds.AbstractWorkerSounds;
import com.minecolonies.coremod.sounds.ModSoundEvents;
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
     * Guard tower job literal.
     */
    private static final String GUARD_TOWER = "GuardTower";

    /**
     * Get a random between 1 and 100.
     */
    private static final int ONE_HUNDRED = 100;

    /**
     * Standard pitch value.
     */
    public static final double PITCH = 0.9D;

    /**
     * Random object.
     */
    private static final Random rand = new Random();

    /**
     * Volume to play at.
     */
    public static final double VOLUME = 0.5D;

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
     * @param worldIn    the world to play the sound in.
     * @param citizen    the citizen to play the sound for.
     * @param saturation the saturation of the citizen.
     */
    public static void playRandomSound(@NotNull final World worldIn, @NotNull final EntityCitizen citizen, final double saturation)
    {
        if (1 >= rand.nextInt(CHANCE_TO_PLAY_SOUND))
        {
            String prefix = "";

            if (citizen.getWorkBuilding() != null)
            {
                prefix = citizen.getWorkBuilding().getJobName();
            }

            if (GUARD_TOWER.equals(prefix) && citizen.getWorkBuilding() instanceof AbstractBuildingGuards)
            {
                if (((AbstractBuildingGuards) citizen.getWorkBuilding()).getJob() == AbstractBuildingGuards.GuardJob.RANGER)
                {
                    prefix = "archer";
                }
                else
                {
                    prefix = "knight";
                }
            }

            for (final AbstractWorkerSounds sounds : ModSoundEvents.handlers)
            {
                if (sounds.getWorkerString().equals(prefix))
                {
                    sounds.playSound(worldIn, citizen.getPosition(), citizen.isFemale(), saturation);
                }
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

    /**
     * Play an interaction sound with chance at the citizen.
     *
     * @param world    the world.
     * @param position the position.
     * @param chance   the chance.
     * @param citizen  the citizen.
     */
    public static void playInteractionSoundAtCitizenWithChance(@NotNull final World world, @NotNull final BlockPos position, final int chance, @NotNull final EntityCitizen citizen)
    {
        if (chance > rand.nextInt(ONE_HUNDRED))
        {
            String prefix = "";

            if (citizen.getWorkBuilding() != null)
            {
                prefix = citizen.getWorkBuilding().getJobName();
            }

            if ("GuardTower".equals(prefix) && citizen.getWorkBuilding() instanceof AbstractBuildingGuards)
            {
                if (((AbstractBuildingGuards) citizen.getWorkBuilding()).getJob() == AbstractBuildingGuards.GuardJob.RANGER)
                {
                    prefix = "archer";
                }
                else
                {
                    prefix = "knight";
                }
            }

            for (final AbstractWorkerSounds sounds : ModSoundEvents.handlers)
            {
                if (sounds.getWorkerString().equals(prefix))
                {
                    sounds.playInteractionSound(world, citizen.getPosition(), citizen.isFemale());
                }
            }
        }
    }
}
