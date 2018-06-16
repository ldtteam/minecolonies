package com.minecolonies.coremod.util;

import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.PathResult;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.MOVE_MINIMAL;
import static com.minecolonies.api.util.constant.CitizenConstants.ROTATION_MOVEMENT;

/**
 * Utility methods for BlockPos.
 */
public final class WorkerUtil
{
    /**
     * Default range for moving to something until we stop.
     */
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;

    private WorkerUtil()
    {
        //Hide default constructor.
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
        if (!EntityUtils.isLivingAtSiteWithMove(worker, x, y, z, range))
        {
            //If not moving the try setting the point where the entity should move to
            if (worker.getNavigator().noPath() && !EntityUtils.tryMoveLivingToXYZ(worker, x, y, z))
            {
                worker.getCitizenStatusHandler().setStatus(Status.PATHFINDING_ERROR);
            }
            return false;
        }
        return true;
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
     * Recalls the citizen, notifies player if not successful.
     *
     * @param spawnPoint the spawnPoint.
     * @param citizen    the citizen.
     * @return true if succesful.
     */
    public static boolean setSpawnPoint(@Nullable final BlockPos spawnPoint, @NotNull final EntityCitizen citizen)
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
        citizen.getNavigator().clearPath();
        return true;
    }

    /**
     * Get a Tooltype for a certain block.
     * We need this because minecraft has a lot of blocks which have strange or no required tool.
     *
     * @param target the target block.
     * @return the toolType to use.
     */
    public static IToolType getBestToolForBlock(final Block target)
    {
        final IToolType toolType = ToolType.getToolType(target.getHarvestTool(target.getDefaultState()));

        if (toolType == ToolType.NONE && target.getDefaultState().getMaterial() == Material.WOOD)
        {
            return ToolType.AXE;
        }
        else if (target == Blocks.HARDENED_CLAY || target == Blocks.STAINED_HARDENED_CLAY)
        {
            return ToolType.PICKAXE;
        }
        return toolType;
    }

    /**
     * Get the correct havestlevel for a certain block.
     * We need this because minecraft has a lot of blocks which have strange or no required harvestlevel.
     *
     * @param target the target block.
     * @return the required harvestLevel.
     */
    public static int getCorrectHavestLevelForBlock(final Block target)
    {
        final int required = target.getHarvestLevel(target.getDefaultState());

        if ((required == -1 && target.getDefaultState().getMaterial() == Material.WOOD)
              || target == Blocks.HARDENED_CLAY || target == Blocks.STAINED_HARDENED_CLAY)
        {
            return 0;
        }
        return required;
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
        return pathpoint != null && pathpoint.x == x && pathpoint.z == z;
    }

    /**
     * Change the citizens Rotation to look at said block.
     *
     * @param block the block he should look at.
     */
    public static void faceBlock(@Nullable final BlockPos block, final EntityCitizen citizen)
    {
        if (block == null)
        {
            return;
        }

        final double xDifference = block.getX() - citizen.posX;
        final double zDifference = block.getZ() - citizen.posZ;
        final double yDifference = block.getY() - (citizen.posY + (double) citizen.getEyeHeight());

        final double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        final double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        final double intendedRotationPitch = -(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI);
        citizen.setOwnRotation((float) EntityUtils.updateRotation(citizen.rotationYaw, intendedRotationYaw, ROTATION_MOVEMENT),
          (float) EntityUtils.updateRotation(citizen.rotationPitch, intendedRotationPitch, ROTATION_MOVEMENT));

        final double goToX = xDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        //Have to move the entity minimally into the direction to render his new rotation.
        citizen.move(MoverType.SELF, (float) goToX, 0, (float) goToZ);
    }
}
