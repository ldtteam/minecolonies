package com.minecolonies.core.entity.pathfinding.navigation;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveAwayFromLocation;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveCloseToXNearY;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveToLocation;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobRandomPos;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;

public class EntityNavigationUtils
{
    /**
     * Distance to consider being near a building block as reached
     */
    public static int BUILDING_REACH_DIST = 4;

    /**
     * Distance to consider being near a block inside a building as reached
     */
    public static int WOKR_IN_BUILDING_DIST = 7;

    /**
     * Distance which counts as reached
     */
    public static double REACHED_DIST = 1.5;

    /**
     * Tries to walk close to a given pos, staying near another position.
     *
     * @param entity
     * @param desiredPosition
     * @param nearbyPosition
     * @param distToDesired
     * @return True when arrived
     */
    public static boolean walkCloseToXNearY(
        final AbstractFastMinecoloniesEntity entity, final BlockPos desiredPosition,
        final BlockPos nearbyPosition,
        final int distToDesired, final boolean safeDestination)
    {
        return walkCloseToXNearY(entity, desiredPosition, nearbyPosition, distToDesired, safeDestination, 1.0);
    }

    /**
     * Tries to walk close to a given pos, staying near another position.
     *
     * @return True when arrived
     */
    public static boolean walkCloseToXNearY(
        final AbstractFastMinecoloniesEntity entity, final BlockPos desiredPosition,
        final BlockPos nearbyPosition,
        final int distToDesired, final boolean safeDestination, final double speedFactor)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());

        // Three cases
        // 1. Navigation Finished
        // 2. Navigation is progressing towards a previous task
        // 3. Navigation did not try once
        boolean isOnRightTask = (nav.getPathResult() != null
            && PathJobMoveCloseToXNearY.isJobFor(nav.getPathResult().getJob(), desiredPosition, nearbyPosition, distToDesired));

        if (nav.isDone() || !isOnRightTask)
        {
            if (isOnRightTask)
            {
                // Check distance once navigation is done, to let the entity walk
                if (BlockPosUtil.dist(entity.blockPosition(), desiredPosition) <= distToDesired)
                {
                    nav.stop();
                    return true;
                }
            }
            else if (BlockPosUtil.dist(entity.blockPosition(), desiredPosition) <= REACHED_DIST)
            {
                nav.stop();
                return true;
            }

            nav.walkCloseToXNearY(desiredPosition, nearbyPosition, 1, speedFactor, safeDestination);
        }

        return false;
    }

    /**
     * Walks to a position within a building
     *
     * @return True when arrived
     */
    public static boolean walkToPosInBuilding(
        final AbstractFastMinecoloniesEntity entity, final BlockPos destination, final IBuilding building, final int reachDistance)
    {
        if (building == null)
        {
            return walkToPos(entity, destination, reachDistance, true);
        }

        Tuple<BlockPos, BlockPos> corners = building.getCorners();
        final BlockPos center =
            new BlockPos((corners.getA().getX() + corners.getB().getX()) / 2, building.getPosition().getY(), (corners.getA().getZ() + corners.getB().getZ()) / 2);

        return walkCloseToXNearY(entity, destination, center, reachDistance, true);
    }

    /**
     * Walks to a position within a building
     *
     * @return True when arrived
     */
    public static boolean walkToBuilding(
        final AbstractFastMinecoloniesEntity entity, final IBuilding building)
    {
        if (building == null)
        {
            return true;
        }

        return walkToPosInBuilding(entity, building.getPosition(), building, BUILDING_REACH_DIST);
    }

    /**
     * Walks to a given position
     *
     * @return True when arrived
     */
    public static boolean walkToPos(
        final AbstractFastMinecoloniesEntity entity, final BlockPos desiredPosition, final boolean safeDestination)
    {
        return walkToPos(entity, desiredPosition, BUILDING_REACH_DIST, safeDestination, 1.0);
    }

    /**
     * Walks to a given position
     *
     * @return True when arrived
     */
    public static boolean walkToPos(
        final AbstractFastMinecoloniesEntity entity, final BlockPos desiredPosition,
        final int distToDesired, final boolean safeDestination)
    {
        return walkToPos(entity, desiredPosition, distToDesired, safeDestination, 1.0);
    }

    /**
     * Walks to a given position
     *
     * @return True when arrived
     */
    public static boolean walkToPos(
        final AbstractFastMinecoloniesEntity entity, final BlockPos desiredPosition,
        final int distToDesired, final boolean safeDestination, final double speedFactor)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());

        boolean isOnRightTask = (nav.getPathResult() != null
            && PathJobMoveToLocation.isJobFor(nav.getPathResult().getJob(), desiredPosition));

        if (nav.isDone() || !isOnRightTask)
        {
            if (isOnRightTask)
            {
                // Check distance once navigation is done, to let the entity walk
                if (BlockPosUtil.dist(entity.blockPosition(), desiredPosition) <= distToDesired)
                {
                    nav.stop();
                    return true;
                }
            }
            else if (BlockPosUtil.dist(entity.blockPosition(), desiredPosition) <= REACHED_DIST)
            {
                nav.stop();
                return true;
            }

            nav.walkTo(desiredPosition, speedFactor, safeDestination);
        }

        return false;
    }

    /**
     * Walks away from a given position
     *
     * @return True when arrived
     */
    public static boolean walkAwayFrom(final AbstractFastMinecoloniesEntity entity, final BlockPos avoid, final int distance, final double speed)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());
        boolean isOnRightTask = (nav.getPathResult() != null && PathJobMoveAwayFromLocation.isJobFor(nav.getPathResult().getJob(), distance, avoid));

        if (nav.isDone() || !isOnRightTask)
        {
            if (isOnRightTask)
            {
                // Check distance once navigation is done, to let the entity walk
                if (BlockPosUtil.dist(entity.blockPosition(), avoid) >= distance)
                {
                    nav.stop();
                    return true;
                }
            }
            else if (BlockPosUtil.dist(entity.blockPosition(), avoid) >= REACHED_DIST)
            {
                nav.stop();
                return true;
            }

            nav.walkAwayFrom(avoid, distance, speed, false);
        }

        return false;
    }

    /**
     * Walks to a random position a given distance away
     *
     * @return True when arrived
     */
    public static boolean walkToRandomPos(final AbstractFastMinecoloniesEntity entity, final int range, final double speedFactor)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());
        boolean isOnRightTask = (nav.getPathResult() != null && nav.getPathResult().getJob() instanceof PathJobRandomPos);

        if (nav.isDone() || !isOnRightTask)
        {
            if (isOnRightTask)
            {
                nav.stop();
                return true;
            }

            nav.walkToRandomPos(range, speedFactor);
        }

        return false;
    }

    /**
     * Walks to a random position a given distance away within the provided box
     *
     * @return True when arrived
     */
    public static boolean walkToRandomPosWithin(final AbstractFastMinecoloniesEntity entity, final int range, final double speedFactor, final Tuple<BlockPos, BlockPos> corners)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());
        boolean isOnRightTask = (nav.getPathResult() != null && nav.getPathResult().getJob() instanceof PathJobRandomPos);

        if (nav.isDone() || !isOnRightTask)
        {
            if (isOnRightTask)
            {
                nav.stop();
                return true;
            }

            nav.walkToRandomPos(range, speedFactor, corners);
        }

        return false;
    }

    /**
     * Walks to a random position a given distance away around the provided center
     *
     * @return True when arrived
     */
    public static boolean walkToRandomPosAround(final AbstractFastMinecoloniesEntity entity, final BlockPos center, final int range, final double speedFactor)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());
        boolean isOnRightTask = (nav.getPathResult() != null && PathJobRandomPos.isJobFor(nav.getPathResult().getJob(), center, range));

        if (nav.isDone() || !isOnRightTask)
        {
            if (isOnRightTask)
            {
                nav.stop();
                return true;
            }

            nav.walkToRandomPosAround(range, speedFactor, center);
        }

        return false;
    }
}
