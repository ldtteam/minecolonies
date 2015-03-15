package com.minecolonies.entity.pathfinding;

import com.minecolonies.MineColonies;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;

public class PathJob implements Runnable
{
    //protected final int startX, startY, startZ;

    protected final ChunkCoordinates start, destination;

    protected final PathPlanner  owner;
    //protected final World       world;
    protected final IBlockAccess world;

    //  Job rules/configuration
    protected boolean           allowDiagonalMovement = false;
    protected boolean           allowJumpPointSearchTypeWalk = false;

    protected final Queue<Node>     nodesOpen    = new PriorityQueue<Node>(500);
    protected final Map<Integer, Node> nodesVisited = new HashMap<Integer, Node>();
    protected Node                  destinationNode;

    protected int totalNodesAdded = 0;
    protected int totalNodesVisited = 0;

//    static public Set<Node>    debugNodesVisited = Collections.synchronizedSet(new HashSet<Node>());
//    static public Set<Node>    debugNodesNotVisited = Collections.synchronizedSet(new HashSet<Node>());
//    static public Set<Node>    debugNodesPath = Collections.synchronizedSet(new HashSet<Node>());

    protected int          debugSleepMs = 25;
    protected Set<Node>    debugNodesVisited = new HashSet<Node>();
    protected Set<Node>    debugNodesNotVisited = new HashSet<Node>();
    protected Set<Node>    debugNodesPath = new HashSet<Node>();

    static public Long debugNodeMonitor = new Long(1);
    static public Set<Node>    lastDebugNodesVisited;
    static public Set<Node>    lastDebugNodesNotVisited;
    static public Set<Node>    lastDebugNodesPath;

    public PathJob(PathPlanner owner, World world, ChunkCoordinates start, ChunkCoordinates end)
    {
//        startX = start.posX;
//        startY = start.posY;
//        startZ = start.posZ;

        int minX = Math.min(start.posX, end.posX);
        int minZ = Math.min(start.posZ, end.posZ);
        int maxX = Math.max(start.posX, end.posX);
        int maxZ = Math.max(start.posZ, end.posZ);

        this.start = start;
        this.destination = end;

        this.owner = owner;
        this.world = new ChunkCache(world, minX, 0, minZ, maxX, 256, maxZ, 20);

        allowDiagonalMovement = false;
        allowJumpPointSearchTypeWalk = false;
    }

    @Override
    public void run()
    {
        debugNodesVisited.clear();
        debugNodesNotVisited.clear();
        debugNodesPath.clear();

        MineColonies.logger.info(String.format("Pathing from [%d,%d,%d] to [%d,%d,%d]",
                start.posX, start.posY, start.posZ, destination.posX, destination.posY, destination.posZ));
//        double heuristic = Math.sqrt(ChunkCoordUtils.distanceSqrd(start, destination));   //  TODO
        Node startNode = new Node(start.posX, start.posY, start.posZ);
        startNode.score = computeHeuristic(startNode.x, startNode.y, startNode.z);
        nodesOpen.offer(startNode);
        //nodesVisited.put(computeNodeKey(start.posX, start.posY, start.posZ), startNode);

        ++totalNodesAdded;

        Node bestNode = startNode;
        double bestNodeDestinationDistanceSqrd = Double.MAX_VALUE;

        int cutoff = 0;
        while (!nodesOpen.isEmpty())
        {
            Node currentNode = nodesOpen.poll();
            currentNode.counterVisited = ++totalNodesVisited;
            debugNodesNotVisited.remove(currentNode);
            debugNodesVisited.add(currentNode);

            currentNode.closed = true;

            //  TODO: is currentNode the end result?
            MineColonies.logger.info(String.format("Examining node [%d,%d,%d] ; g=%f ; f=%f", currentNode.x, currentNode.y, currentNode.z, currentNode.cost, currentNode.score));

            //  If this is the closest node to our destination, treat it as our best node
            double currentNodeDestinationDistanceSqrd = ChunkCoordUtils.distanceSqrd(destination, currentNode.x, currentNode.y, currentNode.z);
            if (currentNodeDestinationDistanceSqrd < bestNodeDestinationDistanceSqrd)
            {
                bestNode = currentNode;
                bestNodeDestinationDistanceSqrd = currentNodeDestinationDistanceSqrd;
            }

            //if (currentNode.score < 400)
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
                        if (walk(currentNode, 0, 1, 0))
                        {
                            break;
                        }
                    }
                }

