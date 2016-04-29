package com.minecolonies.entity.pathfinding;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.Log;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.Callable;

public abstract class PathJob implements Callable<PathEntity>
{
    protected final BlockPos start;
    protected final int      maxRange;

    protected final IBlockAccess world;

    //  Job rules/configuration
    protected boolean allowSwimming                = true;
    protected boolean allowJumpPointSearchTypeWalk = false; //  May be faster, but can produce strange results

    protected final Queue<Node>        nodesOpen    = new PriorityQueue<>(500);
    protected final Map<Integer, Node> nodesVisited = new HashMap<>();

    protected final PathResult         result;

    //  Debug Output
    protected static final int DEBUG_VERBOSITY_NONE  = 0;
    protected static final int DEBUG_VERBOSITY_BASIC = 1;
    protected static final int DEBUG_VERBOSITY_FULL  = 2;
    protected              int totalNodesAdded       = 0;
    protected              int totalNodesVisited     = 0;

    //  Debug Rendering
    protected boolean   debugDrawEnabled     = false;
    protected int       debugSleepMs         = 0;
    protected Set<Node> debugNodesVisited    = null;
    protected Set<Node> debugNodesNotVisited = null;
    protected Set<Node> debugNodesPath       = null;

    static public Object debugNodeMonitor = new Object();
    static public Set<Node> lastDebugNodesVisited;
    static public Set<Node> lastDebugNodesNotVisited;
    static public Set<Node> lastDebugNodesPath;

    /**
     * PathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param end the end position to path to
     * @param range maximum path range
     */
    public PathJob(World world, BlockPos start, BlockPos end, int range)
    {
        this(world, start, end, range, new PathResult());
    }

    public PathJob(World world, BlockPos start, BlockPos end, int range, PathResult result)
    {
        int minX = Math.min(start.getX(), end.getX());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxZ = Math.max(start.getZ(), end.getZ());

        this.world = new ChunkCache(world, new BlockPos(minX, 0, minZ),new BlockPos(maxX, 256, maxZ), range);

        this.start = new BlockPos(start);
        this.maxRange = range;

        this.result = result;

        allowJumpPointSearchTypeWalk = false;

        if (Configurations.pathfindingDebugDraw)
        {
            debugDrawEnabled = true;
            debugSleepMs = 0;
            debugNodesVisited = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath       = new HashSet<>();
        }
    }

    public PathResult getResult() { return result; }

