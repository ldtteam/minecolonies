package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.ai.citizen.lumberjack.Tree;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Find and return a path to the nearest tree
 * Created: May 21, 2015
 *
 * @author Colton
 */
public class PathJobFindTree extends AbstractPathJob
{
    private BlockPos hutLocation;

    /**
     * AbstractPathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param home  the position of the workers hut
     * @param range maximum path range
     */
    public PathJobFindTree(World world, BlockPos start, BlockPos home, int range)
    {
        super(world, start, start, range, new TreePathResult());

        hutLocation = home;
    }

    public static class TreePathResult extends PathResult
    {
        public BlockPos treeLocation;
    }

    @Override
    public TreePathResult getResult()
    {
        return (TreePathResult) super.getResult();
    }

    @Override
    protected double computeHeuristic(BlockPos pos)
    {
        int dx = pos.getX() - hutLocation.getX();
        int dy = pos.getY() - hutLocation.getY();
        int dz = pos.getZ() - hutLocation.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 0.951D;
    }

    @Override
    protected boolean isAtDestination(Node n)
    {
        return n.parent != null && isNearTree(n);
    }

    private boolean isNearTree(Node n)
    {
        if (n.pos.getX() != n.parent.pos.getX())
        {
            int dx = n.pos.getX() > n.parent.pos.getX() ? 1 : -1;
            return isTree(n.pos.add(-dx, 0, 0)) || isTree(n.pos.add(0, 0, -1)) || isTree(n.pos.add(0, 0, +1));
        }
        else
        {
            int dz = n.pos.getZ() > n.parent.pos.getZ() ? 1 : -1;
            return isTree(n.pos.add(0, 0, dz)) || isTree(n.pos.add(-1, 0, 0)) || isTree(n.pos.add(1, 0, 0));
        }
    }

    private boolean isTree(BlockPos pos)
    {
        if (Tree.checkTree(world, pos))
        {
            getResult().treeLocation = pos;
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
    protected boolean isPassable(Block block, BlockPos pos)
    {
        return super.isPassable(block, pos) || block.isLeaves(world, pos);
    }
}
