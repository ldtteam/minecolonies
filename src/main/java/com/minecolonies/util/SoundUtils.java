package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

import java.util.Random;

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



    public static void getRandomSound(World worldIn, EntityCitizen citizen)
    {
        worldIn.playSound(null, "examplemod:song1", 1.0F, 1.0F);

        //    public static PlaySoundAtEntityEvent onPlaySoundAtEntity(Entity entity, SoundEvent name, SoundCategory category, float volume, float pitch) {

        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(citizen, "name", SoundCategory.NEUTRAL, 0.2F, (float)STATIC_PITCH_VALUE);
        if(!event.isCanceled() && event.getSound() != null) {
            p_playSound_8_ = event.getSound();
            p_playSound_9_ = event.getCategory();
            p_playSound_10_ = event.getVolume();
            p_playSound_11_ = event.getPitch();

            for(int i = 0; i < this.eventListeners.size(); ++i) {
                ((IWorldEventListener)this.eventListeners.get(i)).playSoundToAllNearExcept(p_playSound_1_, p_playSound_8_, p_playSound_9_, p_playSound_2_, p_playSound_4_, p_playSound_6_, p_playSound_10_, p_playSound_11_);
            }

        }

        this.worldObj.playSound(
                (EntityPlayer) null,
                this.getPosition(),
                SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.AMBIENT,
                0.2F,
                (float) ((this.rand.nextGaussian() * 0.7D + 1.0D) * 2.0D));
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
    /*public static void playSound(@NotNull World world, String name, int x, int y, int z)
    {
        world.playSound(world, name,
          x + HALF_BLOCK_OFFSET, y + HALF_BLOCK_OFFSET, z + HALF_BLOCK_OFFSET,
          1.0F, (float) (world.rand.nextDouble() * RANDOM_PITCH_VALUE + STATIC_PITCH_VALUE));
    }*/

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
    /*public static void playSound(@NotNull World world, String name, double x, double y, double z, float volume, float pitch)
    {
        world.playSoundEffect(x, y, z, Constants.MOD_ID + ":" + name, volume, pitch);
    }*/

    /**
     * Plays sound near an entity.
     *
     * @param entity Entity to play sound at
     * @param name   Name of the sound to play
     * @param volume Volume to play sound
     * @param pitch  Pitch to play sound
     */
    /*public static void playSoundAtEntity(@NotNull Entity entity, String name, float volume, float pitch)
    {
        entity.worldObj.playSoundAtEntity(entity, Constants.MOD_ID + ":" + name, volume, pitch);
    }*/
}
