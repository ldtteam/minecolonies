package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.ldtteam.domumornamentum.block.decorative.FloatingCarpetBlock;
import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.ldtteam.domumornamentum.block.decorative.ShingleBlock;
import com.ldtteam.domumornamentum.block.decorative.ShingleSlabBlock;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.entity.pathfinding.IPathJob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ShapeUtil;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.entity.pathfinding.*;
import com.minecolonies.core.entity.pathfinding.navigation.IDynamicHeuristicNavigator;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.entity.pathfinding.world.CachingBlockLookup;
import com.minecolonies.core.entity.pathfinding.world.ChunkCache;
import com.minecolonies.core.network.messages.client.SyncPathMessage;
import com.minecolonies.core.util.WorkerUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;

import static com.minecolonies.api.util.constant.PathingConstants.*;
import static com.minecolonies.core.entity.pathfinding.PathingOptions.MAX_COST;

/**
 * Abstract class for Jobs that run in the multithreaded path finder.
 */
public abstract class AbstractPathJob implements Callable<Path>, IPathJob
{
    /**
     * Maximium amount of nodes explored
     */
    public static final int MAX_NODES = 5000;

    /**
     * Start position to path from.
     */
    @NotNull
    protected final BlockPos start;

    /**
     * The pathing cache.
     */
    @NotNull
    protected final LevelReader world;

    /**
     * The original world, do not use offthread
     */
    private final Level actualWorld;

    /**
     * The entity this job belongs to, can be none
     */
    @Nullable
    protected Mob entity = null;

    /**
     * Cached block lookup
     */
    protected CachingBlockLookup cachedBlockLookup;

    /**
     * Mutable pos used ot retrieve world info directly
     */
    protected BlockPos.MutableBlockPos tempWorldPos = new BlockPos.MutableBlockPos();

    /**
     * The result of the path calculation.
     */
    protected final PathResult result;

    /**
     * Maximum nodes we can visit.
     */
    protected int maxNodes;

    /**
     * Queue of all open nodes.
     */
    private Queue<MNode> nodesToVisit;

    /**
     * Queue of all the visited nodes.
     */
    private final Int2ObjectOpenHashMap<MNode> nodes = new Int2ObjectOpenHashMap<>();

    /**
     * Counts of nodes
     */
    private   int totalNodesAdded   = 0;
    protected int totalNodesVisited = 0;

    /**
     * Additional nodes that get explored when reaching the target, useful when the destination is an area or not in a great spot.
     * Pathjobs may increase this value as they see fit
     */
    public int extraNodes = 0;

    /**
     * Debug settings
     */
    protected boolean    debugDrawEnabled       = false;
    protected Set<MNode> debugNodesVisited      = null;
    protected Set<MNode> debugNodesVisitedLater = null;
    protected Set<MNode> debugNodesNotVisited   = null;
    protected Set<MNode> debugNodesPath         = null;
    protected Set<MNode> debugNodesOrgPath      = null;
    protected Set<MNode> debugNodesExtra        = null;

    /**
     * The cost values for certain nodes.
     */
    private PathingOptions pathingOptions = new PathingOptions();

    /**
     * Whether the path reached its destination
     */
    private boolean reachesDestination = false;

    /**
     * The maximum cost discoverd
     */
    private double maxCost = 0;

    /**
     * Heuristic modifier
     */
    private double heuristicMod = 1;

    /**
     * First node
     */
    private MNode startNode = null;

    /**
     * Visited level
     */
    private int visitedLevel = 1;

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param range  estimated maximum path range.
     * @param result path result.
     * @param entity the entity.
     */
    public AbstractPathJob(final Level world, @NotNull final BlockPos start, int range, final PathResult result, @Nullable final Mob entity)
    {
        range = Math.max(10, range);

        // 30% Extra range to account for heuristics/cost based less circular exploring
        final int minX = (int) (start.getX() - range * 1.3);
        final int minZ = (int) (start.getZ() - range * 1.3);
        final int maxX = (int) (start.getX() + range * 1.3);
        final int maxZ = (int) (start.getZ() + range * 1.3);
        this.world = new ChunkCache(world, new BlockPos(minX, 0, minZ), new BlockPos(maxX, 0, maxZ));
        this.actualWorld = world;

        this.maxNodes = Math.min(MAX_NODES, range * range);
        nodesToVisit = new PriorityQueue<>(range * 2);
        this.start = new BlockPos(start);

        cachedBlockLookup = new CachingBlockLookup(start, this.world);

        this.result = result;
        result.setJob(this);

        this.entity = entity;
    }

