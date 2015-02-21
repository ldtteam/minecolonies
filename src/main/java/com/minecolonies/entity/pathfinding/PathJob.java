package com.minecolonies.entity.pathfinding;

import com.minecolonies.MineColonies;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
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

    protected final Queue<Node>     nodesOpen    = new PriorityQueue<Node>(500);
    protected final Map<Long, Node> nodesVisited = new HashMap<Long, Node>();

    protected int totalNodesAdded = 0;
    protected int totalNodesVisited = 0;

    public PathJob(PathPlanner owner, World world, ChunkCoordinates start, ChunkCoordinates end)
    {
//        startX = start.posX;
//        startY = start.posY;
//        startZ = start.posZ;
//
        this.start = start;
        this.destination = end;

        this.owner = owner;
        this.world = new ChunkCache(world, start.posX, start.posY, start.posZ, end.posX, end.posY, end.posZ, 20);
    }

    @Override
    public void run()
    {
        MineColonies.logger.info(String.format("Pathing from [%d,%d,%d] to [%d,%d,%d]",
                start.posX, start.posY, start.posZ, destination.posX, destination.posY, destination.posZ));
//        double heuristic = Math.sqrt(ChunkCoordUtils.distanceSqrd(start, destination));   //  TODO
        Node startNode = new Node(start.posX, start.posY, start.posZ);
        nodesVisited.put(computeNodeKey(start.posX, start.posY, start.posZ), startNode);

        Node currentNode = startNode;
        ++totalNodesAdded;

        int cutoff = 0;
        while (currentNode != null)
        {
            ++totalNodesVisited;

            currentNode.closed = true;

            //  TODO: is currentNode the end result?
            MineColonies.logger.info(String.format("Examining node [%d,%d,%d]", currentNode.x, currentNode.y, currentNode.z));
            if (ChunkCoordUtils.distanceSqrd(destination, currentNode.x, currentNode.y, currentNode.z) <= (1.5*1.5*2))
            {
                MineColonies.logger.info("Path found!");
                List<Node> path = new ArrayList<Node>();
                Node backtrace = currentNode;
                while (backtrace != null)
                {
                    path.add(backtrace);
                    backtrace = backtrace.parent;
                }
                Collections.reverse(path);

                for (Node n : path)
                {
                    MineColonies.logger.info(String.format("Step: [%d,%d,%d]", n.x, n.y, n.z));
                }

                MineColonies.logger.info(String.format("Total Nodes Added: %d    Visited: %d", totalNodesAdded, totalNodesVisited));

                return;
            }

            if (currentNode.cost < 40)
            {
                if (currentNode.isLadder)
                {
                    //  On a ladder, we can go 1 straight-up
                    walk(currentNode, currentNode.x, currentNode.y + 1, currentNode.z, 2.0D, false);

                    //  We can also go down 1, if the lower block is a ladder
                    if (isLadder(currentNode.x, currentNode.y - 1, currentNode.z))
                    {
                        walk(currentNode, currentNode.x, currentNode.y - 1, currentNode.z, 2.0D, false);
                    }
                }

                for (int x = -1; x < 2; ++x)
                {
                    for (int z = -1; z < 2; ++z)
                    {
                        if (x == 0 && z == 0) continue;

                        boolean isDiagonal = (x != 0 && z != 0);
                        double cost = isDiagonal ? 1.414D : 1D;
                        walk(currentNode, currentNode.x + x, currentNode.y, currentNode.z + z, cost, isDiagonal);
                    }
                }
            }

            currentNode = nodesOpen.poll();
        }
    }

    //  60 bit value encoding 26 bits each of (x,z) and 8 bits of y [the maximum reachable boundaries)
    //  in form: yxz
    //  Can probably can skip the addition, and only mask 15 bits worth (range of 32768 blocks)
    protected static long computeNodeKey(int x, int y, int z)
    {
        return ((((long)x + 30000000) & 0x3FFFFFF) << 26) |
                (((long)y & 0xFF) << 52) |
                (((long)z + 30000000) & 0x3FFFFFF);
    }

    protected void walk(Node parent, int x, int y, int z, double stepCost, boolean isDiagonal)
    {
        //  Cheap test to perform before doing a 'y' test
        //  Has this node been visited?
        long nodeKey = computeNodeKey(x, y, z);
        Node node = nodesVisited.get(nodeKey);
        if (node != null && node.closed)
        {
            //  Early out on previously visited and closed nodes
            return;
        }

        //  Can we traverse into this node?  Fix the y up
        if (parent != null)
        {
            int newY = getGroundHeight(x, y, z, parent.isLadder);
            if (newY < 0)
            {
                return;
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
                    return;
                }
            }
        }


        if (isDiagonal)
        {
            //  In case of diagonal, BOTH common neighbor non-diagonal blocks must be at the same level

            int dX = x - parent.x;
            int dZ = z - parent.z;

            //  If we have JPS jumping, dX and dZ could be > 1, so we need to normalize
            //  Cheap, fast normalize
//            if (dX > 1)         dX = 1;
//            else if (dX < -1)   dX = -1;
//            if (dZ > 1)         dZ = 1;
//            else if (dZ < -1)   dZ = -1;

            //  Test neighbors, with offset from new block, computed from delta of parent
            //  dX,dZ   x1,z1   x2,z2
            //  -1,1    1,0     0,-1
            //  1,1     -1,0    0,-1
            //  1,-1    -1,0    0,1
            //  -1,-1   1,0     0,1

            //  TODO - Verify this is actually testing the right blocks...
            if (getGroundHeight(x - dX, y, z, parent.isLadder) != y ||
                    getGroundHeight(x, y, z - dZ, parent.isLadder) != y)
            {
                return;
            }
        }

        double heuristic = Math.sqrt(ChunkCoordUtils.distanceSqrd(destination, x, y, z));   //  TODO
        double cost = parent.cost + stepCost;
        double score = cost + heuristic;

        if (node != null)
        {
            //  This node already exists
            if (score >= node.score)
            {
                return;
            }

            if (!nodesOpen.remove(node))
            {
                return;
            }

            node.parent = parent;
            node.cost = cost;
            node.score = score;
        }
        else
        {
            node = new Node(parent, x, y, z, cost, score);
            nodesVisited.put(nodeKey, node);

            if (isLadder(x, y, z))
            {
                node.isLadder = true;
            }
        }

        nodesOpen.offer(node);
        ++totalNodesAdded;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y
     *
     * @param x,y,z coordinate of block
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop
     */
    protected int getGroundHeight(int x, int y, int z, boolean sameLevelOnly)
    {
        //  Check (y+1) first, as it's always needed, either
        //  for the upper body (level), lower body (headroom drop) or lower body (jump)
        if (!isPassable(x, y + 1, z))
        {
            return -1;
        }

        //  Now check the block we want to move to
        if (!isPassable(x, y, z))
        {
            //  Need to try jumping up one
            if (sameLevelOnly)
            {
                return -1;
            }

            //  Check for headroom in the target space
            if (!isPassable(x, y + 2, z))
            {
                return -1;
            }

            //  Jump up one
            return y + 1;
        }

        //  Do we have something to stand on in the target space?
        Block below = world.getBlock(x, y - 1, z);
        if (!isPassable(below) || below.isLadder(world, x, y - 1, z, null))
        {
            //  Level path, continue
            return y;
        }

        //  Nothing to stand on
        if (sameLevelOnly)
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

    protected boolean isPassable(int x, int y, int z)
    {
        return isPassable(world.getBlock(x, y, z));
    }

    protected boolean isPassable(Block block)
    {
        return block != null && !block.getMaterial().isSolid();
    }

    protected boolean isLadder(int x, int y, int z)
    {
        return world.getBlock(x, y, z).isLadder(world, x, y, z, null);
    }
}
