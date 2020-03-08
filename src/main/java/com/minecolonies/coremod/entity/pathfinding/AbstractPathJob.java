package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Callable;

import static com.minecolonies.api.util.constant.PathingConstants.*;

/**
 * Abstract class for Jobs that run in the multithreaded path finder.
 */
public abstract class AbstractPathJob implements Callable<Path>
{
    @Nullable
    protected static Set<Node>          lastDebugNodesVisited;
    @Nullable
    protected static Set<Node>          lastDebugNodesNotVisited;
    @Nullable
    protected static Set<Node>          lastDebugNodesPath;
    @NotNull
    protected final  BlockPos           start;
    @NotNull
    protected final  IWorldReader       world;
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
     * Are there hard xz restrictions.
     */
    private boolean xzRestricted = false;

    /**
     * The restriction parameters
     */
    private int maxX;
    private int minX;
    private int maxZ;
    private int minZ;

    /**
     * The entity this job belongs to.
     */
    private WeakReference<LivingEntity> entity;

    /**
     * AbstractPathJob constructor.
     *
     * @param world the world within which to path.
     * @param start the start position from which to path from.
     * @param end   the end position to path to.
     * @param range maximum path range.
     * @param entity the entity.
     */
    public AbstractPathJob(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntity entity)
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
     * @see AbstractPathJob#AbstractPathJob(World, BlockPos, BlockPos, int, LivingEntity)
     */
    public AbstractPathJob(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final PathResult result, final LivingEntity entity)
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

