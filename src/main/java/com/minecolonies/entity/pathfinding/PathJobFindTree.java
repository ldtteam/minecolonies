package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.ai.Tree;
import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

/**
 * Find and return a path to the nearest tree
 * Created: May 21, 2015
 *
 * @author Colton
 */
public class PathJobFindTree extends PathJob
{
    public static class TreePathResult extends PathResult
    {
        public ChunkCoordinates    treeLocation;
    }

    ChunkCoordinates hutLocation;

    /**
     * PathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param home   the position of the workers hut
     * @param range maximum path range
     */
    public PathJobFindTree(World world, ChunkCoordinates start, ChunkCoordinates home, int range)
    {
        super(world, start, start, range, new TreePathResult());

        hutLocation = home;
    }

    @Override
    public TreePathResult getResult() { return (TreePathResult)super.getResult(); }

    @Override
    protected double computeHeuristic(int x, int y, int z)
    {
        int dx = x - hutLocation.posX;
        int dy = y - hutLocation.posY;
        int dz = z - hutLocation.posZ;

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 0.501D ;
    }

    @Override
    protected boolean isAtDestination(Node n)
    {
        if(n.parent == null)
        {
            return false;
        }

        if (n.x != n.parent.x)
        {
            int dx = n.x > n.parent.x ? 1 : -1;
            return isTree(n.x+dx, n.y, n.z) || isTree(n.x, n.y, n.z-1) || isTree(n.x, n.y, n.z+1);
        }
        else//z
        {
            int dz = n.z > n.parent.z ? 1 : -1;
            return isTree(n.x, n.y, n.z+dz) || isTree(n.x-1, n.y, n.z) || isTree(n.x+1, n.y, n.z);
        }
    }

    private boolean isTree(int x, int y, int z)
    {
        if(Tree.checkTree(world, x, y, z))
        {
            getResult().treeLocation = new ChunkCoordinates(x, y, z);
            return true;
        }

        return false;
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        return 0;
    }

    @Override
    protected boolean isPassable(Block block, int x, int y, int z)
    {
        return super.isPassable(block, x, y, z) || block.isLeaves(world, x, y, z);
    }
}
