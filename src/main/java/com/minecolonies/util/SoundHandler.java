package com.minecolonies.util;

import com.minecolonies.lib.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class SoundHandler
{
    public static void playSound(World world, String name, int x, int y, int z)
    {
        playSound(world, name, x + 0.5D, y + 0.5D, z + 0.5D, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }

    public static void playSound(World world, String name, double x, double y, double z, float volume, float pitch)
    {
        world.playSoundEffect(x, y, z, Constants.MOD_ID + ":" + name, volume, pitch);
    }

    public static void playSoundAtEntity(Entity entity, String name, float volume, float pitch)
    {
        entity.worldObj.playSoundAtEntity(entity, Constants.MOD_ID + ":" + name, volume, pitch);
    }
}