                //  We can also go down 1, if the lower block is a ladder
                if (dy <= 0 || dx != 0 || dz != 0)
                {
                    if (isLadder(currentNode.x, currentNode.y - 1, currentNode.z))
                    {
                        if (walk(currentNode, 0, -1, 0))
                        {
                            break;
                        }
                    }
                }

//                for (int x = -1; x < 2; ++x)
//                {
//                    for (int z = -1; z < 2; ++z)
//                    {
//                        if (x == 0 && z == 0) continue;
//
//                        walk(currentNode, x, 0, z);
//                    }
//                }

//                walk(currentNode, 0, 0, -1);    //  N
//                walk(currentNode, 1, 0, 0);     //  E
//                walk(currentNode, 0, 0, 1);     //  S
//                walk(currentNode, -1, 0, 0);    //  W
//                if (allowDiagonalMovement)
//                {
//                    walk(currentNode, 1, 0, -1);    //  NE
//                    walk(currentNode, 1, 0, 1);     //  SE
//                    walk(currentNode, -1, 0, 1);    //  SW
//                    walk(currentNode, -1, 0, -1);   //  NW
//                }

                if ((dz <= 0) && walk(currentNode, 0, 0, -1))   break;  //  N
                if ((dx >= 0) && walk(currentNode, 1, 0, 0))    break;  //  E
                if ((dz >= 0) && walk(currentNode, 0, 0, 1))    break;  //  S
                if ((dx <= 0) && walk(currentNode, -1, 0, 0))   break;  //  W
                if (allowDiagonalMovement)
                {
                    if ((dx >= 0 || dz < 0) && walk(currentNode, 1, 0, -1))     break;  //  NE
                    if ((dx >= 0 || dz > 0) && walk(currentNode, 1, 0, 1))      break;  //  SE
                    if ((dx <= 0 || dz > 0) && walk(currentNode, -1, 0, 1))     break;  //  SW
                    if ((dx <= 0 || dz < 0) && walk(currentNode, -1, 0, -1))    break;  //  NW
                }
            }

            if (true)
            {
                synchronized (debugNodeMonitor)
                {
                    lastDebugNodesNotVisited = new HashSet<Node>(debugNodesNotVisited);
                    lastDebugNodesVisited = new HashSet<Node>(debugNodesVisited);
                    lastDebugNodesPath = new HashSet<Node>();
                }

                if (debugSleepMs != 0)
                {
                    try { Thread.sleep(debugSleepMs); } catch (InterruptedException ex) {}
                }
            }
        }

        if (destinationNode == null)
        {
            destinationNode = bestNode;
        }

        if (destinationNode != null)
        {
            MineColonies.logger.info("Path found!");
            List<Node> path = new ArrayList<Node>();
            Node backtrace = destinationNode;
            while (backtrace != null)
            {
                path.add(backtrace);
                backtrace = backtrace.parent;
            }
            Collections.reverse(path);

            for (Node n : path)
            {
                MineColonies.logger.info(String.format("Step: [%d,%d,%d]", n.x, n.y, n.z));
                debugNodesVisited.remove(n);
                debugNodesPath.add(n);
            }

            MineColonies.logger.info(String.format("Total Nodes Added: %d    Visited: %d", totalNodesAdded, totalNodesVisited));
        }

        synchronized (debugNodeMonitor)
        {
            lastDebugNodesNotVisited = debugNodesNotVisited;
            lastDebugNodesVisited = debugNodesVisited;
            lastDebugNodesPath = debugNodesPath;
        }
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

        if (dx != 0 && dz != 0)
        {
            //  Diagonal
            return 1.414D;
        }

        return 1D;
    }

    protected double computeHeuristic(int x, int y, int z)
    {
        //  Method 2 - Minimum distance in steps
        int dx = x - destination.posX;
        int dy = y - destination.posY;
        int dz = z - destination.posZ;

        //  This gives the best results; we ignore dy because (excepting ladders) dy translation
        // comes free with dx/dz movement.  Including Y component results in strange behavior
        // that prefers roundabout paths that bring it closer in the Y but are less optimal
        dx = (dx * dx);
        dy = 0; //(dy * dy);
        dz = (dz * dz);
        return (dx + dy + dz) / 2;
        //return Math.sqrt(dx + dy + dz);
    }

    protected boolean isAtDestination(Node n)
    {
        return ChunkCoordUtils.distanceSqrd(destination, n.x, n.y, n.z) <= (1.5*1.5*2);
    }

    protected double getScoreCutoff()
    {
        return 120D * 120D;
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
            int newY = getGroundHeight(parent, x, y, z, !parent.isLadder, !parent.isLadder);
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
//        if (parent != null &&
//                dy == 0 &&
//                y > parent.y)
//        {
//            //  Tax the cost for jumping up one (warning: also taxes stairs)
//            stepCost *= 1.25D;
//        }

        double heuristic = computeHeuristic(x, y, z);
        double cost = parent.cost + stepCost;
        double score = cost + heuristic;

        if (score >= getScoreCutoff())
        {
            //  If going to this node makes it impossible to reach the destination
            return false;
        }

        if (dx != 0 && dz != 0)
        {
            //  In case of diagonal, BOTH common neighbor non-diagonal blocks must be at the same level

            //  Test neighbors, with offset from new block, computed from delta of parent
            //  dX,dZ   x1,z1   x2,z2
            //  -1,1    1,0     0,-1
            //  1,1     -1,0    0,-1
            //  1,-1    -1,0    0,1
            //  -1,-1   1,0     0,1

            //  TODO - Verify this is actually testing the right blocks...
            //  ... so far it seems right
            int cornerY = getGroundHeight(parent, x - dx, y, z, true, !parent.isLadder);
            if (cornerY > y || cornerY == -1) return false;
            cornerY = getGroundHeight(parent, x, y, z - dz, true, !parent.isLadder);
            if (cornerY > y || cornerY == -1) return false;
        }

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
            node.cost = cost;
            node.heuristic = heuristic;
            node.score = score;
        }
        else
        {
            node = new Node(parent, x, y, z, cost, heuristic, score);
            nodesVisited.put(nodeKey, node);
            debugNodesNotVisited.add(node);

            if (isLadder(x, y, z))
            {
                node.isLadder = true;
            }

            node.counterAdded = ++totalNodesAdded;
        }

        if (isAtDestination(node))
        {
            destinationNode = node;
            return true;
        }

        //  Jump Point Search-ish optimization:
        // If this node was a (heuristic-based) improvement on our parent,
        // lets go another step in the same direction...
        if (allowJumpPointSearchTypeWalk &&
                node.heuristic <= parent.heuristic)
        {
            if (walk(node, dx, dy, dz))
            {
                return true;
            }
        }

        nodesOpen.offer(node);

        return false;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y
     *
     * @param x,y,z coordinate of block
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop
     */
    protected int getGroundHeight(Node parent, int x, int y, int z, boolean canDrop, boolean canJump)
    {
        Block b = world.getBlock(x, y + 1, z);

        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (!isPassable(b, x, y + 1, z))
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
            target = world.getBlock(x, y + 2, z);
            if (!isPassable(target, x, y+2, z))
            {
                return -1;
            }

            //  Check for jump room from the origin space
            if (world.getBlock(parent.x, parent.y + 2, parent.z).getMaterial() != Material.air)
            {
                return -1;
            }

            //  Jump up one
            return y + 1;
        }

        //  Do we have something to stand on in the target space?
        Block below = world.getBlock(x, y - 1, z);
        if (!isPassable(below, x, y - 1, z))
        {
            if (!isWalkableSurface(below, x, y - 1, z))
            {
                return -1;
            }

            //  Level path, continue
            return y;
        }

        if (below.isLadder(world, x, y - 1, z, null))
        {
            return y;
        }

        //  Nothing to stand on
        if (!canDrop)
        {
            return -1;
        }

        //  How far of a drop?
        if (!isPassable(x, y - 2, z))
        {
            return y - 1;
        }

        //  Too far
        return -1;
    }

