package com.minecolonies.api.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.*;

/**
 * This is a sound manager that allows playing a queue of sounds, for a specific time with length between a series of sounds.
 */
public class SoundManager
{
    /**
     * Max concurrent sounds.
     */
    private static final int MAX_CONCURRENT_SOUNDS = 5;

    /**
     * The queue to play from.
     */
    private static final Map<UUID, Deque<TimedSound>> soundQueue = new HashMap<>();

    /**
     * Tick the sound queue to play a sound.
     */
    public static void tick()
    {
        if (soundQueue.isEmpty())
        {
            return;
        }

        if (Minecraft.getInstance().level == null)
        {
            soundQueue.clear();
            return;
        }

        int playedSounds = 1;
        for (final Map.Entry<UUID, Deque<TimedSound>> entries : soundQueue.entrySet())
        {
            if (entries.getValue().isEmpty())
            {
                continue;
            }
            final TimedSound instance = entries.getValue().peek();
            if (instance.timeout <= 0)
            {
                Minecraft.getInstance().player.level().playSound(Minecraft.getInstance().player,
                  instance.pos,
                  instance.soundEvent,
                  instance.source,
                  instance.volume,
                  instance.pitch);
                instance.timeout = instance.length;
                instance.repetitions--;
                if (instance.repetitions < 0)
                {
                    entries.getValue().pop();
                }
            }
            else
            {
                instance.timeout--;
            }
            playedSounds++;
            if (playedSounds >= MAX_CONCURRENT_SOUNDS)
            {
                break;
            }
        }
    }

    /**
     * Add a new sound to the queue.
     *
     * @param soundEvent  the sound event to play.
     * @param source      the sound source.
     * @param repetitions the number of repetitions of it.
     * @param length      the length of it.
     * @param pos         the pos to play it at.
     * @param pitch       the pitch to play it with.
     * @param volume      the volume to play it at.
     */
    public static void addToQueue(
      final UUID uuid,
      final SoundEvent soundEvent,
      final SoundSource source,
      final int repetitions,
      final int length,
      final BlockPos pos,
      final float volume,
      final float pitch)
    {
        final Deque<TimedSound> queue = soundQueue.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        queue.add(new TimedSound(soundEvent, source, repetitions, length, pos, volume, pitch));
    }

    /**
     * Storage container for a specific sound.
     */
    public static class TimedSound
    {
        final SoundEvent  soundEvent;
        final SoundSource source;
        final int         length;
        final BlockPos    pos;
        final float       volume;
        final float       pitch;

        int repetitions;
        int timeout = 0;

        public TimedSound(final SoundEvent soundEvent, final SoundSource source, final int repetitions, final int length, final BlockPos pos, final float volume, final float pitch)
        {
            this.soundEvent = soundEvent;
            this.source = source;
            this.repetitions = repetitions;
            this.length = length;
            this.pos = pos;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
