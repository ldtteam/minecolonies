package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.citizen.miner.Node;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.EntityUtils;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
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
     * The min distance a worker has to have to a proxy.
     */
    private static final int MIN_DISTANCE = 25;

    /**
     * The worker entity associated with the proxy.
     */
    private final EntityCitizen worker;

    /**
     * The current proxy the citizen paths to.
     */
    private BlockPos currentProxy;

    /**
     * List of proxies the worker has to follow.
     */
    private ArrayList<BlockPos> proxyList = new ArrayList<>();

    /**
     * Current target the worker has.
     */
    private BlockPos target;

    /**
     * Creates a walkToProxy for a certain worker.
     *
     * @param worker the worker.
     */
    public WalkToProxy(EntityCitizen worker)
    {
        this.worker = worker;
    }

    /**
     * Leads the worker to a certain position due to proxies.
     *
     * @param target the position.
     * @param range  the range.
     * @return true if arrived.
     */
    public boolean walkToBlock(@NotNull BlockPos target, int range)
    {
        return walkToBlock(target, range, false);
    }

    /**
     * Take the direct path to a certain location.
     *
     * @param target the target position.
     * @param range  the range.
     * @param onMove worker on move or not?
     * @return true if arrived.
     */
    private boolean takeTheDirectPath(@NotNull BlockPos target, int range, boolean onMove)
    {
        if (onMove)
        {
            final int targetY = worker.getColonyJob() instanceof JobBuilder ? worker.getPosition().getY() : target.getY();
            return EntityUtils.isWorkerAtSiteWithMove(worker, target.getX(), target.getY(), target.getZ(), range)
                     || EntityUtils.isWorkerAtSite(worker, target.getX(), targetY, target.getZ(), range + 1);
        }
        else
        {
            return !EntityUtils.isWorkerAtSite(worker, target.getX(), target.getY(), target.getZ(), range);
        }
    }

    /**
     * Leads the worker to a certain position due to proxies.
     *
     * @param target the target position.
     * @param range  the range.
     * @param onMove worker on move or not?
     * @return true if arrived.
     */
    public boolean walkToBlock(@NotNull BlockPos target, int range, boolean onMove)
    {
        if(!target.equals(this.target))
        {
            reset();
            this.target = target;
        }

        final double distanceToPath = worker.getColonyJob() instanceof JobBuilder
                ? BlockPosUtil.getDistanceSquared2D(worker.getPosition(), target) : BlockPosUtil.getDistanceSquared(worker.getPosition(), target);

        if (distanceToPath <= MIN_RANGE_FOR_DIRECT_PATH)
        {
            if (distanceToPath <= MIN_DISTANCE)
            {
                currentProxy = null;
            }
            else
            {
                currentProxy = target;
            }

            proxyList = new ArrayList<>();
            return takeTheDirectPath(target, range, onMove);
        }

        if (currentProxy == null)
        {
            currentProxy = fillProxyList(target, distanceToPath);
        }

        final double distanceToProxy = BlockPosUtil.getDistanceSquared2D(worker.getPosition(), currentProxy);
        final double distanceToNextProxy = proxyList.isEmpty() ? BlockPosUtil.getDistanceSquared2D(worker.getPosition(), target)
                                             : BlockPosUtil.getDistanceSquared2D(worker.getPosition(), proxyList.get(0));
        final double distanceProxyNextProxy = proxyList.isEmpty() ? BlockPosUtil.getDistanceSquared2D(currentProxy, target)
                                                : BlockPosUtil.getDistanceSquared2D(currentProxy, proxyList.get(0));
        if (distanceToProxy < MIN_DISTANCE || distanceToNextProxy < distanceProxyNextProxy)
        {
            if (proxyList.isEmpty())
            {
                currentProxy = target;
            }

            if (proxyList.isEmpty())
            {
                return takeTheDirectPath(target, range, onMove);
            }

            worker.getNavigator().clearPathEntity();
            currentProxy = proxyList.get(0);
            proxyList.remove(0);
        }

        if (currentProxy != null && !EntityUtils.isWorkerAtSiteWithMove(worker, currentProxy.getX(), currentProxy.getY(), currentProxy.getZ(), range))
        {
            //only walk to the block
            return !onMove;
        }

        return !onMove;
    }

    /**
     * Calculates a list of proxies to a certain target for a worker.
     *
     * @param target         the target.
     * @param distanceToPath the complete distance.
     * @return the first position to path to.
     */
    @NotNull
    private BlockPos fillProxyList(@NotNull BlockPos target, double distanceToPath)
    {
        BlockPos proxyPoint;

        if (worker.getColonyJob() != null && worker.getColonyJob() instanceof JobMiner)
        {
            proxyPoint = getMinerProxy(target, distanceToPath);
        }
        else
        {
            proxyPoint = getProxy(target, worker.getPosition(), distanceToPath);
        }

        if (!proxyList.isEmpty())
        {
            proxyList.remove(0);
        }

        return proxyPoint;
    }

    //todo reset on target change. Store target.
    /**
     * Reset the proxy.
     */
    public void reset()
    {
        currentProxy = null;
        proxyList = new ArrayList<>();
    }

    /**
     * Returns a proxy point to the goal for the miner especially.
     *
     * @param target         the target.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    @NotNull
    private BlockPos getMinerProxy(final BlockPos target, final double distanceToPath)
    {
        final AbstractBuildingWorker building = worker.getWorkBuilding();
        if (building == null || !(building instanceof BuildingMiner))
        {
            return getProxy(target, worker.getPosition(), distanceToPath);
        }

        ((BuildingMiner) building).getLadderLocation();

        final Level level = ((BuildingMiner) building).getCurrentLevel();
        final BlockPos ladderPos = ((BuildingMiner) building).getLadderLocation();

        //If his current working level is null, we have nothing to worry about.
        if (level != null)
        {
            final int levelDepth = level.getDepth() + 2;
            final int targetY = target.getY();
            final int workerY = worker.getPosition().getY();

            //Check if miner is underground in shaft and his target is overground.
            if (workerY <= levelDepth && targetY > levelDepth)
            {
                if(level.getRandomNode() != null && level.getRandomNode().getParent() != null)
                {
                    com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(level.getRandomNode().getParent());
                    while (new Point2D.Double(currentNode.getX(), currentNode.getZ()) != currentNode.getParent() && currentNode.getParent() != null)
                    {
                        proxyList.add(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }
                }

                proxyList.add(new BlockPos(ladderPos.getX(), level.getDepth(), ladderPos.getZ()));
                return getProxy(target, worker.getPosition(), distanceToPath);

                //If he already is at ladder location, the closest node automatically will be his hut block.
            }
            //Check if target is underground in shaft and miner is over it.
            else if (targetY <= levelDepth && workerY > levelDepth)
            {
                final BlockPos buildingPos = building.getLocation();
                BlockPos newProxy;

                //First calculate way to miner building.
                newProxy = getProxy(buildingPos, worker.getPosition(), BlockPosUtil.getDistanceSquared(worker.getPosition(), buildingPos));

                //Then add the ladder position as the latest node.
                proxyList.add(new BlockPos(ladderPos.getX(), level.getDepth(), ladderPos.getZ()));

                if(level.getRandomNode().getParent() != null)
                {
                    final List<BlockPos> nodesToTarget = new ArrayList<>();
                    com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(level.getRandomNode().getParent());
                    while (new Point2D.Double(currentNode.getX(), currentNode.getZ()) != currentNode.getParent() && currentNode.getParent() != null)
                    {
                        nodesToTarget.add(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }

                    for (int i = nodesToTarget.size() - 1; i >= 0; i--)
                    {
                        proxyList.add(nodesToTarget.get(i));
                    }
                }

                return newProxy;
            }
            //If he is on the same Y level as his target and both underground, don't use a proxy. Just don't.
            else if (targetY <= levelDepth)
            {
                double closestNode = Double.MAX_VALUE;
                Node lastNode = null;
                for(final Map.Entry<Point2D, Node> node : level.getNodes().entrySet())
                {
                    final double distanceToNode = node.getKey().distance(worker.getPosition().getX(), worker.getPosition().getZ());
                    if(distanceToNode < closestNode)
                    {
                        lastNode = node.getValue();
                        closestNode = distanceToNode;
                    }
                }

                if(lastNode != null && lastNode.getParent() != null)
                {
                    com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(lastNode.getParent());
                    while (new Point2D.Double(currentNode.getX(), currentNode.getZ()) != currentNode.getParent() && currentNode.getParent() != null)
                    {
                        proxyList.add(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }
                }

                if(level.getRandomNode().getParent() != null)
                {
                    final List<BlockPos> nodesToTarget = new ArrayList<>();
                    com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(level.getRandomNode().getParent());
                    while (new Point2D.Double(currentNode.getX(), currentNode.getZ()) != currentNode.getParent() && currentNode.getParent() != null)
                    {
                        nodesToTarget.add(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }

                    for (int i = nodesToTarget.size() - 1; i >= 0; i--)
                    {
                        proxyList.add(nodesToTarget.get(i));
                    }
                }

                if(!proxyList.isEmpty())
                {
                    return proxyList.get(0);
                }
                return target;
            }
        }

        return getProxy(target, worker.getPosition(), distanceToPath);
    }

    /**
     * Returns a proxy point to the goal.
     *
     * @param target         the target.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    @NotNull
    private BlockPos getProxy(@NotNull BlockPos target, @NotNull BlockPos position, double distanceToPath)
    {
        if (worker.getColony() == null)
        {
            return target;
        }

        double weight = Double.MAX_VALUE;
        BlockPos proxyPoint = null;

        for (BlockPos wayPoint : worker.getColony().getWayPoints(position, target))
        {
            final double simpleDistance = BlockPosUtil.getDistanceSquared(position, wayPoint);
            final double currentWeight = simpleDistance * simpleDistance + BlockPosUtil.getDistanceSquared(wayPoint, target);
            if (currentWeight < weight
                  && BlockPosUtil.getDistanceSquared2D(wayPoint, target) < distanceToPath
                  && simpleDistance > MIN_DISTANCE
                  && simpleDistance < distanceToPath
                  && !proxyList.contains(proxyPoint))
            {
                proxyPoint = wayPoint;
                weight = currentWeight;
            }
        }

        if (proxyList.contains(proxyPoint))
        {
            proxyPoint = null;
        }

        if (proxyPoint != null)
        {
            proxyList.add(proxyPoint);

            getProxy(target, proxyPoint, distanceToPath);

            return proxyList.get(0);
        }

        //No proxy point exists.
        return target;
    }
}
