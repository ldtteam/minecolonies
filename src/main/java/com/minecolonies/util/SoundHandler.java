package com.minecolonies.util;

import net.minecraft.entity.Entity;

import com.minecolonies.lib.Constants;
import net.minecraft.world.World;

/**
 * Created by Colton on 5/10/2014.
 */
public class SoundHandler
{
    public static void playSound(World world, String name, int x, int y, int z)
    {
        playSound(world, name, x + 0.5D, y + 0.5D, z + 0.5D, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }

    public static void playSound(World world, String name, double x, double y, double z, float volume, float pitch)
    {
        world.playSoundEffect(x, y, z, Constants.MODID + ":" + name, volume, pitch);
    }

    public static void playSoundAtEntity(Entity entity, String name, float volume, float pitch)
    {
        entity.worldObj.playSoundAtEntity(entity, Constants.MODID + ":" + name, volume, pitch);
    }
}
