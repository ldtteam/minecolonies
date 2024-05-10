package com.minecolonies.api.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This is a sound manager that allows playing a queue of sounds, for a specific time with length between a series of sounds.
 */
public class SoundManager
{
    /**
     * The queue to play from.
     */
    private final Deque<TimedSound> soundQueue = new ArrayDeque<>();

    /**
     * The client level.
     */
    private final ClientLevel level;

    /**
     * Create a new instance of the sound manager.
     * @param level the client level it belongs to.
     */
    public SoundManager(final ClientLevel level)
    {
        this.level = level;
    }

    /**
     * Tick the sound queue to play a sound.
     */
    public void tick()
    {
        if (soundQueue.isEmpty())
        {
            return;
        }

        final TimedSound instance = soundQueue.peek();
        if (instance.timeout <= 0)
        {
            level.playSound(Minecraft.getInstance().player, instance.pos, instance.soundEvent, instance.source, instance.volume, instance.pitch);
            instance.timeout = instance.length;
            instance.repetitions--;
            if (instance.repetitions < 0)
            {
                soundQueue.pop();
            }
        }
        else
        {
            instance.timeout--;
        }
    }

    /**
     * Add a new sound to the queue.
     * @param soundEvent the sound event to play.
     * @param source the sound source.
     * @param repetitions the number of repetitions of it.
     * @param length the length of it.
     * @param pos the pos to play it at.
     * @param pitch the pitch to play it with.
     * @param volume the volume to play it at.
     */
    public void addToQueue(final SoundEvent soundEvent, final SoundSource source, final int repetitions, final int length, final BlockPos pos, final float volume, final float pitch)
    {
        soundQueue.add(new TimedSound(soundEvent, source, repetitions, length, pos, volume, pitch));
    }

    /**
     * Storage container for a specific sound.
     */
    public static class TimedSound
    {
        final SoundEvent soundEvent;
        final SoundSource source;
        final int length;
        final BlockPos pos;
        final float volume;
        final float pitch;

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
