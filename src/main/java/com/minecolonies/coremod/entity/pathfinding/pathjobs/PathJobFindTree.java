package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.pathfinding.TreePathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import com.minecolonies.coremod.entity.pathfinding.Node;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Find and return a path to the nearest tree. Created: May 21, 2015
 */
public class PathJobFindTree extends AbstractPathJob
{
    /**
     * Tie breaker constant.
     */
    private static final double TIE_BREAKER = 0.951D;

    /**
     * How much should be restricted area shrinked because of isTree check
     */
    private static final Vector3i AREA_SHRINK = new Vector3i(-1, 0, -1);

    /**
     * The location of the hut of the lumberjack.
     */
    private final BlockPos hutLocation;

    /**
     * The list of trees the Lumberjack is supposed to cut.
     */
    private final List<ItemStorage> treesToNotCut;

    /**
     * The Colony the tree is in.
     */
    private final IColony colony;

    /**
     * Fake goal when using restricted area
     */
    private final BlockPos boxCenter;

    /**
     * AbstractPathJob constructor.
     *
     * @param world      the world within which to path.
     * @param start      the start position from which to path from.
     * @param home       the position of the worker hut.
     * @param range      maximum path range.
     * @param treesToCut the trees the lj is supposed to cut.
     * @param entity     the entity.
     * @param colony     the colony.
     */
    public PathJobFindTree(
      final World world,
      @NotNull final BlockPos start,
      final BlockPos home,
      final int range,
      final List<ItemStorage> treesToCut,
      final IColony colony,
      final LivingEntity entity)
    {
        super(world, start, start, range, new TreePathResult(), entity);
        this.treesToNotCut = treesToCut;
        this.hutLocation = home;
        this.colony = colony;
        this.boxCenter = null;
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world            the world within which to path.
     * @param start            the start position from which to path from.
     * @param home             the position of the worker hut.
     * @param startRestriction start of the restricted area.
     * @param endRestriction   end of the restricted area.
     * @param treesToCut       the trees the lj is supposed to cut.
     * @param entity           the entity.
     * @param colony           the colony.
     */
    public PathJobFindTree(
      final World world,
      @NotNull final BlockPos start,
      final BlockPos home,
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final BlockPos furthestRestriction,
      final List<ItemStorage> treesToCut,
      final IColony colony,
      final LivingEntity entity)
    {
        super(world,
            start,
            startRestriction,
            endRestriction,
            (int) Math.sqrt(BlockPosUtil.getDistanceSquared2D(home, furthestRestriction) * 1.5),
            AREA_SHRINK,
            false,
            new TreePathResult(),
            entity);
        this.treesToNotCut = treesToCut;
        this.hutLocation = home;
        this.colony = colony;

        final BlockPos size = startRestriction.subtract(endRestriction);
        this.boxCenter = endRestriction.add(size.getX()/2, size.getY()/2, size.getZ()/2);
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
        return boxCenter == null ? pos.distanceSq(hutLocation) * TIE_BREAKER : BlockPosUtil.getDistanceSquared2D(pos, boxCenter);
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
            return isTree(n.pos.add(dx, 0, 0)) || isTree(n.pos.add(0, 0, -1)) || isTree(n.pos.add(0, 0, 1));
        }
    }

    private boolean isTree(final BlockPos pos)
    {
        if (Tree.checkTree(world, pos, treesToNotCut) && Tree.checkIfInColonyAndNotInBuilding(pos, colony))
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
    protected boolean isPassable(@NotNull final BlockState block, final BlockPos pos)
    {
        return super.isPassable(block, pos) || (block.isIn(BlockTags.LEAVES) && isInRestrictedArea(pos)) || Compatibility.isDynamicTrunkShell(block.getBlock());
    }
}