    /**
     * Callable method for initiating asynchronous task
     * @return path to follow or null
     */
    @Override
    public final PathEntity call()
    {
        try
        {
            return search();
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        return null;
    }

    /**
     * Perform the search
     *
     * @return PathEntity of a path to the given location, a best-effort, or null
     */
    protected PathEntity search()
    {
        Node startNode = new Node(start,
                computeHeuristic(start));

        if (isLadder(start))
        {
            startNode.isLadder = true;
        }
        else if (world.getBlockState(start).getBlock().getMaterial().isLiquid())
        {
            startNode.isSwimming = true;
        }

        nodesOpen.offer(startNode);
        nodesVisited.put(computeNodeKey(start), startNode);

        ++totalNodesAdded;

        Node bestNode = startNode;
        double bestNodeResultScore = getNodeResultScore(bestNode);

        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            Node currentNode = nodesOpen.poll();
            currentNode.counterVisited = ++totalNodesVisited;
            if (debugDrawEnabled)
            {
                debugNodesNotVisited.remove(currentNode);
                debugNodesVisited.add(currentNode);
            }

            currentNode.closed = true;

            if (Configurations.pathfindingDebugVerbosity == DEBUG_VERBOSITY_FULL)
            {
                Log.logger.info(String.format("Examining node [%d,%d,%d] ; g=%f ; f=%f", currentNode.pos.getX(), currentNode.pos.getY(), currentNode.pos.getZ(), currentNode.cost, currentNode.score));
            }

            if (isAtDestination(currentNode))
            {
                bestNode = currentNode;
                result.setPathReachesDestination(true);
                break;
            }

            //  If this is the closest node to our destination, treat it as our best node
            double nodeResultScore = getNodeResultScore(currentNode);
            if (nodeResultScore > bestNodeResultScore)
            {
                bestNode = currentNode;
                bestNodeResultScore = nodeResultScore;
            }

            if (currentNode.steps <= maxRange)
            {
                BlockPos dPos = new BlockPos(0, 0, 0);
                if (currentNode.parent != null)
                {
                	dPos = currentNode.pos.subtract(currentNode.parent.pos);
                }

                if (currentNode.isLadder)
                {
                    //  On a ladder, we can go 1 straight-up
                    if (dPos.getY() >= 0 || dPos.getX() != 0 || dPos.getZ() != 0)
                    {
                        walk(currentNode, new BlockPos(0, 1, 0));
                    }
                }

                //  We can also go down 1, if the lower block is a ladder
                if (dPos.getY() <= 0 || dPos.getX() != 0 || dPos.getZ() != 0)
                {
                    if (isLadder(currentNode.pos.down()))
                    {
                        walk(currentNode, new BlockPos(0, -1, 0));
                    }
                }

                if (dPos.getZ() <= 0)    walk(currentNode, new BlockPos(0, 0, -1));    //  N
                if (dPos.getX() >= 0)    walk(currentNode, new BlockPos(1, 0, 0));     //  E
                if (dPos.getZ() >= 0)    walk(currentNode, new BlockPos(0, 0, 1));     //  S
                if (dPos.getX() <= 0)    walk(currentNode, new BlockPos(-1, 0, 0));    //  W
            }

            if (debugDrawEnabled && debugSleepMs != 0)
            {
                synchronized (debugNodeMonitor)
                {
                    lastDebugNodesNotVisited = new HashSet<>(debugNodesNotVisited);
                    lastDebugNodesVisited = new HashSet<>(debugNodesVisited);
                    lastDebugNodesPath = null;
                }

                if (debugSleepMs != 0)
                {
                    try { Thread.sleep(debugSleepMs); }
                    catch (InterruptedException ex) { return null;}
                }
            }
        }

        PathEntity path = finalizePath(bestNode);

        if (debugDrawEnabled)
        {
            synchronized (debugNodeMonitor)
            {
                lastDebugNodesNotVisited = debugNodesNotVisited;
                lastDebugNodesVisited = debugNodesVisited;
                lastDebugNodesPath = debugNodesPath;
            }
        }

        return path;
    }

    /**
     * Generates a good path starting location for the entity to path from, correcting for the following conditions:
     * - Being in water: pathfinding in water occurs along the surface; adjusts position to surface
     * - Being in a fence space: finds correct adjacent position which is not a fence space, to prevent starting path
     *   from within the fence block
     *
     * @param entity Entity for the pathfinding operation
     * @return ChunkCoordinates for starting location
     */
    public static BlockPos prepareStart(EntityLiving entity)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor_double(entity.posX),
                                                                    (int) entity.posY,
                                                                    MathHelper.floor_double(entity.posZ));
        Block b = entity.worldObj.getBlockState(pos).getBlock();

        if (entity.isInWater())
        {
            while (b.getMaterial().isLiquid())
            {
                pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
                b = entity.worldObj.getBlockState(pos).getBlock();
            }
        }