//
//    protected int isPassableArea(int x, int y, int z)
//    {
//        Block floor = world.getBlock(x, y - 1, z);
//        Block lower = world.getBlock(x, y, z);
//        Block upper = world.getBlock(x, y + 1, z);
//
//        //  Check (y+1) first, as it's always needed, either for the upper body (level),
//        //  lower body (headroom drop) or lower body (jump up)
//        if (!isPassable(x, y + 1, z))
//        {
//            return -1;
//        }
//
//        //  Now check the block we want to move to
//        Block target = world.getBlock(x, y, z);
//        if (!isPassable(target, x, y, z))
//        {
//            //  Need to try jumping up one
//            if (sameLevelOnly)
//            {
//                return -1;
//            }
//
//            if (!isWalkableSurface(target))
//            {
//                return -1;
//            }
//
//            //  Check for headroom in the target space
//            if (!isPassable(x, y + 2, z))
//            {
//                return -1;
//            }
//
//            //  Jump up one
//            return y + 1;
//        }
//
//        //  Do we have something to stand on in the target space?
//        Block below = world.getBlock(x, y - 1, z);
//        if (!isPassable(below))
//        {
//            if (!isWalkableSurface(below))
//            {
//                return -1;
//            }
//
//            //  Level path, continue
//            return y;
//        }
//
//        if (below.isLadder(world, x, y - 1, z, null))
//        {
//            return y;
//        }
//
//        //  Nothing to stand on
//        if (sameLevelOnly)
//        {
//            return -1;
//        }
//
//        //  How far of a drop?
//        if (!isPassable(x, y - 2, z))
//        {
//            return y - 1;
//        }
//
//        //  Too far
//        return -1;
//    }

