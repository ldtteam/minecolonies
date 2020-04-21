package com.minecolonies.api.util;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.sounds.EventType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.sounds.ModSoundEvents.SOUND_EVENTS;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

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
     * The base pitch, add more to this to change the sound.
     */
    private static final double BASE_PITCH = 0.8D;

    /**
     * The pitch will be divided by this to calculate it for the arrow sound.
     */
    private static final double PITCH_DIVIDER = 1.0D;

    /**
     * Random is multiplied by this to get a random sound.
     */
    private static final double PITCH_MULTIPLIER = 0.4D;

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
     * Play a random sound at the citizen.
     * @param worldIn the world to play it in.
     * @param pos the pos to play it at.
     * @param citizen the citizen to play it for.
     */
    public static void playRandomSound(@NotNull final World worldIn, @NotNull final BlockPos pos, @NotNull final ICitizenData citizen)
    {
        final double v = rand.nextDouble() * TICKS_SECOND;
        if (v <= 0.1)
        {
            if (citizen.getSaturation() < 2)
            {
                playSoundAtCitizenWith(worldIn, pos, EventType.SATURATION_LOW, citizen);
            }
            else
            {
                playSoundAtCitizenWith(worldIn, pos, EventType.SATURATION_HIGH, citizen);
            }
        }
        else if (v <= 0.2)
        {
            if (citizen.getCitizenHappinessHandler().getHappiness() < 5)
            {
                playSoundAtCitizenWith(worldIn, pos, EventType.UNHAPPY, citizen);
            }
            else
            {
                playSoundAtCitizenWith(worldIn, pos, EventType.HAPPY, citizen);
            }
        }
        else if (v <= 0.3)
        {
            playSoundAtCitizenWith(worldIn, pos, EventType.GENERAL, citizen);
        }
        else if (v <= 0.4 && citizen.getCitizenEntity().isPresent() && citizen.getCitizenEntity().get().getCitizenDiseaseHandler().isSick())
        {
            playSoundAtCitizenWith(worldIn, pos, EventType.SICKNESS, citizen);
        }
        else if (v <= 0.5 && (citizen.getHomeBuilding() == null || citizen.getHomeBuilding().getBuildingLevel() <= 2))
        {
            playSoundAtCitizenWith(worldIn, pos, EventType.BAD_HOUSING, citizen);
        }
        else if (v <= 0.6 && worldIn.isRaining())
        {
            playSoundAtCitizenWith(worldIn, pos, EventType.BAD_WEATHER, citizen);
        }
        else if (v <= 1.0)
        {
            playSoundAtCitizenWith(worldIn, pos, EventType.NOISE, citizen);
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
        worldIn.playSound(null,
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
     * @param type    sound to play.
     * @param citizenData the citizen.
     */
    public static void playSoundAtCitizenWith(@NotNull final World worldIn, @NotNull final BlockPos position, @Nullable final EventType type, @Nullable final ICitizenData citizenData)
    {
        if (citizenData == null)
        {
            return;
        }

        if (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().disableCitizenVoices.get())
        {
            return;
        }

        final Map<EventType, Tuple<SoundEvent, SoundEvent>> map;
        if (citizenData.getJob() != null)
        {
            map = SOUND_EVENTS.get(citizenData.getJob().getJobRegistryEntry().getRegistryName().getPath());
        }
        else
        {
            map = SOUND_EVENTS.get(citizenData.isChild() ? "child" : "citizen");
        }

        final SoundEvent event = citizenData.isFemale() ? map.get(type).getB() : map.get(type).getA();

        if (type.getChance() > rand.nextDouble() * ONE_HUNDRED)
        {
            worldIn.playSound(null,
              position,
              event,
              SoundCategory.NEUTRAL,
              (float) VOLUME,
              (float) PITCH);
        }
    }

    /**
     * Get a random pitch for a sound.
     * @param random the random method.
     * @return a random double for the pitch.
     */
    public static double getRandomPitch(final Random random)
    {
        return PITCH_DIVIDER / (random.nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