    /**
     * Internal constructor, for secondary pathjobs within another one
     *
     * @param chunkCache
     * @param start
     * @param range
     * @param result
     * @param entity
     */
    protected AbstractPathJob(final Level actualWorld, final LevelReader chunkCache, @NotNull final BlockPos start, int range, final PathResult result, @Nullable final Mob entity)
    {
        range = Math.max(10, range);
        this.maxNodes = Math.min(MAX_NODES, range * range);
        nodesToVisit = new PriorityQueue<>(range * 2);
        this.start = new BlockPos(start);

        world = chunkCache;
        cachedBlockLookup = new CachingBlockLookup(start, this.world);
        this.actualWorld = actualWorld;

        this.result = result;
        result.setJob(this);

        this.entity = entity;
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param result path result.
     * @param entity the entity.
     */
    public AbstractPathJob(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final PathResult result, @Nullable final Mob entity)
    {
        // Load at least 2 chunks further around start+end, extended with more distance
        final int expandedRange = (2 * 16) + BlockPosUtil.distManhattan(start, end) / 2;

        final int minX = Math.min(start.getX(), end.getX()) - expandedRange;
        final int minZ = Math.min(start.getZ(), end.getZ()) - expandedRange;
        final int maxX = Math.max(start.getX(), end.getX()) + expandedRange;
        final int maxZ = Math.max(start.getZ(), end.getZ()) + expandedRange;
        this.world = new ChunkCache(world, new BlockPos(minX, 0, minZ), new BlockPos(maxX, 0, maxZ));

        // Max nodes in relation to the box area
        final int xDiff = Math.max(1, Math.abs(start.getX() - end.getX()));
        // Higher limit for Y changes, as Y is more difficult to traverse(jump/drop costs)
        final int yDiff = Math.max(1, Math.abs((start.getY() - end.getY()))) * 5;
        final int zDiff = Math.max(1, Math.abs((start.getZ() - end.getZ())));

        this.maxNodes =
          Math.min(MAX_NODES, 300 + Math.max(Math.max(Math.max(2, xDiff / 10) * yDiff * zDiff, xDiff * Math.max(2, yDiff / 10) * zDiff), xDiff * yDiff * Math.max(2, zDiff / 10)));
        nodesToVisit = new PriorityQueue<>(maxNodes / 4);
        this.start = new BlockPos(start);

        cachedBlockLookup = new CachingBlockLookup(start, this.world);
        actualWorld = world;

        this.result = result;
        result.setJob(this);
        this.entity = entity;
    }

    /**
     * Callable method for initiating asynchronous task.
     *
     * @return path to follow or null.
     */
    @Override
    public final Path call()
    {
        try
        {
            return search();
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Pathfinding Exception from: " + start + " range: " + Math.sqrt(maxNodes) + " entity: " + entity + " type: " + getClass().getSimpleName(), e);
        }

        return null;
    }

    /**
     * Sets the initial first node up
     *
     * @return
     */
    private MNode getAndSetupStartNode()
    {
        final MNode startNode = new MNode(null, start.getX(), start.getY(), start.getZ(), 0, computeHeuristic(start.getX(), start.getY(), start.getZ()));

        if (PathfindingUtils.isLadder(cachedBlockLookup.getBlockState(start.getX(), start.getY(), start.getZ()), pathingOptions))
        {
            startNode.setLadder();
        }
        else if (!pathingOptions.canWalkUnderWater() && PathfindingUtils.isLiquid(cachedBlockLookup.getBlockState(start.below())))
        {
            startNode.setSwimming();
        }

        startNode.setOnRails(pathingOptions.canUseRails() && cachedBlockLookup.getBlockState(start).getBlock() instanceof BaseRailBlock);

        nodesToVisit.offer(startNode);
        nodes.put(MNode.computeNodeKey(start.getX(), start.getY(), start.getZ()), startNode);

        ++totalNodesAdded;

        this.startNode = startNode;
        return startNode;
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    protected Path search()
    {
        MNode bestNode = getAndSetupStartNode();
        double bestNodeEndScore = getEndNodeScore(bestNode);
        // Node count since we found a better end node than the current one
        int nodesSinceEndNode = 0;

        while (!nodesToVisit.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            final MNode node = nodesToVisit.poll();

            if (node.isVisited())
            {
                // Revisiting is used to update neighbours to an updated cost
                visitNode(node);
                node.increaseVisited();
                continue;
            }

            nodesSinceEndNode++;
            totalNodesVisited++;

            // Limiting max amount of nodes mapped, encountering a high cost node increases the limit
            if (totalNodesVisited > maxNodes + (maxCost * maxCost) * 2)
            {
                if (stopOnNodeLimit(totalNodesVisited, bestNode, nodesSinceEndNode))
                {
                    break;
                }
            }

            if (!reachesDestination && isAtDestination(node))
            {
                bestNode = node;
                bestNodeEndScore = getEndNodeScore(node);
                result.setPathReachesDestination(true);
                handleDebugPathReach(bestNode);

                reachesDestination = true;
                if (reevaluteHeuristic(bestNode, true))
                {
                    recalcHeuristic(bestNode);
                }
                else
                {
                    break;
                }
            }

            // Re-evaluate current heuristic when progress is going bad
            if (((nodesSinceEndNode >= maxNodes / 2 && nodesSinceEndNode % 400 == 0)) && !reachesDestination)
            {
                if (reevaluteHeuristic(bestNode, reachesDestination))
                {
                    recalcHeuristic(bestNode);
                    recalcHeuristic(node);
                }
            }

            if (!node.isCornerNode())
            {
                // Calculates a score for a possible end node, defaults to heuristic(closest)
                final double nodeEndSCore = getEndNodeScore(node);
                if (nodeEndSCore < bestNodeEndScore)
                {
                    if (!reachesDestination || isAtDestination(node))
                    {
                        nodesSinceEndNode = 0;
                        bestNode = node;
                        bestNodeEndScore = nodeEndSCore;
                    }
                }
            }

            // Don't keep searching more costly nodes when there is a destination
            if (reachesDestination && node.getScore() > bestNode.getScore())
            {
                if (reevaluteHeuristic(bestNode, reachesDestination))
                {
                    recalcHeuristic(bestNode);
                    recalcHeuristic(node);
                }
                else
                {
                    break;
                }
            }

            handleDebugOptions(node);
            visitNode(node);
            node.increaseVisited();
        }

        // Explore additional possible endnodes after reaching, if we got extra nodes to search
        if (extraNodes > 0 && reachesDestination)
        {
            // Make sure to expand from the final node
            visitNode(bestNode);

            if (!nodesToVisit.isEmpty())
            {
                // Search only closest nodes to the goal
                final Queue<MNode> original = nodesToVisit;
                nodesToVisit = new PriorityQueue<>(nodesToVisit.size(), (a, b) -> {
                    if ((a.getHeuristic()) < (b.getHeuristic()))
                    {
                        return -1;
                    }
                    else if (a.getHeuristic() > b.getHeuristic())
                    {
                        return 1;
                    }
                    else
                    {
                        return a.getCounterAdded() - b.getCounterAdded();
                    }
                });
                nodesToVisit.addAll(original);

                while (!nodesToVisit.isEmpty())
                {
                    if (Thread.currentThread().isInterrupted())
                    {
                        return null;
                    }

                    final MNode node = nodesToVisit.poll();
                    if (node.isVisited())
                    {
                        visitNode(node);
                        continue;
                    }

                    handleDebugExtraNode(node);

                    final double nodeEndSCore = getEndNodeScore(node);
                    if (nodeEndSCore < bestNodeEndScore && (!reachesDestination || isAtDestination(node)))
                    {
                        bestNode = node;
                        bestNodeEndScore = nodeEndSCore;
                    }

                    if (extraNodes > 0)
                    {
                        extraNodes--;
                        if (extraNodes == 0)
                        {
                            break;
                        }
                    }
                    visitNode(node);
                }
            }
        }

        return finalizePath(bestNode);
    }

    /**
     * Stops the pathjob when hitting a node limit
     *
     * @param totalNodesVisited
     * @param bestNode
     * @param nodesSinceEndNode
     * @return
     */
    protected boolean stopOnNodeLimit(final int totalNodesVisited, final MNode bestNode, final int nodesSinceEndNode)
    {
        return true;
    }

    /**
     * Analyzes the heuristic for an overestimation
     *
     * @param node    currently "best" node
     * @param reaches if we did reach the destination
     * @return true if the heuristic estimation got adjusted
     */
    private boolean reevaluteHeuristic(final MNode node, final boolean reaches)
    {
        if (startNode.getHeuristic() < 0.01)
        {
            return false;
        }

        double costPerEstimation = node.getCost() / startNode.getHeuristic();

        if (!reaches)
        {
            if (node.parent != null && this instanceof IDestinationPathJob job)
            {
                final double heuristicCostEstimationPerDist = startNode.getHeuristic() / Math.max(1, BlockPosUtil.dist(job.getDestination(), start));

                int dist = 0;
                MNode currNode = node;
                while (currNode.parent != null)
                {
                    currNode = currNode.parent;
                    dist++;
                }

                final double realCostPerDist = node.getCost() / dist;
                costPerEstimation = realCostPerDist / heuristicCostEstimationPerDist;
            }
            else
            {
                int count = 0;
                costPerEstimation = 0;
                // Assume linearity
                double lowestAroundStart = Double.MAX_VALUE;
                lowestAroundStart = Math.min(lowestAroundStart, computeHeuristic(startNode.x + 1, startNode.y, startNode.z) * heuristicMod);
                lowestAroundStart = Math.min(lowestAroundStart, computeHeuristic(startNode.x - 1, startNode.y, startNode.z) * heuristicMod);
                lowestAroundStart = Math.min(lowestAroundStart, computeHeuristic(startNode.x, startNode.y, startNode.z + 1) * heuristicMod);
                lowestAroundStart = Math.min(lowestAroundStart, computeHeuristic(startNode.x, startNode.y, startNode.z - 1) * heuristicMod);
                lowestAroundStart = Math.min(lowestAroundStart, computeHeuristic(startNode.x, startNode.y + 1, startNode.z) * heuristicMod);
                lowestAroundStart = Math.min(lowestAroundStart, computeHeuristic(startNode.x, startNode.y - 1, startNode.z) * heuristicMod);

                final double heuristicPerDist = startNode.getHeuristic() - lowestAroundStart;

                if (heuristicPerDist <= 0)
                {
                    return false;
                }

                for (final MNode cur : nodesToVisit)
                {
                    if (cur.getHeuristic() >= startNode.getHeuristic() || cur.isVisited())
                    {
                        continue;
                    }

                    count++;
                    costPerEstimation += cur.getCost() / (BlockPosUtil.distManhattan(cur.x, cur.y, cur.z, startNode.x, startNode.y, startNode.z) * heuristicPerDist);

                    if (count == 20)
                    {
                        break;
                    }
                }

                if (count == 0)
                {
                    return false;
                }

                costPerEstimation = costPerEstimation / count;
            }
        }

        if (costPerEstimation <= 0.0)
        {
            return false;
        }

        // Detect an overstimating heuristic(not guranteed, but can check the found path)
        if (costPerEstimation < 0.9 || (costPerEstimation > 1.2 && !reaches))
        {
            // Overshoot a bit
            costPerEstimation *= costPerEstimation < 1 ? 0.9 : 1.1;

            if (reaches && entity != null && entity.getNavigation() instanceof IDynamicHeuristicNavigator navigator)
            {
                double foundPathCostPerDist = node.getCost() / Math.max(1, BlockPosUtil.distManhattan(start, node.x, node.y, node.z));

                // If the path we found is per block more expensive than the entities historic we explore more for a potential cheaper path
                if (foundPathCostPerDist > navigator.getAvgHeuristicModifier())
                {
                    double modifier = Math.min(0.8, Math.max(0.3, navigator.getAvgHeuristicModifier() / foundPathCostPerDist));
                    costPerEstimation *= modifier;
                }
            }

            // Set a future heuristic modification
            if (reaches)
            {
                heuristicMod *= costPerEstimation;
            }
            else
            {
                // When not reaching slowly adjust to assist reaching
                double currentMod = heuristicMod;
                heuristicMod -= heuristicMod / 2;
                heuristicMod += (currentMod * costPerEstimation) / 2;
            }

            // Fix up existing heuristic values
            final List<MNode> nodes = new ArrayList<>(nodesToVisit);
            nodesToVisit.clear();
            for (final MNode recalc : nodes)
            {
                recalcHeuristic(recalc);
                nodesToVisit.offer(recalc);
            }

            recalcHeuristic(startNode);
            recalcHeuristic(node);
            visitedLevel++;
            return true;
        }

        return false;
    }

    /**
     * Recalculates the heuristic value for the node
     *
     * @param node given node
     */
    private void recalcHeuristic(final MNode node)
    {
        node.setHeuristic(computeHeuristic(node.x, node.y, node.z) * heuristicMod);
    }

    /**
     * Visits the given node and explores neighbours
     *
     * @param node
     */
    protected void visitNode(final MNode node)
    {
        cachedBlockLookup.resetToNextPos(node.x, node.y, node.z);

        int dX = 0;
        int dY = 0;
        int dZ = 0;

        if (node.parent != null)
        {
            dX = node.x - node.parent.x;
            dY = node.y - node.parent.y;
            dZ = node.z - node.parent.z;
        }

        if (node.isLadder() || node.isVisited())
        {
            exploreInDirection(node, 0, 1, 0);
            exploreInDirection(node, 0, -1, 0);
        }
        // Only explore downwards when dropping
        else if (node.isCornerNode() && (node.parent == null || !(dX == 0 && dY == 1 && dZ == 0)))
        {
            exploreInDirection(node, 0, -1, 0);
            return;
        }
        // Walk downwards node if passable
        else if (!node.isSwimming() && isPassable(node.x, node.y - 1, node.z, false, node.parent))
        {
            exploreInDirection(node, 0, -1, 0);
        }

        // N
        if (dZ <= 0)
        {
            exploreInDirection(node, 0, 0, -1);
        }

        // E
        if (dX >= 0)
        {
            exploreInDirection(node, 1, 0, 0);
        }

        // S
        if (dZ >= 0)
        {
            exploreInDirection(node, 0, 0, 1);
        }

        // W
        if (dX <= 0)
        {
            exploreInDirection(node, -1, 0, 0);
        }
    }

    /**
     * "Walk" from the parent in the direction specified by the delta, determining the new x,y,z position for such a move and adding or updating a node, as appropriate.
     *
     * @param node Node being walked from.
     */
    protected final void exploreInDirection(final MNode node, int dX, int dY, int dZ)
    {
        int nextX = node.x + dX;
        int nextY = node.y + dY;
        int nextZ = node.z + dZ;

        //  Can we traverse into this node?  Fix the y up, skip on already explored nodes
        final int newY = node.isVisited() ? nextY : getGroundHeight(node, nextX, nextY, nextZ);

        if (newY < world.getMinBuildHeight())
        {
            return;
        }

        boolean corner = false;
        if (nextY != newY)
        {
            if (node.isCornerNode() && (dX != 0 || dZ != 0))
            {
                return;
            }

            // if the new position is above the current node, we're taking the node directly above
            if (!node.isCornerNode() && newY - node.y > 0 && (node.parent == null || !BlockPosUtil.equals(node.parent.x,
              node.parent.y,
              node.parent.z,
              node.x,
              node.y + newY - nextY,
              node.z)))
            {
                dX = 0;
                dY = newY - nextY;
                dZ = 0;

                nextX = node.x + dX;
                nextY = node.y + dY;
                nextZ = node.z + dZ;
                corner = true;
            }
            // If we're going down, take the air-corner before going to the lower node
            else if (!node.isCornerNode() && newY - node.y < 0 && (dX != 0 || dZ != 0) &&
                       (node.parent == null || (node.x != node.parent.x || node.y - 1 != node.parent.y
                                                  || node.z != node.parent.z)))
            {
                dY = 0;

                nextX = node.x + dX;
                nextY = node.y + dY;
                nextZ = node.z + dZ;

                corner = true;
            }
            // Fix up normal y
            else
            {
                dX = 0;
                dY = newY - nextY;
                dZ = 0;

                nextY = newY;
            }
        }

        final int nodeKey = MNode.computeNodeKey(nextX, nextY, nextZ);
        MNode nextNode = nodes.get(nodeKey);

        if (nextNode != null && nextNode.isCornerNode())
        {
            if (node.isCornerNode())
            {
                // Do not allow connecting corner nodes
                return;
            }

            corner = true;
        }

        // Current node is already visited, only update nearby costs do not create new nodes
        if (node.isVisited())
        {
            if (nextNode == null || nextNode == node.parent)
            {
                return;
            }
        }

        final BlockState aboveState = cachedBlockLookup.getBlockState(nextX, nextY + 1, nextZ);
        final BlockState state = cachedBlockLookup.getBlockState(nextX, nextY, nextZ);
        final BlockState belowState = cachedBlockLookup.getBlockState(nextX, nextY - 1, nextZ);

        final boolean isSwimming = calculateSwimming(belowState, state, aboveState, nextNode);
        if (isSwimming && !pathingOptions.canSwim())
        {
            return;
        }

        final boolean swimStart = isSwimming && !node.isSwimming();
        final boolean onRoad = WorkerUtil.isPathBlock(belowState.getBlock());
        final boolean onRails = pathingOptions.canUseRails() && (corner ? belowState : state).getBlock() instanceof BaseRailBlock;
        final boolean railsExit = !onRails && node != null && node.isOnRails();
        final boolean ladder = PathfindingUtils.isLadder(state, pathingOptions);
        final boolean isDiving = isSwimming && PathfindingUtils.isWater(world, null, aboveState, null);

        double nextCost = 0;
        if (!corner)
        {
            MNode costFrom = node;
            // Base cost calc on parent if we're expanding from a corner node
            if (node.isCornerNode() && node.parent != null)
            {
                dX = nextX - node.parent.x;
                dY = nextY - node.parent.y;
                dZ = nextZ - node.parent.z;
                costFrom = node.parent;
            }

            nextCost = computeCost(costFrom, dX, dY, dZ, isSwimming, onRoad, isDiving, onRails, railsExit, swimStart, ladder, state, belowState, nextX, nextY, nextZ);
            nextCost = modifyCost(nextCost, costFrom, swimStart, isSwimming, nextX, nextY, nextZ, state, belowState);

            if (nextCost > maxCost)
            {
                maxCost = Math.min(MAX_COST, Math.ceil(nextCost));
            }
        }

        final double heuristic = computeHeuristic(nextX, nextY, nextZ) * heuristicMod;
        final double cost = node.getCost() + nextCost;

        if (nextNode == null)
        {
            nextNode = createNode(node, nextX, nextY, nextZ, nodeKey, heuristic, cost);
            nextNode.setOnRails(onRails);
            nextNode.setCornerNode(corner);
            if (isSwimming)
            {
                nextNode.setSwimming();
            }

            if (ladder)
            {
                nextNode.setLadder();
            }

            nodesToVisit.offer(nextNode);
        }
        else
        {
            updateNode(node, nextNode, heuristic, cost);
        }
    }

    @NotNull
    private MNode createNode(
      final MNode parent, final int x, final int y, final int z, final int nodeKey, final double heuristic, final double cost)
    {
        final MNode node;
        node = new MNode(parent, x, y, z, cost, heuristic);
        nodes.put(nodeKey, node);
        if (debugDrawEnabled)
        {
            debugNodesNotVisited.add(node);
        }

        totalNodesAdded++;
        node.setCounterAdded(totalNodesAdded);
        return node;
    }

    /**
     * Updates an already existing node with new heuristic/cost valvues
     *
     * @param node
     * @param heuristic
     * @param cost
     * @return
     */
    private void updateNode(@NotNull final MNode node, @NotNull final MNode nextNode, final double heuristic, final double cost)
    {
        //  This node already exists
        if (cost >= nextNode.getCost() || nextNode.getVisitedCount() > visitedLevel)
        {
            return;
        }

        nodesToVisit.remove(nextNode);
        nextNode.parent = node;
        nextNode.setCost(cost);
        nextNode.setHeuristic(heuristic);

        nodesToVisit.offer(nextNode);
    }

    /**
     * Compute the heuristic cost ('h' value) of a given position x,y,z.
     * <p>
     * Returning a value of 0 performs a breadth-first search. Returning a value less than actual possible cost to goal guarantees shortest path, but at computational expense.
     * Returning a value exactly equal to the cost to the goal guarantees shortest path and least expense (but generally. only works when path is straight and unblocked). Returning
     * a value greater than the actual cost to goal produces good, but not perfect paths, and is fast. Returning a very high value (such that 'h' is very high relative to 'g') then
     * only 'h' (the heuristic) matters as the search will be a very fast greedy best-first-search, ignoring cost weighting and distance.
     *
     * @return the heuristic.
     */
    protected abstract double computeHeuristic(final int x, final int y, final int z);

    /**
     * Return true if the given node is a viable final destination, and the path should generate to here.
     *
     * @param n Node to test.
     * @return true if the node is a viable destination.
     */
    protected abstract boolean isAtDestination(MNode n);

    /**
     * Calculates a score for potential points where the path may end given no destination
     * By default the heuristic for the closest node is used
     *
     * @param n Node to test.
     * @return score for the node.
     */
    protected double getEndNodeScore(MNode n)
    {
        return n.getHeuristic();
    }

    /**
     * Compute the cost (immediate 'g' value) of moving from the parent space to the new space.
     *
     * @param parent
     * @param isSwimming true is the current node would require the citizen to swim.
     * @param onPath     checks if the node is on a path.
     * @param onRails    checks if the node is a rail block.
     * @param railsExit  the exit of the rails.
     * @param swimStart  if its the swim start.
     * @param blockState
     * @return cost to move from the parent to the new position.
     */
    protected double computeCost(
      final MNode parent, final int dX, final int dY, final int dZ,
      final boolean isSwimming,
      final boolean onPath,
      final boolean isDiving,
      final boolean onRails,
      final boolean railsExit,
      final boolean swimStart,
      final boolean ladder,
      final BlockState state, final BlockState below,
      final int x, final int y, final int z)
    {
        double cost = 1;

        if (pathingOptions.randomnessFactor > 0.0d)
        {
            cost += ColonyConstants.rand.nextDouble() * pathingOptions.randomnessFactor;
        }

        if (!isSwimming)
        {
            if (onPath)
            {
                cost *= pathingOptions.onPathCost;
            }
            if (onRails)
            {
                cost *= pathingOptions.onRailCost;
            }
        }

        if (state.getBlock() == Blocks.CAVE_AIR)
        {
            cost += pathingOptions.caveAirCost;
        }

        if (!isDiving)
        {
            if (dY != 0 && !(ladder && parent.isLadder()) && !(Math.abs(dY) == 1 && below.is(BlockTags.STAIRS)))
            {
                if (dY > 0)
                {
                    cost += pathingOptions.jumpCost;
                }
                else if (pathingOptions.dropCost != 0)
                {
                    cost += pathingOptions.dropCost * Math.abs(dY * dY * dY);
                }
            }
        }

        if (state.hasProperty(BlockStateProperties.OPEN) && !(state.getBlock() instanceof PanelBlock))
        {
            cost += pathingOptions.traverseToggleAbleCost;
        }
        else if (!onPath && !ShapeUtil.hasCollision(cachedBlockLookup, tempWorldPos.set(x, y, z), state))
        {
            cost += pathingOptions.walkInShapesCost;
        }

        if (below.getBlock() instanceof ShingleBlock || below.getBlock() instanceof ShingleSlabBlock)
        {
            cost += 3;
        }

        if (railsExit)
        {
            cost += pathingOptions.railsExitCost;
        }

        if (!isDiving && ladder && !parent.isLadder() && !(state.getBlock() instanceof LadderBlock))
        {
            cost += pathingOptions.nonLadderClimbableCost;
        }

        if (isSwimming)
        {
            if (swimStart)
            {
                cost += pathingOptions.swimCostEnter;
            }
            else
            {
                cost += pathingOptions.swimCost;
            }
            if (isDiving)
            {
                cost += pathingOptions.divingCost;
            }
        }

        return cost;
    }

    /**
     * Modifies costs if needed for a node
     *
     * @param cost
     * @param parent
     * @param swimstart
     * @param swimming
     * @param blockState
     * @param state
     * @return
     */
    protected double modifyCost(
      final double cost,
      final MNode parent,
      final boolean swimstart,
      final boolean swimming,
      final int x,
      final int y,
      final int z,
      final BlockState state, final BlockState below)
    {
        return cost;
    }

    /**
     * Generate the path to the target node.
     *
     * @param targetNode the node to path to.
     * @return the path.
     */
    @NotNull
    private Path finalizePath(final MNode targetNode)
    {
        //  Compute length of path, since we need to allocate an array.  This is cheaper/faster than building a List
        //  and converting it.  Yes, we have targetNode.steps, but I do not want to rely on that being accurate (I might
        //  fudge that value later on for cutoff purposes
        int pathLength = 1;
        int railsLength = 0;
        MNode node = targetNode;
        while (node.parent != null)
        {
            ++pathLength;
            if (node.isOnRails())
            {
                ++railsLength;
            }
            node = node.parent;
        }

        final Node[] points = new Node[pathLength];
        points[0] = new PathPointExtended(new BlockPos(node.x, node.y, node.z));
        if (debugDrawEnabled)
        {
            addPathNodeToDebug(node);
        }


        MNode nextInPath = null;
        Node next = null;
        node = targetNode;
        while (node.parent != null)
        {
            if (debugDrawEnabled)
            {
                addPathNodeToDebug(node);
            }

            --pathLength;

            final BlockPos pos = new BlockPos(node.x, node.y, node.z);

            if (node.isSwimming())
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                pos.offset(BLOCKPOS_DOWN);
            }

            final PathPointExtended p = new PathPointExtended(pos);
            if (railsLength >= MineColonies.getConfig().getServer().minimumRailsToPath.get())
            {
                p.setOnRails(node.isOnRails());
                if (p.isOnRails() && (!node.parent.isOnRails() || node.parent.parent == null))
                {
                    p.setRailsEntry();
                }
                else if (p.isOnRails() && points.length > pathLength + 1)
                {
                    final PathPointExtended point = ((PathPointExtended) points[pathLength + 1]);
                    if (!point.isOnRails())
                    {
                        point.setRailsExit();
                    }
                }
            }

            if (node.isLadder())
            {
                p.setOnLadder(true);
                // TODO: Check working, logic is a bit odd
                if (nextInPath != null && nextInPath.y > pos.getY())
                {
                    //  We only care about facing if going up
                    //In the case of BlockVines (Which does not have Direction) we have to check the metadata of the vines... bitwise...
                    PathfindingUtils.setLadderFacing(world, pos, p);
                }
            }

            if (next != null)
            {
                next.cameFrom = p;
            }
            next = p;
            points[pathLength] = p;

            nextInPath = node;
            node = node.parent;
        }

        doDebugPrinting(points);

        if (points.length > 1)
        {
            result.costPerDist = targetNode.getCost() / BlockPosUtil.distManhattan(start, targetNode.x, targetNode.y, targetNode.z);
        }

        result.searchedNodes = totalNodesVisited;
        return new Path(Arrays.asList(points), new BlockPos(targetNode.x, targetNode.y, targetNode.z), reachesDestination);
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y.
     *
     * @param node parent node.
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop.
     */
    protected int getGroundHeight(final MNode node, final int x, final int y, final int z)
    {
        if (!pathingOptions.canWalkUnderWater() && PathfindingUtils.isLiquid(cachedBlockLookup.getBlockState(x, y + 1, z)))
        {
            return -100;
        }
        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (checkHeadBlock(node, x, y, z))
        {
            return handleTargetNotPassable(node, x, y + 1, z, cachedBlockLookup.getBlockState(x, y + 1, z));
        }

        //  Now check the block we want to move to
        final BlockState target = cachedBlockLookup.getBlockState(x, y, z);
        if (!isPassable(target, x, y, z, node, false))
        {
            return handleTargetNotPassable(node, x, y, z, target);
        }

        //  Do we have something to stand on in the target space?
        final BlockState below = cachedBlockLookup.getBlockState(x, y - 1, z);
        final SurfaceType walkability = SurfaceType.getSurfaceType(world, below, tempWorldPos.set(x, y - 1, z), pathingOptions);
        if (walkability == SurfaceType.WALKABLE)
        {
            //  Level path
            return y;
        }
        else if (walkability == SurfaceType.NOT_PASSABLE)
        {
            return -100;
        }

        return handleNotStanding(node, x, y, z, below);
    }

    /**
     * Checks for headblock space
     *
     * @param parent
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean checkHeadBlock(@Nullable final MNode parent, final int x, final int y, final int z)
    {

        if (!canLeaveBlock(x, y + 1, z, parent, true))
        {
            return true;
        }

        if (!isPassable(x, y + 1, z, true, parent))
        {
            // TODO: Checking +1 and -1 seems odd? probably one intended to be current instead
            final VoxelShape bb1 = cachedBlockLookup.getBlockState(x, y - 1, z).getCollisionShape(world, tempWorldPos.set(x, y - 1, z));
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(x, y + 1, z).getCollisionShape(world, tempWorldPos.set(x, y + 1, z));
            if ((y + 1 + ShapeUtil.getStartY(bb2, 1)) - (y - 1 + ShapeUtil.getEndY(bb1, 0)) < 2)
            {
                return true;
            }
            if (parent != null)
            {
                final VoxelShape bb3 =
                  cachedBlockLookup.getBlockState(parent.x, parent.y - 1, parent.z).getCollisionShape(world, tempWorldPos.set(parent.x, parent.y - 1, parent.z));
                if ((y + 1 + ShapeUtil.getStartY(bb2, 1)) - (parent.y - 1 + ShapeUtil.getEndY(bb3, 0)) < 1.75)
                {
                    return true;
                }
            }
        }

        if (parent != null)
        {
            final BlockState belowState = cachedBlockLookup.getBlockState(x, y - 1, z);
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(x, y + 1, z).getCollisionShape(world, tempWorldPos.set(x, y + 1, z));
            final VoxelShape bb = cachedBlockLookup.getBlockState(x, y, z).getCollisionShape(world, tempWorldPos.set(x, y, z));
            if ((y + 1 + ShapeUtil.getStartY(bb2, 1)) - (y + ShapeUtil.getEndY(bb, 0)) >= 2)
            {
                return false;
            }

            return parent.isSwimming() && PathfindingUtils.isLiquid(belowState) && !isPassable(x, y, z, false, parent);
        }
        return false;
    }

    /**
     * Is the space passable.
     *
     * @param block  the block we are checking.
     * @param parent the parent node.
     * @param head   the head position.
     * @return true if the block does not block movement.
     */
    protected boolean isPassable(@NotNull final BlockState block, final int x, final int y, final int z, final MNode parent, final boolean head)
    {
        if (!canLeaveBlock(x, y, z, parent, head))
        {
            return false;
        }

        if (!block.isAir())
        {
            final VoxelShape shape = block.getCollisionShape(world, tempWorldPos.set(x, y, z));
            if (!pathingOptions.canPassDanger() && ShapeUtil.max(shape, Direction.Axis.Y) < 0.5 && PathfindingUtils.isDangerous(cachedBlockLookup.getBlockState(x, y - 1, z)))
            {
                return false;
            }
            if (block.blocksMotion() && !(ShapeUtil.isEmpty(shape) || ShapeUtil.max(shape, Direction.Axis.Y) <= 0.1))
            {
                if (block.getBlock() instanceof TrapDoorBlock || block.getBlock() instanceof PanelBlock)
                {
                    int parentY = parent == null ? start.getY() : parent.y;
                    if (head)
                    {
                        parentY++;
                    }

                    final int dY = y - parentY;

                    final Direction direction = BlockPosUtil.getXZFacing(parent == null ? start.getX() : parent.x, parent == null ? start.getZ() : parent.z, x, z);
                    final Direction facing = block.getValue(TrapDoorBlock.FACING);

                    if (block.getBlock() instanceof PanelBlock && !block.getValue(PanelBlock.OPEN))
                    {
                        if (dY == 0)
                        {
                            return (head && block.getValue(PanelBlock.HALF) == Half.TOP);
                        }

                        if (head && dY == 1 && block.getValue(PanelBlock.HALF) == Half.TOP)
                        {
                            return true;
                        }

                        if (!head && dY == -1 && block.getValue(PanelBlock.HALF) == Half.BOTTOM)
                        {
                            return true;
                        }

                        return false;
                    }

                    // We can enter a space of a trapdoor if it's facing the same direction
                    if (direction == facing.getOpposite())
                    {
                        return true;
                    }

                    // We cannot enter a space of a trapdoor if its facing the opposite direction.
                    if (direction == facing)
                    {
                        return false;
                    }

                    return true;
                }
                else
                {
                    return pathingOptions.canEnterDoors() && (block.getBlock() instanceof DoorBlock || block.getBlock() instanceof FenceGateBlock)
                             || block.getBlock() instanceof AbstractBlockMinecoloniesConstructionTape
                             || block.getBlock() instanceof PressurePlateBlock
                             || block.getBlock() instanceof BlockDecorationController
                             || block.getBlock() instanceof SignBlock
                             || block.getBlock() instanceof AbstractBannerBlock
                             || !block.getBlock().properties.hasCollision;
                }
            }
            else if (!pathingOptions.canPassDanger() && PathfindingUtils.isDangerous(block))
            {
                return false;
            }
            else
            {
                if (PathfindingUtils.isLadder(block, pathingOptions))
                {
                    return true;
                }

                if (ShapeUtil.isEmpty(shape) || ShapeUtil.max(shape, Direction.Axis.Y) <= 0.1 && !PathfindingUtils.isLiquid((block)) && (block.getBlock() != Blocks.SNOW
                                                                                                                                           || block.getValue(SnowLayerBlock.LAYERS)
                                                                                                                                                == 1))
                {
                    final BlockPathTypes pathType = block.getBlockPathType(world, tempWorldPos.set(x, y, z), entity);
                    if (pathType == null || pathType.getDanger() == null)
                    {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }

    /**
     * Checks passability
     *
     * @param x
     * @param y
     * @param z
     * @param head
     * @param parent
     * @return
     */
    protected boolean isPassable(final int x, final int y, final int z, final boolean head, final MNode parent)
    {
        final BlockState state = cachedBlockLookup.getBlockState(x, y, z);
        final VoxelShape shape = state.getCollisionShape(world, tempWorldPos.set(x, y, z));
        if (ShapeUtil.isEmpty(shape) || ShapeUtil.max(shape, Direction.Axis.Y) <= 0.1)
        {
            return !head
                     || !(state.getBlock() instanceof WoolCarpetBlock || state.getBlock() instanceof FloatingCarpetBlock)
                     || PathfindingUtils.isLadder(state, pathingOptions);
        }
        return isPassable(state, x, y, z, parent, head);
    }

    /**
     * Handles not passable positions
     *
     * @param parent
     * @param x
     * @param y
     * @param z
     * @param target
     * @return
     */
    private int handleTargetNotPassable(@Nullable final MNode parent, final int x, final int y, final int z, @NotNull final BlockState target)
    {
        final boolean canJump = parent != null && !parent.isLadder() && !parent.isSwimming();
        //  Need to try jumping up one, if we can
        if (!canJump || SurfaceType.getSurfaceType(world, target, tempWorldPos.set(x, y, z), getPathingOptions()) != SurfaceType.WALKABLE)
        {
            return -100;
        }

        //  Check for headroom in the target space
        if (!isPassable(x, y + 2, z, false, parent))
        {
            final VoxelShape bb1 = cachedBlockLookup.getBlockState(x, y, z).getCollisionShape(world, tempWorldPos.set(x, y, z));
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(x, y + 2, z).getCollisionShape(world, tempWorldPos.set(x, y + 2, z));
            if ((y + 2 + ShapeUtil.getStartY(bb2, 1)) - (y + ShapeUtil.getEndY(bb1, 0)) < 2)
            {
                return -100;
            }
        }

        if (!canLeaveBlock(x, y + 2, z, parent, true))
        {
            return -100;
        }

        //  Check for jump room from the origin space
        if (!isPassable(parent.x, parent.y + 2, parent.z, false, parent))
        {
            final VoxelShape bb1 = cachedBlockLookup.getBlockState(x, y, z).getCollisionShape(world, tempWorldPos.set(x, y, z));
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(parent.x, parent.y + 2, parent.z).getCollisionShape(world, tempWorldPos.set(parent.x, parent.y + 2, parent.z));
            if ((parent.y + 2 + ShapeUtil.getStartY(bb2, 1)) - (y + ShapeUtil.getEndY(bb1, 0)) < 2)
            {
                return -100;
            }
        }


        final BlockState parentBelow = cachedBlockLookup.getBlockState(parent.x, parent.y - 1, parent.z);
        final VoxelShape parentBB = parentBelow.getCollisionShape(world, tempWorldPos.set(parent.x, parent.y - 1, parent.z));

        double parentY = ShapeUtil.max(parentBB, Direction.Axis.Y);
        double parentMaxY = parentY + parent.y - 1;
        final double targetMaxY = ShapeUtil.max(target.getCollisionShape(world, tempWorldPos.set(x, y, z)), Direction.Axis.Y) + y;
        if (targetMaxY - parentMaxY < MAX_JUMP_HEIGHT)
        {
            return y + 1;
        }
        if (target.is(BlockTags.STAIRS)
              && parentY - HALF_A_BLOCK < MAX_JUMP_HEIGHT
              && target.getValue(StairBlock.HALF) == Half.BOTTOM
              && BlockPosUtil.getXZFacing(parent.x, parent.z, x, z) == target.getValue(StairBlock.FACING))
        {
            return y + 1;
        }
        return -100;
    }

    /**
     * Handles not standing goto positions
     *
     * @param parent
     * @param x
     * @param y
     * @param z
     * @param below
     * @return
     */
    private int handleNotStanding(@Nullable final MNode parent, final int x, final int y, final int z, @NotNull final BlockState below)
    {
        final boolean isSwimming = parent != null && parent.isSwimming();

        if (!pathingOptions.canWalkUnderWater() && PathfindingUtils.isLiquid(below))
        {
            return handleInLiquid(x, y, z, below, isSwimming);
        }

        if (PathfindingUtils.isLadder(below, pathingOptions))
        {
            return y;
        }

        return checkDrop(parent, x, y, z, isSwimming);
    }

    /**
     * Checks dropping down
     *
     * @param parent
     * @param x
     * @param y
     * @param z
     * @param isSwimming
     * @return
     */
    private int checkDrop(@Nullable final MNode parent, final int x, final int y, final int z, final boolean isSwimming)
    {
        final boolean canDrop = parent != null && !parent.isLadder();
        //  Nothing to stand on
        if (!canDrop || ((parent.x != x || parent.z != z) && isPassable(parent.x, parent.y - 1, parent.z, false, parent)
                           &&
                           SurfaceType.getSurfaceType(world,
                             cachedBlockLookup.getBlockState(parent.x, parent.y - 1, parent.z),
                             tempWorldPos.set(parent.x, parent.y - 1, parent.z),
                             getPathingOptions())
                             == SurfaceType.DROPABLE))
        {
            return -100;
        }

        for (int i = 2; i <= (pathingOptions.canDrop ? 10 : 2); i++)
        {
            final BlockState below = cachedBlockLookup.getBlockState(x, y - i, z);
            if (SurfaceType.getSurfaceType(world, below, tempWorldPos.set(x, y - i, z), getPathingOptions()) == SurfaceType.WALKABLE)
            {
                //  Level path
                return y - i + 1;
            }
            else if (!below.isAir())
            {
                return -100;
            }
        }

        return -100;
    }

    /**
     * Handles goto position in liquid
     *
     * @param x
     * @param y
     * @param z
     * @param below
     * @param isSwimming
     * @return
     */
    private int handleInLiquid(final int x, final int y, final int z, @NotNull final BlockState below, final boolean isSwimming)
    {
        if (isSwimming)
        {
            //  Already swimming in something, or allowed to swim and this is water
            return y;
        }

        if (pathingOptions.canSwim() && PathfindingUtils.isWater(world, tempWorldPos.set(x, y - 1, z)))
        {
            //  This is water, and we are allowed to swim
            return y;
        }

        //  Not allowed to swim or this isn't water, and we're on dry land
        return -100;
    }

    /**
     * Check if we can leave the block at this pos.
     *
     * @param parent the parent pos (to check if we can leave)
     * @return true if so.
     */
    private boolean canLeaveBlock(final int x, final int y, final int z, final MNode parent, final boolean head)
    {
        int parentX = parent == null ? start.getX() : parent.x;
        int parentY = parent == null ? start.getY() : parent.y;
        int parentZ = parent == null ? start.getZ() : parent.z;
        if (head)
        {
            parentY++;
        }

        final int dY = y - parentY;

        final BlockState parentBlock = cachedBlockLookup.getBlockState(parentX, parentY, parentZ);
        if (parentBlock.getBlock() instanceof TrapDoorBlock || parentBlock.getBlock() instanceof PanelBlock)
        {
            if (!parentBlock.getValue(TrapDoorBlock.OPEN))
            {
                if (dY != 0)
                {
                    if (parentBlock.getBlock() instanceof TrapDoorBlock)
                    {
                        return true;
                    }
                    return (head && parentBlock.getValue(PanelBlock.HALF) == Half.TOP && dY < 0) || (!head && parentBlock.getValue(PanelBlock.HALF) == Half.BOTTOM
                                                                                                       && dY > 0);
                }
                return true;
            }
            if (x - parentX != 0 || z - parentZ != 0)
            {
                // Check if we can leave the current block, there might be a trapdoor or panel blocking us.
                final Direction direction = BlockPosUtil.getXZFacing(parentX, parentZ, x, z);
                final Direction facing = parentBlock.getValue(TrapDoorBlock.FACING);
                if (direction == facing.getOpposite())
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean calculateSwimming(final BlockState below, final BlockState state, final BlockState above, @Nullable final MNode node)
    {
        if (node != null)
        {
            return node.isSwimming();
        }

        return PathfindingUtils.isWater(cachedBlockLookup, null, below, null)
                 || PathfindingUtils.isWater(cachedBlockLookup, null, state, null)
                 || PathfindingUtils.isWater(cachedBlockLookup, null, above, null);
    }

    /**
     * Initializes debug tracking
     */
    public void initDebug()
    {
        if (!debugDrawEnabled)
        {
            debugDrawEnabled = true;
            debugNodesVisited = new HashSet<>();
            debugNodesVisitedLater = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath = new HashSet<>();
            debugNodesOrgPath = new HashSet<>();
            debugNodesExtra = new HashSet<>();
        }
    }

    /**
     * Handles debugging for a given node
     *
     * @param node
     */
    protected void handleDebugOptions(final MNode node)
    {
        if (debugDrawEnabled)
        {
            addNodeToDebug(node);

            if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() == DEBUG_VERBOSITY_FULL)
            {
                Log.getLogger().info(String.format("Examining node [%d,%d,%d] ; c=%f ; h=%f",
                    node.x, node.y, node.z, node.getCost(), node.getHeuristic()));
            }
        }
    }

    /**
     * Add extra nodes to debug view
     *
     * @param node
     */
    private void handleDebugExtraNode(final MNode node)
    {
        if (debugDrawEnabled)
        {
            debugNodesNotVisited.remove(node);
            debugNodesExtra.add(node);
        }
    }

    /**
     * Add original path nodes to debug view
     *
     * @param bestNode
     */
    private void handleDebugPathReach(final MNode bestNode)
    {
        if (debugDrawEnabled)
        {
            debugNodesOrgPath.add(bestNode);

            MNode currentNode = bestNode;
            while (currentNode.parent != null)
            {
                currentNode = currentNode.parent;
                debugNodesOrgPath.add(currentNode);
            }
        }
    }

    /**
     * Turns on debug printing.
     *
     * @param points the points to print.
     */
    private void doDebugPrinting(@NotNull final Node[] points)
    {
        if (debugDrawEnabled)
        {
            if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
            {
                Log.getLogger().info("Path found:");

                for (@NotNull final Node p : points)
                {
                    Log.getLogger().info(String.format("Step: [%d,%d,%d]", p.x, p.y, p.z));
                }

                Log.getLogger().info(String.format("Total Nodes Visited %d / %d", totalNodesVisited, totalNodesAdded));
            }
        }
    }

    /**
     * Adds a node to the debug view
     *
     * @param currentNode
     */
    private void addNodeToDebug(final MNode currentNode)
    {
        if (debugNodesOrgPath.contains(currentNode))
        {
            return;
        }

        debugNodesNotVisited.remove(currentNode);
        debugNodesVisited.add(currentNode);

        if (reachesDestination)
        {
            debugNodesVisited.remove(currentNode);
            debugNodesVisitedLater.add(currentNode);
        }
    }

    /**
     * Adds a path node to the debug view
     *
     * @param node
     */
    private void addPathNodeToDebug(final MNode node)
    {
        debugNodesVisited.remove(node);
        debugNodesPath.add(node);
    }

    /**
     * Sync the path to the client.
     */
    public void syncDebug(final List<ServerPlayer> debugWatchers)
    {
        if (debugDrawEnabled)
        {
            final SyncPathMessage message = new SyncPathMessage(debugNodesVisited,
              debugNodesNotVisited,
              debugNodesPath,
              debugNodesVisitedLater,
              debugNodesOrgPath,
              debugNodesExtra);

            for (final ServerPlayer player : debugWatchers)
            {
                Network.getNetwork().sendToPlayer(message, player);
            }
        }
    }

    @Override
    public PathResult getResult()
    {
        return result;
    }

    /**
     * Sets the pathing options
     *
     * @param pathingOptions the pathing options to set.
     */
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        this.pathingOptions.importFrom(pathingOptions);
    }

    @Override
    public PathingOptions getPathingOptions()
    {
        return pathingOptions;
    }

    @Override
    public Mob getEntity()
    {
        return entity;
    }

    @Override
    public Level getActualWorld()
    {
        return actualWorld;
    }

    @Override
    public BlockPos getStart()
    {
        return start;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " start:" + start + " entity:" + entity + " maxNodes:" + maxNodes + " totalNodesVisited:" + totalNodesVisited + " h-rebalances:" + (
            visitedLevel - 1) + " reaches:"
            + reachesDestination;
    }
}
