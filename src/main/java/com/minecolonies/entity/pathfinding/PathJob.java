package com.minecolonies.entity.pathfinding;

import com.minecolonies.MineColonies;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.Callable;

public class PathJob implements Callable<PathEntity>
{
    protected final ChunkCoordinates start, destination;
    protected final int maxRange;

    protected final IBlockAccess world;

    //  Job rules/configuration
    protected boolean allowSwimming = true;
    protected boolean allowJumpPointSearchTypeWalk = false;

    private static final float DESTINATION_SLACK_NONE     = 0;
    private static final float DESTINATION_SLACK_ADJACENT = 3.1F;    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    protected            float destinationSlack           = DESTINATION_SLACK_NONE; //  0 = exact match

    protected final Queue<Node>        nodesOpen    = new PriorityQueue<Node>(500);
    protected final Map<Integer, Node> nodesVisited = new HashMap<Integer, Node>();

    //  Debug Output
    protected int totalNodesAdded   = 0;
    protected int totalNodesVisited = 0;

    //  Debug Rendering
    protected boolean   debugEnabled         = false;
    protected int       debugSleepMs         = 0;
    protected Set<Node> debugNodesVisited    = null;
    protected Set<Node> debugNodesNotVisited = null;
    protected Set<Node> debugNodesPath       = null;

    static public Long debugNodeMonitor = new Long(1);
    static public Set<Node> lastDebugNodesVisited;
    static public Set<Node> lastDebugNodesNotVisited;
    static public Set<Node> lastDebugNodesPath;

    public PathJob(World world, ChunkCoordinates start, ChunkCoordinates end, int range)
    {
        int minX = Math.min(start.posX, end.posX);
        int minZ = Math.min(start.posZ, end.posZ);
        int maxX = Math.max(start.posX, end.posX);
        int maxZ = Math.max(start.posZ, end.posZ);

        this.world = new ChunkCache(world, minX, 0, minZ, maxX, 256, maxZ, 20);

        this.start = new ChunkCoordinates(start);
        this.destination = new ChunkCoordinates(end);
        this.maxRange = range;

        allowJumpPointSearchTypeWalk = false;

        if (Configurations.pathfindingDebugDraw)
        {
            debugEnabled = true;
            debugSleepMs = 0;
            debugNodesVisited = new HashSet<Node>();
            debugNodesNotVisited = new HashSet<Node>();
            debugNodesPath       = new HashSet<Node>();
        }
    }

    @Override
    public PathEntity call()
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

