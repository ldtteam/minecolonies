package com.minecolonies.coremod.util;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Helper class for teleporting citizens.
 */
public final class TeleportHelper
{
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;

    /**
     * Private constructor to hide the implicit public one.
     */
    private TeleportHelper()
    {
        // Intentionally left empty.
    }

    public static boolean teleportCitizen(final AbstractEntityCitizen citizen, final World world, final BlockPos location)
    {
        if (citizen == null || world == null)
        {
            return false;
        }

        final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, location);
        if (spawnPoint == null)
        {
            return false;
        }

        if(citizen.getCitizenSleepHandler().isAsleep())
        {
            citizen.getCitizenSleepHandler().onWakeUp();
        }

        citizen.stopRiding();
        citizen.setLocationAndAngles(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        if(citizen.getProxy() != null)
        {
            citizen.getProxy().reset();
        }
        citizen.getNavigator().clearPath();
        if(citizen.getProxy() != null)
        {
            citizen.getProxy().reset();
        }

        return true;
    }
}
