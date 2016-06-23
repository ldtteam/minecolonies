package com.minecolonies.colony;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IWorldAccess;

/**
 * Allows us to respond to entity addition and removal events.
 */
public class ColonyManagerWorldAccess implements IWorldAccess
{
    @Override
    public void markBlockForUpdate(BlockPos pos)
    {
        // We don't care about this.
    }

    @Override
    public void notifyLightSet(BlockPos pos)
    {
        // We don't care about this.
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        // We don't care about this.
    }

    @Override
    public void playSound(String soundName, double x, double y, double z, float volume, float pitch)
    {
        // We don't care about this.
    }

    @Override
    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch)
    {
        // We don't care about this.
    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        // We don't care about this.
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
            CitizenData citizen = ((EntityCitizen) entity).getCitizenData();
            if (citizen != null)
            {
                citizen.setCitizenEntity(null);
            }
        }
    }

    @Override
    public void playRecord(String recordName, BlockPos blockPosIn)
    {
        // We don't care about this.
    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data)
    {
        // We don't care about this.
    }

    @Override
    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data)
    {
        // We don't care about this.
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        // We don't care about this.
    }
}