//    protected int getGroundHeight(int x, int y, int z, boolean fromLadder)
//    {
//        if (isViable(x, y, z, 0))       return y;       //  Level
//        if (fromLadder) return -1;
//        if (isViable(x, y - 1, z, -1))  return y - 1;   //  Drop
//        if (isViable(x, y + 1, z, +1))  return y + 1;   //  Jump
//        return -1;
//    }
//
//    protected boolean isViable(int x, int y, int z, int yOffset)
//    {
//        Block block = world.getBlock(x, y, z);
//
//        //  If the block itself isn't passable, no joy!
//        if (!isPassable(block))
//        {
//            return false;
//        }
//
//        //  If a Human can't stand at the location, it's not passable!
//        if (!isPassable(x, y + 1, z))
//        {
//            //  No room above
//            return false;
//        }
//
//        if (block == Blocks.ladder)
//        {
//            return true;
//        }
//
//        //  Is this a drop?
//        if (isPassable(x, y - 1, z) &&
//                ((block == Blocks.air) ||
//                block.getBlocksMovement(world, x, y - 1, z)))
//        {
//            //  This is a drop with no ladder
//            return false;
//        }
//
//        //  If descending, check for headroom
//        if (yOffset < 0 && !isPassable(x, y - yOffset, z))
//        {
//            return false;
//        }
//
//        return true;
//    }
//
//    protected boolean isPassable(int x, int y, int z)
//    {
//        return isPassable(world.getBlock(x, y, z), x, y, z);
//    }
//
//    protected boolean isPassable(int x, int y, int z)
//    {
//        Block block = world.getBlock(x, y, z);
//
//        if (block == null)
//        {
//            return false;
//        }
//
//        if (block.getMaterial().isSolid())
//        {
//            if (block instanceof BlockDoor ||
//                    block instanceof BlockTrapDoor)
//            {
//                return true;
//            }
//
//            return false;
//        }
//
//        return true;
//    }

    protected boolean isPassable(int x, int y, int z)
    {
        return isPassable(world.getBlock(x, y, z), x, y, z);
    }

    protected boolean isPassable(Block block, int x, int y, int z)
    {
        if (block == null)
        {
            return false;
        }

        if (block.getMaterial() != Material.air)
        {
            if (!block.getBlocksMovement(world, x, y, z))
            {
                if (block instanceof BlockDoor /*||
                        block instanceof BlockTrapDoor*/)
                {
                    return true;
                }
                if (block instanceof BlockFenceGate)
                {
                    return true;
                }

                return false;
            }
        }

        return true;
    }

    protected boolean isWalkableSurface(Block block, int x, int y, int z)
    {
        return !block.getBlocksMovement(world, x, y, z) &&
                !(block instanceof BlockFence) &&
                !(block instanceof BlockFenceGate);
    }

    protected boolean isLadder(int x, int y, int z)
    {
        return world.getBlock(x, y, z).isLadder(world, x, y, z, null);
    }
}
