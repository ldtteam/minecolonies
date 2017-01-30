package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

/**
 * Allows us to respond to entity addition and removal events.
 */
public class ColonyManagerWorldAccess implements IWorldEventListener
{

    @Override
    public void notifyBlockUpdate(final World worldIn, final BlockPos pos, final IBlockState oldState, final IBlockState newState, final int flags)
    {
        //Not needed

    }

    @Override
    public void notifyLightSet(final BlockPos pos)
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
                                          final EntityPlayer player, final SoundEvent soundIn, final SoundCategory category, final double x,
                                          final double y, final double z, final float volume, final float pitch)
    {
        //Not needed

    }

    @Override
    public void playRecord(final SoundEvent soundIn, final BlockPos pos)
    {
        //Not needed

    }

    @Override
    public void spawnParticle(
                               final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord,
                               final double xSpeed, final double ySpeed, final double zSpeed, final int... parameters)
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
                               final int... parameters)
    {
        //Not needed
    }

    @Override
    public void onEntityAdded(final Entity entity)
    {
        if (entity instanceof EntityCitizen)
        {
            ((EntityCitizen) entity).updateColonyServer();
        }
    }

    @Override
    public void onEntityRemoved(final Entity entity)
    {
        if (entity instanceof EntityCitizen)
        {
            final CitizenData citizen = ((EntityCitizen) entity).getCitizenData();
            if (citizen != null)
            {
                citizen.setCitizenEntity(null);
            }
        }
    }

    @Override
    public void broadcastSound(final int soundID, final BlockPos pos, final int data)
    {
        //Not needed

    }

    @Override
    public void playEvent(final EntityPlayer player, final int type, final BlockPos blockPosIn, final int data)
    {
        //Not needed

    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {
        //Not needed

    }
}
