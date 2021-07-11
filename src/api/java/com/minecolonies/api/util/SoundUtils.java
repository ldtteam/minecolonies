package com.minecolonies.api.util;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.sounds.EventType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.NoteBlockEvent.Note;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.sounds.ModSoundEvents.CITIZEN_SOUND_EVENTS;

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
     * A much less chaotic scale (D major pentatonic) for random pitches
     */
    public static final Note[] PENTATONIC = {
      // First Octave
      Note.A, Note.B, Note.D, Note.E, Note.F_SHARP,
      // Second Octave
      Note.A, Note.B, Note.D
    };

    /**
     * The minimum required distance to play a sound to a player
     */
    private static final double MIN_REQUIRED_SOUND_DIST = 10 * 10;

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
     *
     * @param worldIn the world to play it in.
     * @param pos     the pos to play it at.
     * @param citizen the citizen to play it for.
     */
    public static void playRandomSound(@NotNull final World worldIn, @NotNull final BlockPos pos, @NotNull final ICitizenData citizen)
    {
        boolean playerCloseEnough = false;
        for (final PlayerEntity player : citizen.getColony().getPackageManager().getCloseSubscribers())
        {
            if (player.blockPosition().distSqr(pos) < MIN_REQUIRED_SOUND_DIST)
            {
                playerCloseEnough = true;
                break;
            }
        }

        if (!playerCloseEnough)
        {
            return;
        }

        final double v = rand.nextDouble();
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
            if (citizen.getCitizenHappinessHandler().getHappiness(citizen.getColony()) < 5)
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
        else if (v <= 0.4 && citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick())
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
     * Play a success sound.
     * @param player the player to play it for.
     * @param position the position it is played at.
     */
    public static void playSuccessSound(@NotNull final PlayerEntity player, @NotNull final BlockPos position)
    {
        if (player instanceof ServerPlayerEntity)
        {
            ((ServerPlayerEntity) player).connection.send(new SPlaySoundEffectPacket(SoundEvents.NOTE_BLOCK_BELL,
              SoundCategory.NEUTRAL,
              position.getX(),
              position.getY(),
              position.getZ(),
              (float) VOLUME * 2,
              (float) 1.0));
        }
    }

    /**
     * Play an error sound.
     * @param player the player to play it for.
     * @param position the position it is played at.
     */
    public static void playErrorSound(@NotNull final PlayerEntity player, @NotNull final BlockPos position)
    {
        if (player instanceof ServerPlayerEntity)
        {
            ((ServerPlayerEntity) player).connection.send(new SPlaySoundEffectPacket(SoundEvents.NOTE_BLOCK_DIDGERIDOO,
              SoundCategory.NEUTRAL,
              position.getX(),
              position.getY(),
              position.getZ(),
              (float) VOLUME * 2,
              (float) 0.3));
        }
    }

    /**
     * Plays a sound with a certain chance at a certain position.
     *
     * @param worldIn     the world to play the sound in.
     * @param position    position to play the sound at.
     * @param type        sound to play.
     * @param citizenData the citizen.
     */
    public static void playSoundAtCitizenWith(
      @NotNull final World worldIn,
      @NotNull final BlockPos position,
      @Nullable final EventType type,
      @Nullable final ICitizenData citizenData)
    {
        if (citizenData == null)
        {
            return;
        }

        final Map<EventType, Tuple<SoundEvent, SoundEvent>> map;
        if (citizenData.getJob() != null)
        {
            map = CITIZEN_SOUND_EVENTS.get(citizenData.getJob().getJobRegistryEntry().getRegistryName().getPath());
        }
        else
        {
            map = CITIZEN_SOUND_EVENTS.get(citizenData.isChild() ? "child" : "citizen");
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
     *
     * @param random the random method.
     * @return a random double for the pitch.
     */
    public static double getRandomPitch(final Random random)
    {
        return PITCH_DIVIDER / (random.nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }

    /**
     * Generates a random tone from the D major pentatonic scale
     *
     * @param random the RNG instance
     * @return a number representing the pitch to the sound engine
     */
    public static double getRandomPentatonic(final Random random)
    {
        int index = random.nextInt(PENTATONIC.length);
        int tone = PENTATONIC[index].ordinal() + Math.floorDiv(index, 5) * 12;
        return Math.pow(2.0D, (double)(tone - 12) / 12.0D);
    }

    /**
     * Plays a sound for the given player, but not for surrounding entities
     */
    public static void playSoundForPlayer(final ServerPlayerEntity playerEntity, final SoundEvent sound, float volume, final float pitch)
    {
        playerEntity.connection.send(new SPlaySoundEffectPacket(sound,
          playerEntity.getSoundSource(),
          playerEntity.getX(),
          playerEntity.getY(),
          playerEntity.getZ(),
          16.0F * volume,
          pitch));
    }
}
