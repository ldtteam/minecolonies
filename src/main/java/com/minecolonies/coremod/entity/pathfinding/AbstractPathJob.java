package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Abstract class for Jobs that run in the multithreaded path finder.
 */
public abstract class AbstractPathJob implements Callable<Path>
{
    //  Debug Output
    protected static final int      DEBUG_VERBOSITY_NONE  = 0;
    protected static final int      DEBUG_VERBOSITY_BASIC = 1;
    protected static final int      DEBUG_VERBOSITY_FULL  = 2;
    protected static final Object   debugNodeMonitor      = new Object();
    private static final   int      SHIFT_SOUTH           = 1;
    private static final   int      SHIFT_WEST            = 2;
    private static final   int      SHIFT_NORTH           = 3;
    private static final   int      SHIFT_EAST            = 4;
    private static final   BlockPos BLOCKPOS_IDENTITY     = new BlockPos(0, 0, 0);
    private static final   BlockPos BLOCKPOS_UP           = new BlockPos(0, 1, 0);
    private static final   BlockPos BLOCKPOS_DOWN         = new BlockPos(0, -1, 0);
    private static final   BlockPos BLOCKPOS_NORTH        = new BlockPos(0, 0, -1);
    private static final   BlockPos BLOCKPOS_SOUTH        = new BlockPos(0, 0, 1);
    private static final   BlockPos BLOCKPOS_EAST         = new BlockPos(1, 0, 0);
    private static final   BlockPos BLOCKPOS_WEST         = new BlockPos(-1, 0, 0);
    private static final   int      MAX_Y                 = 256;
    private static final   int      MIN_Y                 = 0;

    /**
     * The maximum amount of nodes to Map, set in the config.
     */
    private static final int MAX_NODES_VISITED = Configurations.pathfinding.pathfindingMaxNodes;

    /**
     * Additional cost of jumping and dropping - base 1.
     */
    private static final double JUMP_DROP_COST = 2.5D;

    /**
     * Cost improvement of paths - base 1.
     */
    private static final double ON_PATH_COST = 0.5D;

    /**
     * Additional cost of swimming - base 1.
     */
    private static final double SWIM_COST = 10D;

    /**
     * Distance which is considered to be too close to a fence.
     */
    private static final double TOO_CLOSE_TO_FENCE = 0.1D;

    /**
     * Distance which is considered to be too far from a fence.
     */
    private static final double TOO_FAR_FROM_FENCE = 0.9D;

    /**
     * Shift x by this value to calculate the node key..
     */
    private static final int SHIFT_X_BY = 20;

    /**
     * Shift the y value by this to calculate the node key..
     */
    private static final int SHIFT_Y_BY = 12;

    @Nullable
    protected static Set<Node>          lastDebugNodesVisited;
    @Nullable
    protected static Set<Node>          lastDebugNodesNotVisited;
    @Nullable
    protected static Set<Node>          lastDebugNodesPath;
    @NotNull
    protected final  BlockPos           start;
    @NotNull
    protected final  IBlockAccess       world;
    protected final  PathResult         result;
    private final    int                maxRange;
    private final    Queue<Node>        nodesOpen                    = new PriorityQueue<>(500);
    private final    Map<Integer, Node> nodesVisited                 = new HashMap<>();
    //  Debug Rendering
    protected        boolean            debugDrawEnabled             = false;
    @Nullable
    protected        Set<Node>          debugNodesVisited            = null;
    @Nullable
    protected        Set<Node>          debugNodesNotVisited         = null;
    @Nullable
    protected        Set<Node>          debugNodesPath               = null;
    //  Job rules/configuration
    private          boolean            allowSwimming                = true;
    //  May be faster, but can produce strange results
    private          boolean            allowJumpPointSearchTypeWalk = false;
    private          int                totalNodesAdded              = 0;
    private          int                totalNodesVisited            = 0;

    /**
     * The entity this job belongs to.
     */
    private EntityLivingBase entity;

