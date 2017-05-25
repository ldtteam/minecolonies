package com.minecolonies.coremod.util;

import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.PathResult;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for BlockPos.
 */
public final class WorkerUtil
{
    /**
     * Default range for moving to something until we stop.
     */
    private static final int    DEFAULT_MOVE_RANGE       = 3;
    private static final int    TELEPORT_RANGE      = 512;
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;
    private static final int    SCAN_RADIUS         = 5;

    private WorkerUtil()
    {
        //Hide default constructor.
    }

    /**
     * {@link WorkerUtil#isWorkerAtSiteWithMove(EntityCitizen, int, int, int)}.
     *
     * @param worker Worker to check.
     * @param site   Chunk coordinates of site to check.
     * @return True when worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, @NotNull final BlockPos site)
    {
        return isWorkerAtSiteWithMove(worker, site.getX(), site.getY(), site.getZ());
    }

    /**
     * {@link WorkerUtil#isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}.
     *
     * @param worker Worker to check.
     * @param site   Chunk coordinates of site to check.
     * @param range  Range to check in.
     * @return True when within range, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, @NotNull final BlockPos site, final int range)
    {
        return isWorkerAtSiteWithMove(worker, site.getX(), site.getY(), site.getZ(), range);
    }

    /**
     * Attempt to move to XYZ.
     * True when found and destination is set.
     *
     * @param citizen     Citizen to move to XYZ.
     * @param destination Chunk coordinate of the distance.
     * @return True when found, and destination is set, otherwise false.
     */
    public static PathResult moveLivingToXYZ(@NotNull final EntityCitizen citizen, @NotNull final BlockPos destination)
    {
        return citizen.getNavigator().moveToXYZ(destination.getX(), destination.getY(), destination.getZ(), 1.0);
    }

    /**
     * {@link #isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @return True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, final int x, final int y, final int z)
    {
        //Default range of 3 works better
        //Range of 2 get some workers stuck
        return isWorkerAtSiteWithMove(worker, x, y, z, DEFAULT_MOVE_RANGE);
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, final int x, final int y, final int z, final int range)
    {
        if (!isWorkerAtSite(worker, x, y, z, TELEPORT_RANGE))
        {
            final BlockPos spawnPoint =
              Utils.scanForBlockNearPoint(worker.getEntityWorld(),
                new BlockPos(x, y, z),
                SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS, 2,
                Blocks.AIR,
                Blocks.SNOW_LAYER,
                Blocks.TALLGRASS,
                Blocks.RED_FLOWER,
                Blocks.YELLOW_FLOWER,
                Blocks.CARPET);

            worker.setLocationAndAngles(
              spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
              spawnPoint.getY(),
              spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
              worker.rotationYaw,
              worker.rotationPitch);
            return true;
        }
        //If too far away
        if (!isWorkerAtSite(worker, x, y, z, range))
        {
            //If not moving the try setting the point where the entity should move to
            if (worker.getNavigator().noPath() && !EntityUtils.tryMoveLivingToXYZ(worker, x, y, z))
            {
                worker.setStatus(EntityCitizen.Status.PATHFINDING_ERROR);
            }
            return false;
        }
        return true;
    }

    /**
     * Returns whether or not the worker is within a specific range of his
     * working site.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSite(@NotNull final EntityCitizen worker, final int x, final int y, final int z, final int range)
    {
        return worker.getPosition().distanceSq(new Vec3i(x, y, z)) < MathUtils.square(range);
    }

    /**
     * Recalls the citizen, notifies player if not successful.
     *
     * @param spawnPoint the spawnPoint.
     * @param citizen    the citizen.
     * @return true if succesful.
     */
    public static boolean setSpawnPoint(@Nullable BlockPos spawnPoint, @NotNull EntityCitizen citizen)
    {
        if (spawnPoint == null)
        {
            return false;
        }

        citizen.setLocationAndAngles(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.rotationYaw,
          citizen.rotationPitch);
        citizen.getNavigator().clearPathEntity();
        return true;
    }

    /**
     * Returns whether or not a citizen is heading to a specific location.
     *
     * @param citizen Citizen you want to check
     * @param x       X-coordinate
     * @param z       Z-coordinate
     * @return True if citizen heads to (x, z), otherwise false
     */
    public static boolean isPathingTo(@NotNull final EntityCitizen citizen, final int x, final int z)
    {
        final PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == x && pathpoint.zCoord == z;
    }
}
