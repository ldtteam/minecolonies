package com.minecolonies.core.entity.pathfinding.proxy;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Vec2i;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.MinerLevelManagementModule;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobMiner;
import com.minecolonies.core.entity.ai.workers.util.MinerLevel;
import com.minecolonies.core.entity.ai.workers.util.MineNode;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
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
    private final AbstractEntityCitizen citizen;

    /**
     * Creates a walkToProxy for a certain worker.
     *
     * @param entity the citizen entity.
     */
    public EntityCitizenWalkToProxy(final AbstractEntityCitizen entity)
    {
        super(entity);
        this.citizen = entity;
    }

    @Override
    public Set<BlockPos> getWayPoints()
    {
        if (citizen.getCitizenColonyHandler().getColonyOrRegister() == null)
        {
            return Collections.emptySet();
        }

        return citizen.getCitizenColonyHandler().getColonyOrRegister().getWayPoints().keySet();
    }

    @Override
    public boolean careAboutY()
    {
        return true;
    }

    @Override
    public BlockPos getSpecializedProxy(final BlockPos target, final double distanceToPath)
    {
        final IBuilding building = citizen.getCitizenColonyHandler().getWorkBuilding();
        if (citizen.getCitizenJobHandler().getColonyJob() != null && citizen.getCitizenJobHandler().getColonyJob() instanceof JobMiner && building instanceof BuildingMiner)
        {
            return getMinerProxy(target, distanceToPath, (BuildingMiner) building);
        }
        else if (citizen.getCitizenJobHandler().getColonyJob() != null && citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
        {
            if (building instanceof AbstractBuildingGuards)
            {
                AbstractBuildingGuards guardbuilding = (AbstractBuildingGuards) building;
                if (guardbuilding.getTask().equals(GuardTaskSetting.PATROL_MINE) && guardbuilding.getMinePos() != null)
                {
                    final IBuilding miner = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuilding(guardbuilding.getMinePos());
                    if (miner instanceof BuildingMiner)
                    {
                        return getMinerProxy(target, distanceToPath, (BuildingMiner) miner);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a proxy point to the goal for the miner especially.
     *
     * @param target         the target.
     * @param distanceToPath the total distance.
     * @param building       the building to walk to.
     * @return a proxy or, if not applicable null.
     */
    @NotNull
    private BlockPos getMinerProxy(final BlockPos target, final double distanceToPath, @NotNull final BuildingMiner building)
    {
        final MinerLevelManagementModule module = building.getFirstModuleOccurance(MinerLevelManagementModule.class);
        final MinerLevel level = module.getCurrentLevel();
        final BlockPos ladderPos = building.getLadderLocation();

        //If his current working level is null, we have nothing to worry about.
        if (level != null)
        {
            final BlockPos vector = building.getLadderLocation().subtract(building.getCobbleLocation());

            final int levelDepth = level.getDepth() + 2;
            final int targetY = target.getY();
            final int workerY = citizen.blockPosition().getY();

            //Check if miner is underground in shaft and his target is overground.
            if (workerY <= levelDepth && targetY > levelDepth)
            {
                if (module.getActiveNode() != null && module.getActiveNode().getParent() != null)
                {
                    MineNode currentNode = level.getNode(module.getActiveNode().getParent());
                    if (currentNode == null)
                    {
                        module.setActiveNode(null);
                        module.setOldNode(null);
                        return getProxy(target, citizen.blockPosition(), distanceToPath);
                    }

                    while (currentNode.getParent() != null)
                    {
                        if (currentNode.getStyle() == MineNode.NodeType.SHAFT)
                        {
                            final Direction facing = BlockPosUtil.getXZFacing(ladderPos, new BlockPos(currentNode.getX(), 0, currentNode.getZ()));
                            final BlockPos ladderHeight = new BlockPos(ladderPos.getX(), targetY + 1, ladderPos.getZ());

                            return new BlockPos(ladderHeight.relative(facing, 7));
                        }
                        else
                        {
                            addToProxyList(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        }
                        currentNode = level.getNode(currentNode.getParent());
                    }
                }

                addToProxyList(
                  new BlockPos(
                    ladderPos.getX() + vector.getX() * OTHER_SIDE_OF_SHAFT,
                    level.getDepth(),
                    ladderPos.getZ() + vector.getZ() * OTHER_SIDE_OF_SHAFT));
                return getProxy(target, citizen.blockPosition(), distanceToPath);

                //If he already is at ladder location, the closest node automatically will be his hut block.
            }
            //Check if target is underground in shaft and miner is over it.
            else if (targetY <= levelDepth && workerY > levelDepth)
            {
                final BlockPos buildingPos = building.getPosition();
                final BlockPos newProxy;

                //First calculate way to miner building.
                newProxy = getProxy(buildingPos, citizen.blockPosition(), BlockPosUtil.getDistanceSquared(citizen.blockPosition(), buildingPos));

                if (buildingPos.getY() - level.getDepth() > 25)
                {
                    addToProxyList(
                      new BlockPos(
                        ladderPos.getX() + vector.getX(),
                        level.getDepth() + (buildingPos.getY() - level.getDepth()) / 2,
                        ladderPos.getZ() + vector.getZ()));
                }

                //Then add the ladder position as the latest node.
                addToProxyList(
                  new BlockPos(
                    ladderPos.getX() + vector.getX() * OTHER_SIDE_OF_SHAFT,
                    level.getDepth(),
                    ladderPos.getZ() + vector.getZ() * OTHER_SIDE_OF_SHAFT));

                if (module.getActiveNode() != null && module.getActiveNode().getParent() != null)
                {
                    calculateNodes(level, levelDepth, building);
                }

                return newProxy;
            }
            //If he is on the same Y level as his target and both underground.
            else if (targetY <= levelDepth)
            {
                double closestNode = Double.MAX_VALUE;
                MineNode lastNode = null;
                for (final Map.Entry<Vec2i, MineNode> node : level.getNodes().entrySet())
                {
                    final double distanceToNode = node.getKey().distanceSq(citizen.blockPosition().getX(), citizen.blockPosition().getZ());
                    if (distanceToNode < closestNode)
                    {
                        lastNode = node.getValue();
                        closestNode = distanceToNode;
                    }
                }

                if (lastNode != null && lastNode.getStyle() == MineNode.NodeType.SHAFT)
                {
                    final Direction facing = BlockPosUtil.getXZFacing(ladderPos, new BlockPos(lastNode.getX(), 0, lastNode.getZ()));
                    final BlockPos ladderHeight = new BlockPos(ladderPos.getX(), targetY + 1, ladderPos.getZ());
                    return new BlockPos(ladderHeight.relative(facing, 7));
                }

                if (lastNode != null && lastNode.getParent() != null)
                {
                    MineNode currentNode = level.getNode(lastNode.getParent());
                    while (new Vec2i(currentNode.getX(), currentNode.getZ()).equals(currentNode.getParent()) && currentNode.getParent() != null)
                    {
                        addToProxyList(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
                        currentNode = level.getNode(currentNode.getParent());
                    }
                }

                if (module.getActiveNode() != null && module.getActiveNode().getParent() != null)
                {
                    calculateNodes(level, levelDepth, building);
                }

                if (!getProxyList().isEmpty())
                {
                    return getProxyList().get(0);
                }
                return target;
            }
        }

        return getProxy(target, citizen.blockPosition(), distanceToPath);
    }

    private void calculateNodes(final MinerLevel level, final int levelDepth, final BuildingMiner buildingMiner)
    {
        final List<BlockPos> nodesToTarget = new ArrayList<>();
        MineNode currentNode = level.getNode(buildingMiner.getFirstModuleOccurance(MinerLevelManagementModule.class).getActiveNode().getParent());
        while (currentNode != null && currentNode.getParent() != null)
        {
            if (currentNode.getStyle() == MineNode.NodeType.SHAFT)
            {
                final BlockPos ladderPos = buildingMiner.getLadderLocation();
                final Direction facing = BlockPosUtil.getXZFacing(ladderPos, new BlockPos(currentNode.getX(), 0, currentNode.getZ()));
                final BlockPos ladderHeight = new BlockPos(ladderPos.getX(), levelDepth + 1, ladderPos.getZ());
                nodesToTarget.add(new BlockPos(ladderHeight.relative(facing, 7)));
            }
            else
            {
                nodesToTarget.add(new BlockPos(currentNode.getX(), levelDepth, currentNode.getZ()));
            }
            currentNode = level.getNode(currentNode.getParent());
        }

        for (int i = nodesToTarget.size() - 1; i >= 0; i--)
        {
            addToProxyList(nodesToTarget.get(i));
        }
    }

    /**
     * Method to call to detect if an entity living is at site with move.
     *
     * @param entity the entity to check.
     * @param x      the x value.
     * @param y      the y value.
     * @param z      the z value.
     * @param range  the range.
     * @return true if so.
     */
    @Override
    public boolean isLivingAtSiteWithMove(final Mob entity, final int x, final int y, final int z, final int range)
    {
        if (!WorkerUtil.isWorkerAtSiteWithMove((AbstractEntityCitizen) entity, x, y, z, range))
        {
            EntityUtils.tryMoveLivingToXYZ(entity, x, y, z);
            return false;
        }
        return true;
    }
}
