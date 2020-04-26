package com.minecolonies.coremod.colony;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Allows us to respond to entity addition and removal events.
 */
public class ColonyManagerWorldAccess implements IWorldEventListener
{

    @Override
    public void notifyBlockUpdate(@NotNull final World worldIn, @NotNull final BlockPos pos, @NotNull final IBlockState oldState, @NotNull final IBlockState newState, final int flags)
    {
        //Not needed

    }

    @Override
    public void notifyLightSet(@NotNull final BlockPos pos)
    {
        //Not needed

    }

    @Override
    public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        //Not needed

    }

    @Override
    public void playSoundToAllNearExcept(
                                          final EntityPlayer player, @NotNull final SoundEvent soundIn, @NotNull final SoundCategory category, final double x,
                                          final double y, final double z, final float volume, final float pitch)
    {
        //Not needed

    }

    @Override
    public void playRecord(@NotNull final SoundEvent soundIn, @NotNull final BlockPos pos)
    {
        //Not needed

    }

    @Override
    public void spawnParticle(
                               final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord,
                               final double xSpeed, final double ySpeed, final double zSpeed, @NotNull final int... parameters)
    {
        //Not needed
    }

    @Override
    public void spawnParticle(
                               final int particleID,
                               final boolean ignoreRange,
                               final boolean noDecription,
                               final double xCoord,
                               final double yCoord,
                               final double zCoord,
                               final double xSpeed,
                               final double ySpeed,
                               final double zSpeed,
                               @NotNull final int... parameters)
    {
        //Not needed
    }

    @Override
    public void onEntityAdded(@NotNull final Entity entity)
    {
    }

    @Override
    public void onEntityRemoved(@NotNull final Entity entity)
    {
        if (entity instanceof EntityCitizen)
        {
            entity.setDead();
            return;
        }
        // Removing from world does not kill the entity itself
        if (entity instanceof AbstractEntityMinecoloniesMob)
        {
            entity.setDead();
        }
    }

    @Override
    public void broadcastSound(final int soundID, @NotNull final BlockPos pos, final int data)
    {
        //Not needed

    }

    @Override
    public void playEvent(final EntityPlayer player, final int type, @NotNull final BlockPos blockPosIn, final int data)
    {
        //Not needed

    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, @NotNull final BlockPos pos, final int progress)
    {
        //Not needed

    }
}