    private PathEntity search()
    {
        if (Configurations.pathfindingDebugVerbosity > 0)
        {
            MineColonies.logger.info(String.format("Pathfinding from [%d,%d,%d] to [%d,%d,%d]", start.posX, start.posY, start.posZ, destination.posX, destination.posY, destination.posZ));
        }

        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination.posX, destination.posY, destination.posZ) != destination.posY)
        {
            destinationSlack = DESTINATION_SLACK_ADJACENT;
        }

        Node startNode = new Node(start.posX, start.posY, start.posZ,
                computeHeuristic(start.posX, start.posY, start.posZ));

        if (isLadder(start.posX, start.posY, start.posZ))
        {
            startNode.isLadder = true;
        }
        else if (world.getBlock(start.posX, start.posY - 1, start.posZ).getMaterial().isLiquid())
        {
            startNode.isSwimming = true;
        }

        nodesOpen.offer(startNode);
        nodesVisited.put(computeNodeKey(start.posX, start.posY, start.posZ), startNode);

        ++totalNodesAdded;

        Node bestNode = startNode;
        double bestNodeDestinationDistanceSqrd = Double.MAX_VALUE;

        int cutoff = 0;
        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            Node currentNode = nodesOpen.poll();
            currentNode.counterVisited = ++totalNodesVisited;
            if (debugEnabled)
            {
                debugNodesNotVisited.remove(currentNode);
                debugNodesVisited.add(currentNode);
            }

            currentNode.closed = true;

            //  TODO: is currentNode the end result?
            if (Configurations.pathfindingDebugVerbosity == 2)
            {
                MineColonies.logger.info(String.format("Examining node [%d,%d,%d] ; g=%f ; f=%f", currentNode.x, currentNode.y, currentNode.z, currentNode.cost, currentNode.score));
            }

            if (isAtDestination(currentNode))
            {
                bestNode = currentNode;
                break;
            }

            //  If this is the closest node to our destination, treat it as our best node
            double currentNodeDestinationDistanceSqrd = ChunkCoordUtils.distanceSqrd(destination, currentNode.x, currentNode.y, currentNode.z);
            if (currentNodeDestinationDistanceSqrd < bestNodeDestinationDistanceSqrd)
            {
                bestNode = currentNode;
                bestNodeDestinationDistanceSqrd = currentNodeDestinationDistanceSqrd;
            }

            if (currentNode.steps <= maxRange)
            {
                int dx = 0, dy = 0, dz = 0;
                if (currentNode.parent != null)
                {
                    dx = currentNode.x - currentNode.parent.x;
                    dy = currentNode.y - currentNode.parent.y;
                    dz = currentNode.z - currentNode.parent.z;
                }

                if (currentNode.isLadder)
                {
                    //  On a ladder, we can go 1 straight-up
                    if (dy >= 0 || dx != 0 || dz != 0)
                    {
                        walk(currentNode, 0, 1, 0);
                    }
                }

                //  We can also go down 1, if the lower block is a ladder
                if (dy <= 0 || dx != 0 || dz != 0)
                {
                    if (isLadder(currentNode.x, currentNode.y - 1, currentNode.z))
                    {
                        walk(currentNode, 0, -1, 0);
                    }
                }

                if (dz <= 0)    walk(currentNode, 0, 0, -1);    //  N
                if (dx >= 0)    walk(currentNode, 1, 0, 0);     //  E
                if (dz >= 0)    walk(currentNode, 0, 0, 1);     //  S
                if (dx <= 0)    walk(currentNode, -1, 0, 0);    //  W
            }

            if (debugEnabled)
            {
                synchronized (debugNodeMonitor)
                {
                    lastDebugNodesNotVisited = new HashSet<Node>(debugNodesNotVisited);
                    lastDebugNodesVisited = new HashSet<Node>(debugNodesVisited);
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

        if (debugEnabled)
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

    public static ChunkCoordinates prepareStart(EntityLiving entity, World world)
    {
        int x = MathHelper.floor_double(entity.posX);
        int y = (int)entity.posY;
        int z = MathHelper.floor_double(entity.posZ);

        if (entity.isInWater())
        {
            while (world.getBlock(x, y, z).getMaterial().isLiquid())
            {
                ++y;
            }
        }
//        else if (y > 0 && world.getBlock(x, y - 1, z).getMaterial() == Material.air)
//        {
//            while (y > 0 && world.getBlock(x, y - 1, z).getMaterial() == Material.air)
//            {
//                --y;
//            }
//        }
        else if (world.getBlock(x, y, z) instanceof BlockFence)
        {
            //  Push away from fence
            double dX = entity.posX - Math.floor(entity.posX);
            double dZ = entity.posZ - Math.floor(entity.posZ);

            if (dX < 0.1)       x -= 1;
            else if (dX > 0.9)  x += 1;

            if (dZ < 0.1)       z -= 1;
            else if (dZ > 0.9)  z += 1;
        }

        return new ChunkCoordinates(x, y, z);
    }

    PathEntity finalizePath(Node bestNode)
    {
        int pathLength = 0;
        Node backtrace = bestNode;
        while (backtrace.parent != null)
        {
            ++pathLength;
            backtrace = backtrace.parent;
        }

        PathPoint points[] = new PathPoint[pathLength];

        Node nextInPath = null;
        backtrace = bestNode;
        while (backtrace.parent != null)
        {
            if (debugEnabled)
            {
                debugNodesVisited.remove(backtrace);
                debugNodesPath.add(backtrace);
            }

            --pathLength;

            int x = backtrace.x;
            int y = backtrace.y;
            int z = backtrace.z;

            if (backtrace.isSwimming)
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                y -= 1;
            }

            PathPointExtended p = new PathPointExtended(x, y, z);

            //  Climbing on a ladder?
            if (nextInPath != null && backtrace.isLadder &&
                    (nextInPath.x == x && nextInPath.z == z))
            {
                p.isOnLadder = true;
                if (nextInPath.y > y)
                {
                    //  We only care about facing if going up
                    p.ladderFacing = world.getBlockMetadata(x, y, z);
                }
            }
            else if (backtrace.parent != null && backtrace.parent.isLadder &&
                    (backtrace.parent.x == x && backtrace.parent.z == z))
            {
                p.isOnLadder = true;
            }

            points[pathLength] = p;

            nextInPath = backtrace;
            backtrace = backtrace.parent;
        }

        if (Configurations.pathfindingDebugVerbosity > 0)
        {
            MineColonies.logger.info("Path found:");

            for (PathPoint p : points)
            {
                MineColonies.logger.info(String.format("Step: [%d,%d,%d]", p.xCoord, p.yCoord, p.zCoord));
            }

            MineColonies.logger.info(String.format("Total Nodes Visited %d / %d", totalNodesVisited, totalNodesAdded));
        }

        return new PathEntity(points);
    }

    //  60 bit value encoding 26 bits each of (x,z) and 8 bits of y [the maximum reachable boundaries)
    //  in form: yxz
    //  Can probably can skip the addition, and only mask 15 bits worth (range of 32768 blocks)
//    protected static long computeNodeKey(int x, int y, int z)
//    {
//        return ((((long)x + 30000000) & 0x3FFFFFF) << 26) |
//                (((long)y & 0xFF) << 52) |
//                (((long)z + 30000000) & 0x3FFFFFF);
//    }

    //  32 bit variant, encodes lowest 12 bits of x,z and all bits of y;
    //  This creates unique keys for all blocks within a 4096x256x4096 cube, which is FAR more
    //  than you should be pathfinding
    protected static int computeNodeKey(int x, int y, int z)
    {
        return ((x & 0xFFF) << 20) |
                ((y & 0xFF) << 12) |
                (z & 0xFFF);
    }

    protected double computeCost(int dx, int dy, int dz)
    {
//        if (dy != 0 && dx == 0 && dz == 0)
//        {
//            //  Ladder up/down
//            return 1.5D;
//        }

        return 1D;
    }

    protected double computeHeuristic(int x, int y, int z)
    {
        //  This gives the best results; we ignore dy because (excepting ladders) dy translation
        // comes free with dx/dz movement.  Including Y component results in strange behavior
        // that prefers roundabout paths that bring it closer in the Y but are less optimal
        int dx = x - destination.posX;
        int dz = z - destination.posZ;

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dz)) * 1.001D;
    }

    protected boolean isAtDestination(Node n)
    {
        if (destinationSlack == DESTINATION_SLACK_NONE)
        {
            return n.x == destination.posX &&
                    n.y == destination.posY &&
                    n.z == destination.posZ;
        }

        return ChunkCoordUtils.distanceSqrd(destination, n.x, n.y, n.z) <= destinationSlack;
    }

    protected boolean walk(Node parent, int dx, int dy, int dz)
    {
        int x = parent.x + dx;
        int y = parent.y + dy;
        int z = parent.z + dz;

        //  Cheap test to perform before doing a 'y' test
        //  Has this node been visited?
        int nodeKey = computeNodeKey(x, y, z);
        Node node = nodesVisited.get(nodeKey);
        if (node != null && node.closed)
        {
            //  Early out on previously visited and closed nodes
            return false;
        }

        //  Can we traverse into this node?  Fix the y up
        if (parent != null)
        {
            int newY = getGroundHeight(parent, x, y, z);
            if (newY < 0)
            {
                return false;
            }

            if (y != newY)
            {
                //  Has this node been visited?
                y = newY;
                nodeKey = computeNodeKey(x, y, z);
                node = nodesVisited.get(nodeKey);
                if (node != null && node.closed)
                {
                    //  Early out on previously visited and closed nodes
                    return false;
                }
            }
        }

        //  Cost may have changed due to a jump up or drop
        double stepCost = computeCost(dx, dy, dz);
        if (y != parent.y && (dx != 0 || dz != 0))
        {
            //  Tax the cost for jumping or dropping (warning: also taxes stairs)
            stepCost += 0.1D;
        }

        boolean isSwimming = (node != null) ? node.isSwimming : world.getBlock(x, y - 1, z).getMaterial().isLiquid();
        if (isSwimming)
        {
            stepCost *= 5;
        }

        double heuristic = computeHeuristic(x, y, z);
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
            node = new Node(parent, x, y, z, cost, heuristic, score);
            nodesVisited.put(nodeKey, node);
            if (debugEnabled)
            {
                debugNodesNotVisited.add(node);
            }

            if (isLadder(x, y, z))
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
            walk(node, dx, dy, dz);
        }

        return true;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y
     *
     * @param x,y,z coordinate of block
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop
     */
    protected int getGroundHeight(Node parent, int x, int y, int z)
    {
        boolean canDrop = parent != null && !parent.isLadder;
        boolean canJump = parent != null && !parent.isLadder && !parent.isSwimming;
        boolean isSwimming = parent != null && parent.isSwimming;

        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (!isPassable(x, y + 1, z))
        {
            return -1;
        }

        //  Now check the block we want to move to
        Block target = world.getBlock(x, y, z);
        if (!isPassable(target, x, y, z))
        {
            //  Need to try jumping up one, if we can
            if (!canJump || !isWalkableSurface(target, x, y, z))
            {
                return -1;
            }

            //  Check for headroom in the target space
            if (!isPassable(x, y + 2, z))
            {
                return -1;
            }

            //  Check for jump room from the origin space
            if (!isPassable(parent.x, parent.y + 2, parent.z))
            {
                return -1;
            }

            //  Jump up one
            return y + 1;
        }

        //  Do we have something to stand on in the target space?
        Block below = world.getBlock(x, y - 1, z);
        if (isWalkableSurface(below, x, y - 1, z))
        {
            //  Level path
            return y;
        }

        if (below.getMaterial().isLiquid())
        {
            if (isSwimming)
            {
                //  Already swimming in something, or allowed to swim and this is water
                return y;
            }

            if (allowSwimming && below.getMaterial() == Material.water)
            {
                //  This is water, and we are allowed to swim
                return y;
            }

            //  Not allowed to swim or this isn't water, and we're on dry land
            return -1;
        }

//        if (!isPassable(below, x, y - 1, z))
//        {
//            //  Can this happen anymore?
//            return -1;
//        }

        if (below.isLadder(world, x, y - 1, z, null))
        {
            return y;
        }

        //  Nothing to stand on
        if (!canDrop || isSwimming)
        {
            return -1;
        }

        //  How far of a drop?
        below = world.getBlock(x, y - 2, z);
        if (isWalkableSurface(below, x, y - 2, z))
        {
            return y - 1;
        }

        //  Too far
        return -1;
    }

    protected boolean isPassable(int x, int y, int z)
    {
        return isPassable(world.getBlock(x, y, z), x, y, z);
    }

    protected boolean isPassable(Block block, int x, int y, int z)
    {
        if (block.getMaterial() != Material.air)
        {
            if (!block.getBlocksMovement(world, x, y, z))
            {
                if (block instanceof BlockDoor ||
                        //  block instanceof BlockTrapDoor ||
                        block instanceof BlockFenceGate)
                {
                    return true;
                }

                return false;
            }
            else if (block.getMaterial().isLiquid())
            {
                return false;
            }
        }

        return true;
    }

    protected boolean isWalkableSurface(Block block, int x, int y, int z)
    {
        return //!block.getBlocksMovement(world, x, y, z) &&
                block.getMaterial().isSolid() &&
                !(block instanceof BlockFence) &&
                !(block instanceof BlockFenceGate);
    }

    protected boolean isLadder(int x, int y, int z)
    {
        return world.getBlock(x, y, z).isLadder(world, x, y, z, null);
    }
}
