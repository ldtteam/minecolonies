package com.minecolonies.util;

import com.minecolonies.lib.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Utilities for playing sounds.
 */
public final class SoundUtils
{
    private static final double HALF_BLOCK_OFFSET  = 0.5D;
    private static final double STATIC_PITCH_VALUE = 0.9D;
    private static final double RANDOM_PITCH_VALUE = 0.1D;

    /**
     * Private constructor to hide the implicit public one
     */
    private SoundUtils()
    {
    }

    /**
     * {@link #playSound(World, String, double, double, double, float, float)}.
     *
     * @param world World to play sound in
     * @param name  Name of the sound to play
     * @param x     X-coordinate
     * @param y     Y-coordinate
     * @param z     Z-coordinate
     */
    public static void playSound(World world, String name, int x, int y, int z)
    {
        playSound(world, name,
          x + HALF_BLOCK_OFFSET, y + HALF_BLOCK_OFFSET, z + HALF_BLOCK_OFFSET,
          1.0F, (float) (world.rand.nextDouble() * RANDOM_PITCH_VALUE + STATIC_PITCH_VALUE));
    }

    /**
     * Plays a sound effect at a specific location.
     * Pitch and volume will be applied.
     *
     * @param world  World to play sound in
     * @param name   Name of the sound
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param volume Volume to play sound
     * @param pitch  Pitch to play sound
     */
    public static void playSound(World world, String name, double x, double y, double z, float volume, float pitch)
    {
        world.playSoundEffect(x, y, z, Constants.MOD_ID + ":" + name, volume, pitch);
    }

    /**
     * Plays sound near an entity.
     *
     * @param entity Entity to play sound at
     * @param name   Name of the sound to play
     * @param volume Volume to play sound
     * @param pitch  Pitch to play sound
     */
    public static void playSoundAtEntity(Entity entity, String name, float volume, float pitch)
    {
        entity.worldObj.playSoundAtEntity(entity, Constants.MOD_ID + ":" + name, volume, pitch);
    }
}
