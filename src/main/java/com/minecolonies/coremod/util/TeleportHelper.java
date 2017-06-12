package com.minecolonies.coremod.util;

import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.coremod.entity.EntityCitizen;
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

    public static boolean teleportCitizen(EntityCitizen citizen, World world, BlockPos location)
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

        citizen.dismountRidingEntity();

        citizen.setLocationAndAngles(
                spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
                spawnPoint.getY(),
                spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
                citizen.rotationYaw,
                citizen.rotationPitch);
        citizen.getNavigator().clearPathEntity();

        return true;
    }
}
