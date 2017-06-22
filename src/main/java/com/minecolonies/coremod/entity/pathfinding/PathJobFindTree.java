package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Find and return a path to the nearest tree.
 * Created: May 21, 2015
 */
public class PathJobFindTree extends AbstractPathJob
{
    /**
     * Tie breaker constant.
     */
    private static final double TIE_BREAKER = 0.951D;

    /**
     * The location of the hut of the lumberjack.
     */
    private final BlockPos hutLocation;

    /**
     * The list of trees the Lumberjack is supposed to cut.
     */
    private final Map<ItemStorage, Boolean> treesToCut;

    /**
     * AbstractPathJob constructor.
     * @param world the world within which to path.
     * @param start the start position from which to path from.
     * @param home  the position of the workers hut.
     * @param range maximum path range.
     * @param treesToCut the trees the lj is supposed to cut.
     */
    public PathJobFindTree(
            final World world,
            @NotNull final BlockPos start,
            final BlockPos home,
            final int range,
            final Map<ItemStorage, Boolean> treesToCut)
    {
        super(world, start, start, range, new TreePathResult());
        this.treesToCut = treesToCut;
        hutLocation = home;
    }

    /**
     * Custom result of the class which contains the position of the tree.
     */
    public static class TreePathResult extends PathResult
    {
        /**
         * Position of the found tree.
         */
        public BlockPos treeLocation;
    }

    @NotNull
    @Override
    public TreePathResult getResult()
    {
        return (TreePathResult) super.getResult();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        final int dx = pos.getX() - hutLocation.getX();
        final int dy = pos.getY() - hutLocation.getY();
        final int dz = pos.getZ() - hutLocation.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * TIE_BREAKER;
    }

    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        return n.parent != null && isNearTree(n);
    }

    private boolean isNearTree(@NotNull final Node n)
    {
        if (n.pos.getX() == n.parent.pos.getX())
        {
            final int dz = n.pos.getZ() > n.parent.pos.getZ() ? 1 : -1;
            return isTree(n.pos.add(0, 0, dz)) || isTree(n.pos.add(-1, 0, 0)) || isTree(n.pos.add(1, 0, 0));
        }
        else
        {
            final int dx = n.pos.getX() > n.parent.pos.getX() ? 1 : -1;
            return isTree(n.pos.add(-dx, 0, 0)) || isTree(n.pos.add(0, 0, -1)) || isTree(n.pos.add(0, 0, +1));
        }
    }

    private boolean isTree(final BlockPos pos)
    {
        if (Tree.checkTree(world, pos, treesToCut))
        {
            getResult().treeLocation = pos;
            return true;
        }

        return false;
    }

    @Override
    protected double getNodeResultScore(final Node n)
    {
        return 0;
    }

    @Override
    protected boolean isPassable(@NotNull final IBlockState block)
    {
        return super.isPassable(block) || block.getMaterial() == Material.LEAVES;
    }
}