    /**
     * AbstractPathJob constructor.
     *
     * @param world the world within which to path.
     * @param start the start position from which to path from.
     * @param end   the end position to path to.
     * @param range maximum path range.
     * @param entity the entity.
     */
    public AbstractPathJob(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final EntityLivingBase entity)
    {
        this(world, start, end, range, new PathResult(), entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param end    the end position to path to
     * @param range  maximum path range.
     * @param result path result.
     * @param entity the entity.
     * @see AbstractPathJob#AbstractPathJob(World, BlockPos, BlockPos, int, EntityLivingBase)
     */
    public AbstractPathJob(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final PathResult result, final EntityLivingBase entity)
    {
        final int minX = Math.min(start.getX(), end.getX()) - (range / 2);
        final int minZ = Math.min(start.getZ(), end.getZ()) - (range / 2);
        final int maxX = Math.max(start.getX(), end.getX()) + (range / 2);
        final int maxZ = Math.max(start.getZ(), end.getZ()) + (range / 2);

        this.world = new ChunkCache(world, new BlockPos(minX, MIN_Y, minZ), new BlockPos(maxX, MAX_Y, maxZ), range);

        this.start = new BlockPos(start);
        this.maxRange = range;

        this.result = result;

        allowJumpPointSearchTypeWalk = false;

        if (Configurations.pathfinding.pathfindingDebugDraw)
        {
            debugDrawEnabled = true;
            debugNodesVisited = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath = new HashSet<>();
        }
        this.entity = entity;
    }

    private static boolean onLadderGoingUp(@NotNull final Node currentNode, @NotNull final BlockPos dPos)
    {
        return currentNode.isLadder() && (dPos.getY() >= 0 || dPos.getX() != 0 || dPos.getZ() != 0);
    }

    /**
     * Generates a good path starting location for the entity to path from, correcting for the following conditions.
     * - Being in water: pathfinding in water occurs along the surface; adjusts position to surface.
     * - Being in a fence space: finds correct adjacent position which is not a fence space, to prevent starting path.
     * from within the fence block.
     *
     * @param entity Entity for the pathfinding operation.
     * @return ChunkCoordinates for starting location.
     */
    public static BlockPos prepareStart(@NotNull final EntityLiving entity)
    {
        @NotNull final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor(entity.posX),
                                                                                    (int) entity.posY,
                                                                                    MathHelper.floor(entity.posZ));
        IBlockState bs = CompatibilityUtils.getWorldFromEntity(entity).getBlockState(pos);
        final Block b = bs.getBlock();

        if (entity.isInWater())
        {
            while (bs.getMaterial().isLiquid())
            {
                pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
                bs = CompatibilityUtils.getWorldFromEntity(entity).getBlockState(pos);
            }
        }
        else if (b instanceof BlockFence || b instanceof BlockWall || b instanceof AbstractBlockMinecoloniesDefault)
        {
            //Push away from fence
            final double dX = entity.posX - Math.floor(entity.posX);
            final double dZ = entity.posZ - Math.floor(entity.posZ);

            if (dX < TOO_CLOSE_TO_FENCE)
            {
                pos.setPos(pos.getX() - 1, pos.getY(), pos.getZ());
            }
            else if (dX > TOO_FAR_FROM_FENCE)
            {
                pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ());
            }

            if (dZ < TOO_CLOSE_TO_FENCE)
            {
                pos.setPos(pos.getX(), pos.getY(), pos.getZ() - 1);
            }
            else if (dZ > TOO_FAR_FROM_FENCE)
            {
                pos.setPos(pos.getX(), pos.getY(), pos.getZ() + 1);
            }
        }

        return pos.toImmutable();
    }

    /**
     * Sets the direction where the ladder is facing.
     *
     * @param world the world in.
     * @param pos   the position.
     * @param p     the path.
     */
    private static void setLadderFacing(@NotNull final IBlockAccess world, final BlockPos pos, @NotNull final PathPointExtended p)
    {
        if (world.getBlockState(pos).getBlock() instanceof BlockVine)
        {
            final int meta = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));

