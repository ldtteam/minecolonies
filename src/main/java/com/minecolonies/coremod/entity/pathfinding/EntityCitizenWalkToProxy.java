package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.entity.ai.pathfinding.AbstractWalkToProxy;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Vec2i;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EntityCitizenWalkToProxy extends AbstractWalkToProxy
{
    /**
     * Lead the miner to the other side of the shaft.
     */
    private static final int OTHER_SIDE_OF_SHAFT = 6;

    /**
     * The worker entity associated with the proxy.
     */
    private final EntityCitizen citizen;

    /**
     * Creates a walkToProxy for a certain worker.
     * @param entity the citizen entity.
     */
    public EntityCitizenWalkToProxy(final EntityCitizen entity)
    {
        super(entity);
        this.citizen = entity;
    }

    @Override
    public Set<BlockPos> getWayPoints()
    {
        if (citizen.getColony() == null)
        {
            return Collections.emptySet();
        }

        return citizen.getColony().getWayPoints().keySet();
    }

    @Override
    public boolean careAboutY()
    {
        return citizen.getColonyJob() instanceof JobBuilder;
    }

    @Override
    public BlockPos getSpecializedProxy(final BlockPos target, final double distanceToPath)
    {
        final AbstractBuildingWorker building = citizen.getWorkBuilding();
        if (citizen.getColonyJob() != null && citizen.getColonyJob() instanceof JobMiner && building instanceof BuildingMiner)
        {
            return getMinerProxy(target, distanceToPath, (BuildingMiner) building);
        }
        return null;
    }

    /**
     * Returns a proxy point to the goal for the miner especially.
     *
     * @param target         the target.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    @NotNull
    private BlockPos getMinerProxy(final BlockPos target, final double distanceToPath, @NotNull final BuildingMiner building)
    {
        final Level level = building.getCurrentLevel();
        final BlockPos ladderPos = building.getLadderLocation();

        //If his current working level is null, we have nothing to worry about.
        if (level != null)
        {
            final int levelDepth = level.getDepth() + 2;
            final int targetY = target.getY();
            final int workerY = citizen.getPosition().getY();

            //Check if miner is underground in shaft and his target is overground.
            if (workerY <= levelDepth && targetY > levelDepth)
            {
                if (level.getRandomNode() != null && level.getRandomNode().getParent() != null)
                {
                    com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(level.getRandomNode().getParent());
                    while (new Vec2i(currentNode.getX(), currentNode.getZ()).equals(currentNode.getParent()) && currentNode.getParent() != null)
                    {
                        addToProxyList(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }
                }

                addToProxyList(
                        new BlockPos(
                                ladderPos.getX() + building.getVectorX() * OTHER_SIDE_OF_SHAFT,
                                level.getDepth(),
                                ladderPos.getZ() + building.getVectorZ() * OTHER_SIDE_OF_SHAFT));
                return getProxy(target, citizen.getPosition(), distanceToPath);

                //If he already is at ladder location, the closest node automatically will be his hut block.
            }
            //Check if target is underground in shaft and miner is over it.
            else if (targetY <= levelDepth && workerY > levelDepth)
            {
                final BlockPos buildingPos = building.getLocation();
                BlockPos newProxy;

                //First calculate way to miner building.
                newProxy = getProxy(buildingPos, citizen.getPosition(), BlockPosUtil.getDistanceSquared(citizen.getPosition(), buildingPos));


                //Then add the ladder position as the latest node.
                addToProxyList(
                        new BlockPos(
                                ladderPos.getX() + building.getVectorX() * OTHER_SIDE_OF_SHAFT,
                                level.getDepth(),
                                ladderPos.getZ() + building.getVectorZ() * OTHER_SIDE_OF_SHAFT));

                if (level.getRandomNode() != null && level.getRandomNode().getParent() != null)
                {
                    calculateNodes(level, levelDepth);
                }

                return newProxy;
            }
            //If he is on the same Y level as his target and both underground.
            else if (targetY <= levelDepth)
            {
                double closestNode = Double.MAX_VALUE;
                com.minecolonies.coremod.entity.ai.citizen.miner.Node lastNode = null;
                for (final Map.Entry<Vec2i, com.minecolonies.coremod.entity.ai.citizen.miner.Node> node : level.getNodes().entrySet())
                {
                    final double distanceToNode = node.getKey().distanceSq(citizen.getPosition().getX(), citizen.getPosition().getZ());
                    if (distanceToNode < closestNode)
                    {
                        lastNode = node.getValue();
                        closestNode = distanceToNode;
                    }
                }

                if (lastNode != null && lastNode.getParent() != null)
                {
                    com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(lastNode.getParent());
                    while (new Vec2i(currentNode.getX(), currentNode.getZ()).equals(currentNode.getParent()) && currentNode.getParent() != null)
                    {
                        addToProxyList(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }
                }

                if (level.getRandomNode().getParent() != null)
                {
                    calculateNodes(level, levelDepth);
                }

                if (!getProxyList().isEmpty())
                {
                    return getProxyList().get(0);
                }
                return target;
            }
        }

        return getProxy(target, citizen.getPosition(), distanceToPath);
    }

    /**
     * Method to call to detect if an entity living is at site with move.
     * @param entity the entity to check.
     * @param x the x value.
     * @param y the y value.
     * @param z the z value.
     * @param range the range.
     * @return true if so.
     */
    @Override
    public boolean isLivingAtSiteWithMove(final EntityLiving entity, final int x, final int y, final int z, final int range)
    {
        return WorkerUtil.isWorkerAtSiteWithMove((EntityCitizen) entity, x, y, z, range);
    }

    private void calculateNodes(final Level level, final int levelDepth)
    {
        final List<BlockPos> nodesToTarget = new ArrayList<>();
        com.minecolonies.coremod.entity.ai.citizen.miner.Node currentNode = level.getNode(level.getRandomNode().getParent());
        while (new Vec2i(currentNode.getX(), currentNode.getZ()).equals(currentNode.getParent()) && currentNode.getParent() != null)
        {
            nodesToTarget.add(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
            currentNode = level.getNode(currentNode.getParent());
        }

        for (int i = nodesToTarget.size() - 1; i >= 0; i--)
        {
            addToProxyList(nodesToTarget.get(i));
        }
    }
}