        if (MineColonies.getConfig().getCommon().pathfindingDebugDraw.get())
        {
            debugDrawEnabled = true;
            debugNodesVisited = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath = new HashSet<>();
        }
        this.entity = new WeakReference<>(entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param startRestriction  start of restricted area.
     * @param endRestriction  end of restricted area.
     * @param result path result.
     * @param entity the entity.
     * @see AbstractPathJob#AbstractPathJob(World, BlockPos, BlockPos, int, LivingEntity)
     */
    public AbstractPathJob(final World world, final BlockPos startRestriction, final BlockPos endRestriction, final PathResult result, final LivingEntity entity)
    {
        this.minX = Math.min(startRestriction.getX(), endRestriction.getX());
        this.minZ = Math.min(startRestriction.getZ(), endRestriction.getZ());
        this.maxX = Math.max(startRestriction.getX(), endRestriction.getX());
        this.maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ());

        xzRestricted = true;


        final int range = (int)Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxZ - minZ, 2)) * 2;

        this.world = new ChunkCache(world, new BlockPos(minX, MIN_Y, minZ), new BlockPos(maxX, MAX_Y, maxZ), range);

        this.start = new BlockPos((minX + maxX) / 2, (startRestriction.getY() + endRestriction.getY()) / 2, (minZ + maxZ) / 2);
        this.maxRange = range;

        this.result = result;

        allowJumpPointSearchTypeWalk = false;

        if (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().pathfindingDebugDraw.get())
        {
            debugDrawEnabled = true;
            debugNodesVisited = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath = new HashSet<>();
        }
        this.entity = new WeakReference<>(entity);
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
    public static BlockPos prepareStart(@NotNull final LivingEntity entity)
    {
        @NotNull final BlockPos.Mutable pos = new BlockPos.Mutable(MathHelper.floor(entity.posX),
          (int) entity.posY,
          MathHelper.floor(entity.posZ));
        BlockState bs = CompatibilityUtils.getWorldFromEntity(entity).getBlockState(pos);
        final Block b = bs.getBlock();

        if (entity.isInWater())
        {
            while (bs.getMaterial().isLiquid())
            {
                pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
                bs = CompatibilityUtils.getWorldFromEntity(entity).getBlockState(pos);
            }
        }
        else if (b instanceof FenceBlock || b instanceof WallBlock || b instanceof AbstractBlockMinecoloniesDefault)
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
     *  @param world the world in.
     * @param pos   the position.
     * @param p     the path.
     */
    private static void setLadderFacing(@NotNull final IWorldReader world, final BlockPos pos, @NotNull final PathPointExtended p)
    {
        final BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof VineBlock)
        {
            if (state.get(VineBlock.SOUTH))
            {
                p.setLadderFacing(Direction.SOUTH);
            }
            else if (state.get(VineBlock.WEST))
            {
                p.setLadderFacing(Direction.WEST);
            }
            else if (state.get(VineBlock.NORTH))
            {
                p.setLadderFacing(Direction.NORTH);
            }
            else if (state.get(VineBlock.EAST))
            {
                p.setLadderFacing(Direction.EAST);
            }
        }
        else if (state.getBlock() instanceof ScaffoldingBlock)
        {
            p.setLadderFacing(Direction.UP);
        }
        else
        {
            p.setLadderFacing(state.get(LadderBlock.FACING));
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
    protected double computeCost(@NotNull final BlockPos dPos, final boolean isSwimming, final boolean onPath, final BlockPos blockPos)
    {
        double cost = 1D;

        if (dPos.getY() != 0 && (dPos.getX() != 0 || dPos.getZ() != 0) && !(Math.abs(dPos.getY()) <= 1 && world.getBlockState(blockPos).getBlock() instanceof StairsBlock))
        {
            //  Tax the cost for jumping, dropping
            cost *= JUMP_DROP_COST * Math.abs(dPos.getY());
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

    private static boolean calculateSwimming(@NotNull final IWorldReader world, @NotNull final BlockPos pos, @Nullable final Node node)
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

        double bestNodeResultScore = Double.MIN_VALUE;

        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            final Node currentNode = nodesOpen.poll();

            totalNodesVisited++;

            // Limiting max amount of nodes mapped
            if (totalNodesVisited > MineColonies.getConfig().getCommon().pathfindingMaxNodes.get() || totalNodesVisited > maxRange * maxRange)
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

            if (!xzRestricted || (currentNode.pos.getX() >= minX && currentNode.pos.getX() <= maxX && currentNode.pos.getZ() >= minZ && currentNode.pos.getZ() <= maxZ))
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

        if (MineColonies.getConfig().getCommon().pathfindingDebugVerbosity.get() == DEBUG_VERBOSITY_FULL)
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
                    //In the case of BlockVines (Which does not have Direction) we have to check the metadata of the vines... bitwise...
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

        return new Path(Arrays.asList(points), targetNode.pos, false);
    }

    /**
     * Turns on debug printing.
     *
     * @param points the points to print.
     */
    private void doDebugPrinting(@NotNull final PathPoint[] points)
    {
        if (MineColonies.getConfig().getCommon().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
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

        BlockPos yFix = BlockPos.ZERO;

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
        final boolean onRoad = WorkerUtil.isPathBlock(world.getBlockState(pos.down()).getBlock());
        //  Cost may have changed due to a jump up or drop
        final double stepCost = computeCost(dPos.add(yFix), isSwimming, onRoad, pos);
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
        final BlockState target = world.getBlockState(pos);
        if (!isPassable(target))
        {
            return handleTargeNotPassable(parent, pos, target);
        }

        //  Do we have something to stand on in the target space?
        final BlockState below = world.getBlockState(pos.down());
        final SurfaceType walkability = isWalkableSurface(below, pos);
        if (walkability == SurfaceType.WALKABLE)
        {
            final VoxelShape tbb = below.getCollisionShape(world, pos.down());
            if (tbb.getEnd(Direction.Axis.Y) < 0.5)
            {
                return pos.getY() - 1;
            }
            //  Level path
            return pos.getY();
        }
        else if (walkability == SurfaceType.NOT_PASSABLE)
        {
            return -1;
        }

        return handleNotStanding(parent, pos, below);
    }

    private int handleNotStanding(@Nullable final Node parent, @NotNull final BlockPos pos, @NotNull final BlockState below)
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

        for (int i = 2; i <= 10; i++)
        {
            final BlockState below = world.getBlockState(pos.down(i));
            if (isWalkableSurface(below, pos) == SurfaceType.WALKABLE && i <= 4 || below.getMaterial().isLiquid())
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

    private int handleInLiquid(@NotNull final BlockPos pos, @NotNull final BlockState below, final boolean isSwimming)
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

    private int handleTargeNotPassable(@Nullable final Node parent, @NotNull final BlockPos pos, @NotNull final BlockState target)
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

        final BlockState below = world.getBlockState(parent.pos.down());
        final VoxelShape bb = below.getCollisionShape(world, parent.pos.down());
        if (bb.getEnd(Direction.Axis.Y) < 1)
        {
            double dif = bb.getEnd(Direction.Axis.Y);
            final double parentY = target.getCollisionShape(world, pos).getEnd(Direction.Axis.Y);
            dif = dif + 1 - parentY;
            if (dif < MAX_JUMP_HEIGHT)
            {
                return pos.getY() + 1;
            }
            if (target.getBlock() instanceof StairsBlock
                  && dif - HALF_A_BLOCK < MAX_JUMP_HEIGHT
                  && target.get(StairsBlock.HALF) == Half.BOTTOM
                  && BlockPosUtil.getXZFacing(parent.pos, pos) == target.get(StairsBlock.FACING))
            {
                return pos.getY() + 1;
            }
            return -1;
        }

        final VoxelShape tbb = target.getCollisionShape(world, parent.pos);
        if (tbb.getEnd(Direction.Axis.Y) < 0.5)
        {
            return pos.getY();
        }
        //  Jump up one
        return pos.getY() + 1;
    }

    private boolean checkHeadBlock(@Nullable final Node parent, @NotNull final BlockPos pos)
    {
        BlockPos localPos = pos;
        final VoxelShape bb = world.getBlockState(localPos).getCollisionShape(world, localPos);
        if (bb.getEnd(Direction.Axis.Y) < 1)
        {
            localPos = pos.up();
        }

        if (!isPassable(pos.up()))
        {
            return true;
        }

        if (parent != null)
        {
            final BlockState hereState = world.getBlockState(localPos.down());
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
    protected boolean isPassable(@NotNull final BlockState block)
    {
        if (block.getMaterial() != Material.AIR)
        {
            if (block.getMaterial().blocksMovement())
            {
                return block.getBlock() instanceof DoorBlock
                         || block.getBlock() instanceof FenceGateBlock
                         || block.getBlock() instanceof AbstractBlockMinecoloniesConstructionTape
                         || block.getBlock() instanceof PressurePlateBlock;
            }
            else
            {
                return !block.getMaterial().isLiquid() &&  ( block.getBlock() != Blocks.SNOW || block.get(SnowBlock.LAYERS) == 1);
            }
        }

        return true;
    }

    protected boolean isPassable(final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if (!state.getMaterial().blocksMovement())
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
    protected SurfaceType isWalkableSurface(@NotNull final BlockState blockState, final BlockPos pos)
    {
        final Block block = blockState.getBlock();
        if (block instanceof FenceBlock
              || block instanceof FenceGateBlock
              || block instanceof WallBlock
              || block instanceof AbstractBlockMinecoloniesDefault
              || block instanceof AbstractBlockBarrel
              || (blockState.getShape(world, pos).getEnd(Direction.Axis.Y) > 1.0))
        {
            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof AbstractBlockMinecoloniesConstructionTape)
        {
            return SurfaceType.DROPABLE;
        }

        if (blockState.getMaterial().isSolid() || ( blockState.getBlock() == Blocks.SNOW && blockState.get(SnowBlock.LAYERS) > 1))
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
        return block.isLadder(this.world.getBlockState(pos), world, pos, entity.get());
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