            if (((meta >>> SHIFT_SOUTH) & 1) != 0)
            {
                p.setLadderFacing(EnumFacing.SOUTH);
            }
            else if (((meta >>> SHIFT_WEST) & 1) != 0)
            {
                p.setLadderFacing(EnumFacing.WEST);
            }
            else if (((meta >>> SHIFT_NORTH) & 1) != 0)
            {
                p.setLadderFacing(EnumFacing.NORTH);
            }
            else if (((meta >>> SHIFT_EAST) & 1) != 0)
            {
                p.setLadderFacing(EnumFacing.EAST);
            }
        }
        else
        {
            p.setLadderFacing(world.getBlockState(pos).getValue(BlockLadder.FACING));
        }
    }

    /**
     * Checks if entity is on a ladder.
     *
     * @param node       the path node.
     * @param nextInPath the next path point.
     * @param pos        the position.
     * @return true if on a ladder.
     */
    private static boolean onALadder(@NotNull final Node node, @Nullable final Node nextInPath, @NotNull final BlockPos pos)
    {
        return nextInPath != null && node.isLadder()
                 &&
                 (nextInPath.pos.getX() == pos.getX() && nextInPath.pos.getZ() == pos.getZ());
    }

    /**
     * Generate a pseudo-unique key for identifying a given node by it's coordinates
     * Encodes the lowest 12 bits of x,z and all useful bits of y.
     * This creates unique keys for all blocks within a 4096x256x4096 cube, which is FAR
     * bigger volume than one should attempt to pathfind within
     * This version takes a BlockPos
     *
     * @param pos BlockPos to generate key from
     * @return key for node in map
     */
    private static int computeNodeKey(@NotNull final BlockPos pos)
    {
        return ((pos.getX() & 0xFFF) << SHIFT_X_BY)
                 | ((pos.getY() & 0xFF) << SHIFT_Y_BY)
                 | (pos.getZ() & 0xFFF);
    }

    /**
     * Compute the cost (immediate 'g' value) of moving from the parent space to the new space.
     *
     * @param dPos       The delta from the parent to the new space; assumes dx,dy,dz in range of [-1..1].
     * @param isSwimming true is the current node would require the citizen to swim.
     * @param onPath     checks if the node is on a path.
     * @return cost to move from the parent to the new position.
     */
    protected static double computeCost(@NotNull final BlockPos dPos, final boolean isSwimming, final boolean onPath)
    {
        double cost = 1D;

        if (dPos.getY() != 0 && (dPos.getX() != 0 || dPos.getZ() != 0))
        {
            //  Tax the cost for jumping, dropping (warning: also taxes stairs)
            cost *= JUMP_DROP_COST;
        }

        if (onPath)
        {
            cost *= ON_PATH_COST;
        }

        if (isSwimming)
        {
            cost *= SWIM_COST;
        }

        return cost;
    }

    private static boolean checkPreconditions(final Node node, final int newY)
    {
        if (nodeClosed(node))
        {
            //  Early out on previously visited and closed nodes
            return true;
        }

        return newY < 0;
    }

    private static boolean nodeClosed(@Nullable final Node node)
    {
        return node != null && node.isClosed();
    }

    private static boolean calculateSwimming(@NotNull final IBlockAccess world, @NotNull final BlockPos pos, @Nullable final Node node)
    {
        return (node == null) ? world.getBlockState(pos.down()).getMaterial().isLiquid() : node.isSwimming();
    }

    public PathResult getResult()
    {
        return result;
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
            // Log everything, so exceptions of the pathfinding-thread show in Log
            Log.getLogger().warn("Pathfinding Exception", e);
        }

        return null;
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    protected Path search()
    {
        Node bestNode = getAndSetupStartNode();

        double bestNodeResultScore = getNodeResultScore(bestNode);

        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            final Node currentNode = nodesOpen.poll();

            totalNodesVisited++;

            // Limiting max amount of nodes mapped
            if (totalNodesVisited > MAX_NODES_VISITED)
            {
                break;
            }
            currentNode.setCounterVisited(totalNodesVisited);

            handleDebugOptions(currentNode);

            if (isAtDestination(currentNode))
            {
                bestNode = currentNode;
                result.setPathReachesDestination(true);
                break;
            }

            //  If this is the closest node to our destination, treat it as our best node
            final double nodeResultScore = getNodeResultScore(currentNode);
            if (nodeResultScore > bestNodeResultScore)
            {
                bestNode = currentNode;
                bestNodeResultScore = nodeResultScore;
            }

            if (BlockPosUtil.getDistanceSquared2D(currentNode.pos, start) <= maxRange * maxRange)
            {
                walkCurrentNode(currentNode);
            }
        }

        @NotNull final Path path = finalizePath(bestNode);

        handleDebugDraw();

        return path;
    }

    private void handleDebugOptions(final Node currentNode)
    {
        if (debugDrawEnabled)
        {
            addNodeToDebug(currentNode);
        }

        currentNode.setClosed();

        if (Configurations.pathfinding.pathfindingDebugVerbosity == DEBUG_VERBOSITY_FULL)
        {
            Log.getLogger().info(String.format("Examining node [%d,%d,%d] ; g=%f ; f=%f",
              currentNode.pos.getX(), currentNode.pos.getY(), currentNode.pos.getZ(), currentNode.getCost(), currentNode.getScore()));
        }
    }

    private void addNodeToDebug(final Node currentNode)
    {
        debugNodesNotVisited.remove(currentNode);
        debugNodesVisited.add(currentNode);
    }

    private void addPathNodeToDebug(final Node node)
    {
        debugNodesVisited.remove(node);
        debugNodesPath.add(node);
    }

    private void walkCurrentNode(@NotNull final Node currentNode)
    {
        BlockPos dPos = BLOCKPOS_IDENTITY;
        if (currentNode.parent != null)
        {
            dPos = currentNode.pos.subtract(currentNode.parent.pos);
        }

        //  On a ladder, we can go 1 straight-up
        if (onLadderGoingUp(currentNode, dPos))
        {
            walk(currentNode, BLOCKPOS_UP);
        }

        //  We can also go down 1, if the lower block is a ladder
        if (onLadderGoingDown(currentNode, dPos))
        {
            walk(currentNode, BLOCKPOS_DOWN);
        }

        // N
        if (dPos.getZ() <= 0)
        {
            walk(currentNode, BLOCKPOS_NORTH);
        }

        // E
        if (dPos.getX() >= 0)
        {
            walk(currentNode, BLOCKPOS_EAST);
        }

        // S
        if (dPos.getZ() >= 0)
        {
            walk(currentNode, BLOCKPOS_SOUTH);
        }

        // W
        if (dPos.getX() <= 0)
        {
            walk(currentNode, BLOCKPOS_WEST);
        }
    }

    private boolean onLadderGoingDown(@NotNull final Node currentNode, @NotNull final BlockPos dPos)
    {
        return (dPos.getY() <= 0 || dPos.getX() != 0 || dPos.getZ() != 0) && isLadder(currentNode.pos.down());
    }

    private void handleDebugDraw()
    {
        if (debugDrawEnabled)
        {
            synchronized (debugNodeMonitor)
            {
                lastDebugNodesNotVisited = debugNodesNotVisited;
                lastDebugNodesVisited = debugNodesVisited;
                lastDebugNodesPath = debugNodesPath;
            }
        }
    }

    @NotNull
    private Node getAndSetupStartNode()
    {
        @NotNull final Node startNode = new Node(start,
                                                  computeHeuristic(start));

        if (isLadder(start))
        {
            startNode.setLadder();
        }
        else if (world.getBlockState(start).getMaterial().isLiquid())
        {
            startNode.setSwimming();
        }

        nodesOpen.offer(startNode);
        nodesVisited.put(computeNodeKey(start), startNode);

        ++totalNodesAdded;

        return startNode;
    }

    /**
     * Generate the path to the target node.
     *
     * @param targetNode the node to path to.
     * @return the path.
     */
    @NotNull
    private Path finalizePath(final Node targetNode)
    {
        //  Compute length of path, since we need to allocate an array.  This is cheaper/faster than building a List
        //  and converting it.  Yes, we have targetNode.steps, but I do not want to rely on that being accurate (I might
        //  fudge that value later on for cutoff purposes
        int pathLength = 0;
        @Nullable Node node = targetNode;
        while (node.parent != null)
        {
            ++pathLength;
            node = node.parent;
        }

        @NotNull final PathPoint[] points = new PathPoint[pathLength];

        @Nullable Node nextInPath = null;
        node = targetNode;
        while (node.parent != null)
        {
            if (debugDrawEnabled)
            {
                addPathNodeToDebug(node);
            }

            --pathLength;

            @NotNull final BlockPos pos = node.pos;

            if (node.isSwimming())
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                pos.add(BLOCKPOS_DOWN);
            }

            @NotNull final PathPointExtended p = new PathPointExtended(pos);

            //  Climbing on a ladder?
            if (nextInPath != null && onALadder(node, nextInPath, pos))
            {
                p.setOnLadder(true);
                if (nextInPath.pos.getY() > pos.getY())
                {
                    //  We only care about facing if going up
                    //In the case of BlockVines (Which does not have EnumFacing) we have to check the metadata of the vines... bitwise...
                    setLadderFacing(world, pos, p);
                }
            }
            else if (onALadder(node.parent, node.parent, pos))
            {
                p.setOnLadder(true);
            }

            points[pathLength] = p;

            nextInPath = node;
            node = node.parent;
        }

        doDebugPrinting(points);

        return new Path(points);
    }

    /**
     * Turns on debug printing.
     *
     * @param points the points to print.
     */
    private void doDebugPrinting(@NotNull final PathPoint[] points)
    {
        if (Configurations.pathfinding.pathfindingDebugVerbosity > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info("Path found:");

            for (@NotNull final PathPoint p : points)
            {
                Log.getLogger().info(String.format("Step: [%d,%d,%d]", p.x, p.y, p.z));
            }

            Log.getLogger().info(String.format("Total Nodes Visited %d / %d", totalNodesVisited, totalNodesAdded));
        }
    }

    /**
     * Compute the heuristic cost ('h' value) of a given position x,y,z.
     * <p>
     * Returning a value of 0 performs a breadth-first search.
     * Returning a value less than actual possible cost to goal guarantees shortest path, but at computational expense.
     * Returning a value exactly equal to the cost to the goal guarantees shortest path and least expense (but generally.
     * only works when path is straight and unblocked).
     * Returning a value greater than the actual cost to goal produces good, but not perfect paths, and is fast.
     * Returning a very high value (such that 'h' is very high relative to 'g') then only 'h' (the heuristic) matters
     * as the search will be a very fast greedy best-first-search, ignoring cost weighting and distance.
     *
     * @param pos Position to compute heuristic from.
     * @return the heuristic.
     */
    protected abstract double computeHeuristic(BlockPos pos);

    /**
     * Return true if the given node is a viable final destination, and the path should generate to here.
     *
     * @param n Node to test.
     * @return true if the node is a viable destination.
     */
    protected abstract boolean isAtDestination(Node n);

    /**
     * Compute a 'result score' for the Node; if no destination is determined, the node that had the highest
     * 'result' score is used.
     *
     * @param n Node to test.
     * @return score for the node.
     */
    protected abstract double getNodeResultScore(Node n);

    /**
     * "Walk" from the parent in the direction specified by the delta, determining the new x,y,z position for such a
     * move and adding or updating a node, as appropriate.
     *
     * @param parent Node being walked from.
     * @param dPos   Delta from parent, expected in range of [-1..1].
     * @return true if a node was added or updated when attempting to move in the given direction.
     */
    protected final boolean walk(@NotNull final Node parent, @NotNull final BlockPos dPos)
    {
        BlockPos pos = parent.pos.add(dPos);

        final Block block = world.getBlockState(parent.pos).getBlock();
        if (block == Blocks.FARMLAND || block == Blocks.GRASS_PATH)
        {
            pos = pos.up();
        }

        //  Cheap test to perform before doing a 'y' test
        //  Has this node been visited?
        int nodeKey = computeNodeKey(pos);
        Node node = nodesVisited.get(nodeKey);

        //  Can we traverse into this node?  Fix the y up
        final int newY = getGroundHeight(parent, pos);

        if (checkPreconditions(node, newY))
        {
            return false;
        }

        BlockPos yFix = BlockPos.ORIGIN;

        if (pos.getY() != newY)
        {
            yFix = dPos.add(0, newY - pos.getY(), 0);

            //  Has this node been visited?
            pos = new BlockPos(pos.getX(), newY, pos.getZ());
            nodeKey = computeNodeKey(pos);
            node = nodesVisited.get(nodeKey);
            if (nodeClosed(node))
            {
                //  Early out on previously visited and closed nodes
                return false;
            }
        }

        final boolean isSwimming = calculateSwimming(world, pos, node);
        final boolean onRoad = BlockUtils.isPathBlock(world.getBlockState(pos.down()).getBlock());
        //  Cost may have changed due to a jump up or drop
        final double stepCost = computeCost(dPos.add(yFix), isSwimming, onRoad);
        final double heuristic = computeHeuristic(pos);
        final double cost = parent.getCost() + stepCost;
        final double score = cost + heuristic;

        if (node == null)
        {
            node = createNode(parent, pos, nodeKey, isSwimming, heuristic, cost, score);
        }
        else if (updateCurrentNode(parent, node, heuristic, cost, score))
        {

            return false;
        }

        nodesOpen.offer(node);

        //  Jump Point Search-ish optimization:
        // If this node was a (heuristic-based) improvement on our parent,
        // lets go another step in the same direction...
        performJumpPointSearch(parent, dPos, node);

        return true;
    }

    private void performJumpPointSearch(@NotNull final Node parent, @NotNull final BlockPos dPos, @NotNull final Node node)
    {
        if (allowJumpPointSearchTypeWalk && node.getHeuristic() <= parent.getHeuristic())
        {
            walk(node, dPos);
        }
    }

    @NotNull
    private Node createNode(
                             final Node parent, @NotNull final BlockPos pos, final int nodeKey,
                             final boolean isSwimming, final double heuristic, final double cost, final double score)
    {
        final Node node;
        node = new Node(parent, pos, cost, heuristic, score);
        nodesVisited.put(nodeKey, node);
        if (debugDrawEnabled)
        {
            debugNodesNotVisited.add(node);
        }

        if (isLadder(pos))
        {
            node.setLadder();
        }
        else if (isSwimming)
        {
            node.setSwimming();
        }

        totalNodesAdded++;
        node.setCounterAdded(totalNodesAdded);
        return node;
    }

    private boolean updateCurrentNode(@NotNull final Node parent, @NotNull final Node node, final double heuristic, final double cost, final double score)
    {
        //  This node already exists
        if (score >= node.getScore())
        {
            return true;
        }

        if (!nodesOpen.remove(node))
        {
            return true;
        }

        node.parent = parent;
        node.setSteps(parent.getSteps() + 1);
        node.setCost(cost);
        node.setHeuristic(heuristic);
        node.setScore(score);
        return false;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y.
     *
     * @param parent parent node.
     * @param pos    coordinate of block.
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop.
     */
    protected int getGroundHeight(final Node parent, @NotNull final BlockPos pos)
    {
        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (checkHeadBlock(parent, pos))
        {
            return -1;
        }

        //  Now check the block we want to move to
        final IBlockState target = world.getBlockState(pos);
        if (!isPassable(target))
        {
            return handleTargeNotPassable(parent, pos, target);
        }

        //  Do we have something to stand on in the target space?
        final IBlockState below = world.getBlockState(pos.down());
        final SurfaceType walkability = isWalkableSurface(below, pos);
        if (walkability == SurfaceType.WALKABLE)
        {
            //  Level path
            return pos.getY();
        }
        else if (walkability == SurfaceType.NOT_PASSABLE)
        {
            return -1;
        }

        return handleNotStanding(parent, pos, below);
    }

    private int handleNotStanding(@Nullable final Node parent, @NotNull final BlockPos pos, @NotNull final IBlockState below)
    {
        final boolean isSwimming = parent != null && parent.isSwimming();

        if (below.getMaterial().isLiquid())
        {
            return handleInLiquid(pos, below, isSwimming);
        }

        if (isLadder(below.getBlock(), pos.down()))
        {
            return pos.getY();
        }

        return checkDrop(parent, pos, isSwimming);
    }

    private int checkDrop(@Nullable final Node parent, @NotNull final BlockPos pos, final boolean isSwimming)
    {
        final boolean canDrop = parent != null && !parent.isLadder();
        //  Nothing to stand on
        if (!canDrop || isSwimming)
        {
            return -1;
        }

        for (int i = 2; i <= 4; i++)
        {
            final IBlockState below = world.getBlockState(pos.down(i));
            if (isWalkableSurface(below, pos) == SurfaceType.WALKABLE)
            {
                //  Level path
                return pos.getY() - i + 1;
            }
            else if (below.getMaterial() != Material.AIR)
            {
                return -1;
            }
        }

        return -1;
    }

    private int handleInLiquid(@NotNull final BlockPos pos, @NotNull final IBlockState below, final boolean isSwimming)
    {
        if (isSwimming)
        {
            //  Already swimming in something, or allowed to swim and this is water
            return pos.getY();
        }

        if (allowSwimming && below.getMaterial() == Material.WATER)
        {
            //  This is water, and we are allowed to swim
            return pos.getY();
        }

        //  Not allowed to swim or this isn't water, and we're on dry land
        return -1;
    }

    private int handleTargeNotPassable(@Nullable final Node parent, @NotNull final BlockPos pos, @NotNull final IBlockState target)
    {
        final boolean canJump = parent != null && !parent.isLadder() && !parent.isSwimming();
        //  Need to try jumping up one, if we can
        if (!canJump || isWalkableSurface(target, pos) != SurfaceType.WALKABLE)
        {
            return -1;
        }

        //  Check for headroom in the target space
        if (!isPassable(pos.up(2)))
        {
            return -1;
        }

        //  Check for jump room from the origin space
        if (!isPassable(parent.pos.up(2)))
        {
            return -1;
        }

        final IBlockState below = world.getBlockState(parent.pos.down());
        final AxisAlignedBB bb = below.getCollisionBoundingBox(world, pos.down());
        if (bb != null && bb.maxY < 1)
        {
            return -1;
        }

        //  Jump up one
        return pos.getY() + 1;
    }

    private boolean checkHeadBlock(@Nullable final Node parent, @NotNull final BlockPos pos)
    {
        if (!isPassable(pos.up()))
        {
            return true;
        }

        if (parent != null)
        {
            final IBlockState hereState = world.getBlockState(parent.pos.down());
            return hereState.getMaterial().isLiquid() && !isPassable(pos);
        }
        return false;
    }

    /**
     * Is the space passable.
     *
     * @param block the block we are checking.
     * @return true if the block does not block movement.
     */
    protected boolean isPassable(@NotNull final IBlockState block)
    {
        if (block.getMaterial() != Material.AIR)
        {
            if (block.getMaterial().blocksMovement())
            {
                return block.getBlock() instanceof BlockDoor
                         || block.getBlock() instanceof BlockFenceGate
                         || block.getBlock() instanceof AbstractBlockMinecoloniesConstructionTape
                         || block.getBlock() instanceof BlockPressurePlate;
            }
            else
            {
                return !block.getMaterial().isLiquid();
            }
        }

        return true;
    }

    protected boolean isPassable(final BlockPos pos)
    {
        final IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isPassable(world, pos))
        {
            return true;
        }
        return isPassable(state);
    }

    /**
     * Is the block solid and can be stood upon.
     *
     * @param blockState Block to check.
     * @param pos the position.
     * @return true if the block at that location can be walked on.
     */
    @NotNull
    protected SurfaceType isWalkableSurface(@NotNull final IBlockState blockState, final BlockPos pos)
    {
        final Block block = blockState.getBlock();
        if (block instanceof BlockFence
              || block instanceof BlockFenceGate
              || block instanceof BlockWall
              || block instanceof AbstractBlockMinecoloniesDefault
              || block instanceof AbstractBlockBarrel
              || (blockState.getCollisionBoundingBox(world, pos) != null
                   && blockState.getCollisionBoundingBox(world, pos).maxY > 1.0))
        {
            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof AbstractBlockMinecoloniesConstructionTape)
        {
            return SurfaceType.DROPABLE;
        }

        if (blockState.getMaterial().isSolid())
        {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;
    }

    /**
     * Is the block a ladder.
     *
     * @param block block to check.
     * @param pos   location of the block.
     * @return true if the block is a ladder.
     */
    protected boolean isLadder(@NotNull final Block block, final BlockPos pos)
    {
        return block.isLadder(this.world.getBlockState(pos), world, pos, entity);
    }

    protected boolean isLadder(final BlockPos pos)
    {
        return isLadder(world.getBlockState(pos).getBlock(), pos);
    }

    /**
     * Getter for the allowSwimming.
     *
     * @return true if is allowed.
     */
    protected boolean isAllowedSwimming()
    {
        return allowSwimming;
    }

    /**
     * Setter for the allowSwimming.
     *
     * @param allowSwimming the value to set.
     */
    protected void setAllowedSwimming(final boolean allowSwimming)
    {
        this.allowSwimming = allowSwimming;
    }

    /**
     * Check if we can walk on a surface, drop into, or neither.
     */
    private enum SurfaceType
    {
        WALKABLE,
        DROPABLE,
        NOT_PASSABLE
    }
}
