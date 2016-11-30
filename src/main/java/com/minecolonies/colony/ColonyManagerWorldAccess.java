package com.minecolonies.colony;

import com.minecolonies.entity.EntityCitizen;
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
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags)
    {
        //Not needed

    }

    @Override
    public void notifyLightSet(BlockPos pos)
    {
        //Not needed

    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        //Not needed

    }

    @Override
    public void playSoundToAllNearExcept(
                                          EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
                                          double y, double z, float volume, float pitch)
    {
        //Not needed

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos)
    {
        //Not needed

    }

    @Override
    public void spawnParticle(
                               int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
                               double xSpeed, double ySpeed, double zSpeed, int... parameters)
    {
        //Not needed
    }

    @Override
    public void onEntityAdded(Entity entity)
    {
        if (entity instanceof EntityCitizen)
        {
            ((EntityCitizen) entity).updateColonyServer();
        }
    }

    @Override
    public void onEntityRemoved(Entity entity)
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
    public void broadcastSound(int soundID, BlockPos pos, int data)
    {
        //Not needed

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data)
    {
        //Not needed

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        //Not needed

    }
}
