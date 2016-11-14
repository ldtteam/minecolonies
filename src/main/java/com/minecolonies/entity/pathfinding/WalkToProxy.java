package com.minecolonies.entity.pathfinding;

import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.EntityUtils;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Proxy handling walkToX tasks.
 */
public class WalkToProxy
{
    /**
     * The distance the worker can path directly without the proxy.
     */
    private static final int MIN_RANGE_FOR_DIRECT_PATH = 400;

    /**
     * The worker entity associated with the proxy.
     */
    private final EntityCitizen worker;

    /**
     * Creates a walkToProxy for a certain worker.
     * @param worker the worker.
     */
    public WalkToProxy(EntityCitizen worker)
    {
        this.worker = worker;
    }

    /**
     * Leads the worker to a certain position due to proxies.
     * @param target the position.
     * @param range the range.
     * @return true if arrived.
     */
    public boolean walkToBlock(BlockPos target, int range)
    {
        double distanceToPath = worker.getPosition().distanceSq(target.getX(), target.getY(), target.getZ());

        if(distanceToPath <= MIN_RANGE_FOR_DIRECT_PATH)
        {
            return !EntityUtils.isWorkerAtSite(worker, target.getX(), target.getY(), target.getZ(), range);
        }

        double shortestDistance = Double.MAX_VALUE;
        BlockPos proxyPoint = null;

        for(BlockPos building: worker.getColony().getBuildings().keySet())
        {
            double distanceToProxyPoint = worker.getPosition().distanceSq(building.getX(), building.getY(), building.getZ());
            if(distanceToProxyPoint < shortestDistance
                    && building.distanceSq(target.getX(), target.getY(), target.getZ()) < distanceToPath
                    && distanceToProxyPoint > 25)
            {
                proxyPoint = building;
                shortestDistance = distanceToProxyPoint;
            }
        }

        if(proxyPoint != null)
        {
            if (!EntityUtils.isWorkerAtSiteWithMove(worker, proxyPoint.getX(), proxyPoint.getY(), proxyPoint.getZ(), range))
            {
                //only walk to the block
                return false;
            }
        }
        else
        {
            if (!EntityUtils.isWorkerAtSiteWithMove(worker, target.getX(), target.getY(), target.getZ(), range))
            {
                //only walk to the block
                return true;
            }
        }

        return false;
    }

}
