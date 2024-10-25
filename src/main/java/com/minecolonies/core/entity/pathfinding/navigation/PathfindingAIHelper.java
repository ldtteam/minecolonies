package com.minecolonies.core.entity.pathfinding.navigation;

import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveCloseToXNearY;
import net.minecraft.core.BlockPos;

public class PathfindingAIHelper
{
    /**
     * Tries to walk close to a given pos, staying near another position.
     *
     * @param entity
     * @param desiredPosition
     * @param nearbyPosition
     * @param distToDesired
     * @return True while walking, false when reached
     */
    public static boolean walkCloseToXNearY(
      final AbstractFastMinecoloniesEntity entity, final BlockPos desiredPosition,
      final BlockPos nearbyPosition,
      final int distToDesired)
    {
        final MinecoloniesAdvancedPathNavigate nav = ((MinecoloniesAdvancedPathNavigate) entity.getNavigation());

        if (nav.isDone() || (nav.getPathResult() != null
                               && !(nav.getPathResult().getJob() instanceof PathJobMoveCloseToXNearY job
                                      && job.nearbyPosition.equals(nearbyPosition)
                                      && job.desiredPosition.equals(desiredPosition)
                                      && job.distToDesired == distToDesired)))
        {
            // Check distance once navigation is done, to let the entity walk
            if (BlockPosUtil.dist(entity.blockPosition(), desiredPosition) < distToDesired)
            {
                return false;
            }

            PathJobMoveCloseToXNearY pathJob = new PathJobMoveCloseToXNearY(entity.level, desiredPosition, nearbyPosition, distToDesired, entity);
            nav.setPathJob(pathJob, desiredPosition, 1.0, false);
        }

        return true;
    }
}
