package com.minecolonies.entity.pathfinding;

import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.citizen.miner.Level;
import com.minecolonies.util.EntityUtils;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * The min distance a worker has to have to a proxy.
     */
    private static final int MIN_DISTANCE = 36;

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
    public boolean walkToBlock(@NotNull BlockPos target, int range)
    {
        return walkToBlock(target, range, false);
    }

    /**
     * Leads the worker to a certain position due to proxies.
     * @param target the position.
     * @param range the range.
     * @param onMove worker on move or not?
     * @return true if arrived.
     */
    public boolean walkToBlock(@NotNull BlockPos target, int range, boolean onMove)
    {
        double distanceToPath = worker.getPosition().distanceSq(target.getX(), target.getY(), target.getZ());

        if(distanceToPath <= MIN_RANGE_FOR_DIRECT_PATH)
        {
            if(onMove)
            {
                return EntityUtils.isWorkerAtSiteWithMove(worker, target.getX(), target.getY(), target.getZ(), range)
                        || EntityUtils.isWorkerAtSite(worker, target.getX(), target.getY(), target.getZ(), range + 1);
            }
            else
            {
                return !EntityUtils.isWorkerAtSite(worker, target.getX(), target.getY(), target.getZ(), range);
            }
        }


        BlockPos proxyPoint;

        if(worker.getColonyJob() != null && worker.getColonyJob() instanceof JobMiner)
        {
            proxyPoint = getMinerProxy(target, distanceToPath);
        }
        else
        {
            proxyPoint = getProxy(target, distanceToPath);
        }

        if(proxyPoint != null)
        {
            if (!EntityUtils.isWorkerAtSiteWithMove(worker, proxyPoint.getX(), proxyPoint.getY(), proxyPoint.getZ(), range))
            {
                //only walk to the block
                return true;
            }
        }
        else
        {
            if (!EntityUtils.isWorkerAtSiteWithMove(worker, target.getX(), target.getY(), target.getZ(), range))
            {
                //only walk to the block
                return false;
            }
        }

        return false;
    }

    /**
     * Returns a proxy point to the goal for the miner especially.
     * @param target the target.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    private BlockPos getMinerProxy(final BlockPos target, final double distanceToPath)
    {
        AbstractBuildingWorker building =  worker.getWorkBuilding();
        if(building == null || !(building instanceof BuildingMiner))
        {
            return getProxy(target, distanceToPath);
        }

        ((BuildingMiner) building).getLadderLocation();

        Level level = ((BuildingMiner) building).getCurrentLevel();
        BlockPos ladderPos = ((BuildingMiner) building).getLadderLocation();

        //If his current working level is null, we have nothing to worry about.
        if(level != null)
        {
            int levelDepth = level.getDepth() + 3;
            int targetY = target.getY();
            int workerY = worker.getPosition().getY();

            //Check if miner is underground in shaft and his target is overground.
            if (workerY <= levelDepth && targetY > levelDepth)
            {
                //If he already is at ladder location, the closest node automatically will be his hut block.
                return new BlockPos(ladderPos.getX(), level.getDepth(), ladderPos.getZ());
            }
            //Check if target is underground in shaft and miner is over it.
            else if (targetY <= levelDepth && workerY > levelDepth)
            {
                BlockPos buildingPos = building.getLocation();
                double newDistance = worker.getPosition().distanceSq(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ());
                double distanceToLadder = worker.getPosition().distanceSq(ladderPos.getX(), level.getDepth(), ladderPos.getZ());
                BlockPos newProxy;
                if (newDistance > MIN_DISTANCE && distanceToLadder > newDistance)
                {
                    newProxy = getProxy(buildingPos, newDistance);
                }
                else
                {
                    newProxy = new BlockPos(ladderPos.getX(), level.getDepth(), ladderPos.getZ());
                }

                return newProxy;
            }
            //If he is on the same Y level as his target and both underground, don't use a proxy. Just don't.
            else if(targetY <= levelDepth)
            {
                return target;
            }
        }

        return getProxy(target, distanceToPath);
    }

    /**
     * Returns a proxy point to the goal.
     * @param target the target.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    @Nullable
    private BlockPos getProxy(@NotNull BlockPos target, double distanceToPath)
    {
        if(worker.getColony() == null)
        {
            return null;
        }

        double shortestDistance = Double.MAX_VALUE;
        BlockPos proxyPoint = null;
        for(BlockPos building: worker.getColony().getBuildings().keySet())
        {
            double distanceToProxyPoint = worker.getPosition().distanceSq(building.getX(), worker.getPosition().getY(), building.getZ());
            if(distanceToProxyPoint < shortestDistance
                    && building.distanceSq(target.getX(), target.getY(), target.getZ()) < distanceToPath
                    && distanceToProxyPoint > MIN_DISTANCE)
            {
                proxyPoint = building;
                shortestDistance = distanceToProxyPoint;
            }
        }

        return proxyPoint;
    }
}