//        else if (y > 0 && world.getBlock(x, y - 1, z).getMaterial() == Material.air)
//        {
//            while (y > 0 && world.getBlock(x, y - 1, z).getMaterial() == Material.air)
//            {
//                --y;
//            }
//        }
        else if (b instanceof BlockFence || b instanceof BlockWall)
        {
            //  Push away from fence
            double dX = entity.posX - Math.floor(entity.posX);
            double dZ = entity.posZ - Math.floor(entity.posZ);

            if (dX < 0.1)       pos.set(pos.getX() - 1, pos.getY(), pos.getZ());
            else if (dX > 0.9)  pos.set(pos.getX() + 1, pos.getY(), pos.getZ());

            if (dZ < 0.1)       pos.set(pos.getX(), pos.getY(), pos.getZ() - 1);
            else if (dZ > 0.9)  pos.set(pos.getX(), pos.getY(), pos.getZ() + 1);
        }

        return pos.getImmutable();
    }

    /**
     * Generate the path to the target node
     *
     * @param targetNode
     * @return
     */
    PathEntity finalizePath(Node targetNode)
    {
        //  Compute length of path, since we need to allocate an array.  This is cheaper/faster than building a List
        //  and converting it.  Yes, we have targetNode.steps, but I do not want to rely on that being accurate (I might
        //  fudge that value later on for cutoff purposes
        int pathLength = 0;
        Node node = targetNode;
        while (node.parent != null)
        {
            ++pathLength;
            node = node.parent;
        }

        PathPoint points[] = new PathPoint[pathLength];

        Node nextInPath = null;
        node = targetNode;
        while (node.parent != null)
        {
            if (debugDrawEnabled)
            {
                debugNodesVisited.remove(node);
                debugNodesPath.add(node);
            }

            --pathLength;

            BlockPos pos = node.pos;

            if (node.isSwimming)
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                pos.add(0, -1, 0);
            }

            PathPointExtended p = new PathPointExtended(pos);

            //  Climbing on a ladder?
            if (nextInPath != null && node.isLadder &&
                    (nextInPath.pos.getX() == pos.getX() && nextInPath.pos.getZ() == pos.getZ()))
            {
                p.isOnLadder = true;
                if (nextInPath.pos.getY() > pos.getY())
                {
                    //  We only care about facing if going up
                    p.ladderFacing = world.getBlockState(pos).getValue(BlockLadder.FACING);
                }
            }
            else if (node.parent != null && node.parent.isLadder &&
                    (node.parent.pos.getX() == pos.getX() && node.parent.pos.getZ() == pos.getZ()))
            {
                p.isOnLadder = true;
            }

            points[pathLength] = p;

            nextInPath = node;
            node = node.parent;
        }

        if (Configurations.pathfindingDebugVerbosity > DEBUG_VERBOSITY_NONE)
        {
            Log.logger.info("Path found:");

            for (PathPoint p : points)
            {
                Log.logger.info(String.format("Step: [%d,%d,%d]", p.xCoord, p.yCoord, p.zCoord));
            }

            Log.logger.info(String.format("Total Nodes Visited %d / %d", totalNodesVisited, totalNodesAdded));
        }

        return new PathEntity(points);
    }

    /**
     * Generate a pseudo-unique key for identifying a given node by it's coordinates
     * Encodes the lowest 12 bits of x,z and all useful bits of y.
     * This creates unique keys for all blocks within a 4096x256x4096 cube, which is FAR
     * bigger volume than one should attempt to pathfind within
     *
     * @param pos BlockPos to generate key from
     * @return key for node in map
     */
    protected static int computeNodeKey(BlockPos pos)
    {
        return ((pos.getX() & 0xFFF) << 20) |
                ((pos.getY() & 0xFF) << 12) |
                (pos.getZ() & 0xFFF);

        //  64 bit variant: 60 bits, 26 bits each of (x,z) and 8 bits of y
        //  Covers entire reachable boundaries of the world
        //  can probably skip the addition
//        return ((((long)x + 30000000) & 0x3FFFFFF) << 26) |
//                (((long)y & 0xFF) << 52) |
//                (((long)z + 30000000) & 0x3FFFFFF);
    }

    /**
     * Compute the cost (immediate 'g' value) of moving from the parent space to the new space
     *
     * @param parent The parent node being moved from
     * @param dPos The delta from the parent to the new space; assumes dx,dy,dz in range of [-1..1]
     * @return cost to move from the parent to the new position
     */
    protected double computeCost(Node parent, BlockPos dPos, boolean isSwimming)
    {
        double cost = 1D;

        if (dPos.getY() != 0 && (dPos.getX() != 0 || dPos.getZ() != 0))
        {
            //  Tax the cost for jumping, dropping (warning: also taxes stairs)
            cost *= 1.1D;
        }

        if (isSwimming)
        {
            cost *= 5D;
        }

        return cost;
    }

    /**
     * Compute the heuristic cost ('h' value) of a given position x,y,z
     *
     * Returning a value of 0 performs a breadth-first search
     * Returning a value less than actual possible cost to goal guarantees shortest path, but at computational expense
     * Returning a value exactly equal to the cost to the goal guarantees shortest path and least expense (but generally
     *   only works when path is straight and unblocked)
     * Returning a value greater than the actual cost to goal produces good, but not perfect paths, and is fast
     * Returning a very high value (such that 'h' is very high relative to 'g') then only 'h' (the heuristic) matters,
     *   as the search will be a very fast greedy best-first-search, ignoring cost weighting and distance
     *
     * @param pos Position to compute heuristic from
     * @return
     */
    protected abstract double computeHeuristic(BlockPos pos);

    /**
     * Return true if the given node is a viable final destination, and the path should generate to here
     *
     * @param n Node to test
     * @return true if the node is a viable destination
     */
    protected abstract boolean isAtDestination(Node n);

    /**
     * Compute a 'result score' for the Node; if no destination is determined, the node that had the highest
     * 'result' score is used.
     *
     * @param n Node to test
     * @return score for the node
     */
    protected abstract double getNodeResultScore(Node n);

    /**
     * "Walk" from the parent in the direction specified by the delta, determining the new x,y,z position for such a
     * move and adding or updating a node, as appropriate
     *
     * @param parent Node being walked from
     * @param dPos Delta from parent, expected in range of [-1..1]
     * @return true if a node was added or updated when attempting to move in the given direction
     */
    protected final boolean walk(Node parent, BlockPos dPos)
    {
    	BlockPos pos = parent.pos.add(dPos);

        //  Cheap test to perform before doing a 'y' test
        //  Has this node been visited?
        int nodeKey = computeNodeKey(pos);
        Node node = nodesVisited.get(nodeKey);
        if (node != null && node.closed)
        {
            //  Early out on previously visited and closed nodes
            return false;
        }

        //  Can we traverse into this node?  Fix the y up
        int newY = getGroundHeight(parent, pos);
        if (newY < 0)
        {
            return false;
        }

        if (pos.getY() != newY)
        {
            //  Has this node been visited?
            pos = new BlockPos(pos.getX(), newY, pos.getZ());
            nodeKey = computeNodeKey(pos);
            node = nodesVisited.get(nodeKey);
            if (node != null && node.closed)
            {
                //  Early out on previously visited and closed nodes
                return false;
            }
        }


        boolean isSwimming = (node != null) ? node.isSwimming : world.getBlockState(pos.down()).getBlock().getMaterial().isLiquid();

        //  Cost may have changed due to a jump up or drop
        double stepCost = computeCost(parent, dPos, isSwimming);
        double heuristic = computeHeuristic(pos);
        double cost = parent.cost + stepCost;
        double score = cost + heuristic;

        if (node != null)
        {
            //  This node already exists
            if (score >= node.score)
            {
                return false;
            }

            if (!nodesOpen.remove(node))
            {
                return false;
            }

            node.parent = parent;
            node.steps = parent.steps + 1;
            node.cost = cost;
            node.heuristic = heuristic;
            node.score = score;
        }
        else
        {
            node = new Node(parent, pos, cost, heuristic, score);
            nodesVisited.put(nodeKey, node);
            if (debugDrawEnabled)
            {
                debugNodesNotVisited.add(node);
            }

            if (isLadder(pos))
            {
                node.isLadder = true;
            }
            else if (isSwimming)
            {
                node.isSwimming = true;
            }

            node.counterAdded = ++totalNodesAdded;
        }

        nodesOpen.offer(node);

        //  Jump Point Search-ish optimization:
        // If this node was a (heuristic-based) improvement on our parent,
        // lets go another step in the same direction...
        if (allowJumpPointSearchTypeWalk &&
                node.heuristic <= parent.heuristic)
        {
            walk(node, dPos);
        }

        return true;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y
     *
     * @param pos coordinate of block
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop
     */
    protected int getGroundHeight(Node parent, BlockPos pos)
    {
        boolean canDrop = parent != null && !parent.isLadder;
        boolean canJump = parent != null && !parent.isLadder && !parent.isSwimming;
        boolean isSwimming = parent != null && parent.isSwimming;

        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (!isPassable(pos.up()))
        {
            return -1;
        }

        if(parent != null) {
            Block here = world.getBlockState(parent.pos.down()).getBlock();
            if (here.getMaterial().isLiquid() && !isPassable(pos)) {
                return -1;
            }
        }

        //  Now check the block we want to move to
        Block target = world.getBlockState(pos).getBlock();
        if (!isPassable(target, pos))
        {
            //  Need to try jumping up one, if we can
            if (!canJump || !isWalkableSurface(target, pos))
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

            //  Jump up one
            return pos.getY() + 1;
        }

        //  Do we have something to stand on in the target space?
        Block below = world.getBlockState(pos.down()).getBlock();
        if (isWalkableSurface(below, pos.down()))
        {
            //  Level path
            return pos.getY();
        }

        if (below.getMaterial().isLiquid())
        {
            if (isSwimming)
            {
                //  Already swimming in something, or allowed to swim and this is water
                return pos.getY();
            }

            if (allowSwimming && below.getMaterial() == Material.water)
            {
                //  This is water, and we are allowed to swim
                return pos.getY();
            }

            //  Not allowed to swim or this isn't water, and we're on dry land
            return -1;
        }

//        if (!isPassable(below, x, y - 1, z))
//        {
//            //  Can this happen anymore?
//            return -1;
//        }

        if (isLadder(below, pos.down()))
        {
            return pos.getY();
        }

        //  Nothing to stand on
        if (!canDrop || isSwimming)
        {
            return -1;
        }

        //  How far of a drop?
        below = world.getBlockState(pos.down(2)).getBlock();
        if (isWalkableSurface(below, pos.down(2)))
        {
            return pos.getY() - 1;
        }

        //  Too far
        return -1;
    }

    /**
     * Is the space passable?
     *
     * @param block
     * @param pos
     * @return true if the block does not block movement
     */
    protected boolean isPassable(Block block, BlockPos pos)
    {
        if (block.getMaterial() != Material.air)
        {
            if (block.getMaterial().blocksMovement())
            {
                return block instanceof BlockDoor ||
                        //  block instanceof BlockTrapDoor ||
                        block instanceof BlockFenceGate;
            }
            else if (block.getMaterial().isLiquid())
            {
                return false;
            }
        }

        return true;
    }

    protected boolean isPassable(BlockPos pos)
    {
        return isPassable(world.getBlockState(pos).getBlock(), pos);
    }

    /**
     * Is the block solid and can be stood upon?
     *
     * @param block Block to check
     * @param pos position of block
     * @return
     */
    protected boolean isWalkableSurface(Block block, BlockPos pos)
    {
        return //!block.getBlocksMovement(world, x, y, z) &&
                        block.getMaterial().isSolid() &&
                        !(block instanceof BlockFence) &&
                        !(block instanceof BlockFenceGate) &&
                        !(block instanceof BlockWall);
    }

    /**
     * Is the block a ladder?
     * @param block
     * @param pos
     * @return
     */
    protected boolean isLadder(Block block, BlockPos pos)
    {
        return block.isLadder(world, pos, null);
    }

    protected boolean isLadder(BlockPos pos)
    {
        return isLadder(world.getBlockState(pos).getBlock(), pos);
    }
}
