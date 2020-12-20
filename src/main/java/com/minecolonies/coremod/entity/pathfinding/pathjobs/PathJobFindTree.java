package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.pathfinding.TreePathResult;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import com.minecolonies.coremod.entity.pathfinding.Node;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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
      final List<ItemStorage> treesToCut,
      final IColony colony,
      final LivingEntity entity)
    {
        super(world, startRestriction, endRestriction, new TreePathResult(), entity);
        this.treesToNotCut = treesToCut;
        this.hutLocation = home;
        this.colony = colony;
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

        if (xzRestricted)
        {
            if ((pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ))
            {
                return TIE_BREAKER;
            }
            else
            {
                return pos.distanceSq(new Vec3i(pos.getX() > maxX ? maxX : minX, pos.getY(), pos.getZ() > maxZ ? maxZ : minZ));
            }
        }

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * TIE_BREAKER;
    }

    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        if (xzRestricted && (n.pos.getX() < minX || n.pos.getX() > maxX || n.pos.getZ() < minZ || n.pos.getZ() > maxZ))
        {
            return false;
        }

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
        if (!xzRestricted || (n.pos.getX() >= minX && n.pos.getX() <= maxX && n.pos.getZ() >= minZ && n.pos.getZ() <= maxZ))
        {
            return 0;
        }
        else if (xzRestricted)
        {
            if (n.parent != null)
            {
                final int x = n.pos.getX() > maxX ? maxX : minX;
                final int z = n.pos.getZ() > maxZ ? maxZ : minZ;
                final double parentDist = n.parent.pos.distanceSq(new Vec3i(x, n.parent.pos.getY(), z));
                final double currDist = n.pos.distanceSq(new Vec3i(x, n.pos.getY(), z));
                if (parentDist <= currDist)
                {
                    return currDist;
                }
                return 0.1;
            }
        }

        return 0;
    }

    @Override
    protected boolean isPassable(@NotNull final BlockState block, final BlockPos pos)
    {
        return super.isPassable(block, pos) || block.getMaterial() == Material.LEAVES || Compatibility.isDynamicTrunkShell(block.getBlock());
    }
}
