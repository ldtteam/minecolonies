package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.api.entity.pathfinding.TreePathResult;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    private final List<ItemStorage> treesToNotCut;

    /**
     * The Colony the tree is in.
     */
    private final IColony colony;

    private BlockPos startRestriction = null;
    private BlockPos endRestriction = null;

    public void setAreaRestriction(final BlockPos start, final BlockPos end)
    {
        this.startRestriction = start;
        this.endRestriction = end;
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world      the world within which to path.
     * @param start      the start position from which to path from.
     * @param home       the position of the worker hut.
     * @param range      maximum path range.
     * @param treesToCut the trees the lj is supposed to cut.
     * @param entity the entity.
     */
    public PathJobFindTree(
                            final World world,
                            @NotNull final BlockPos start,
                            final BlockPos home,
                            final int range,
                            final List<ItemStorage> treesToCut,
                            final IColony colony,
                            final EntityLivingBase entity)
    {
        super(world, start, start, range, new TreePathResult(), entity);
        this.treesToNotCut = treesToCut;
        this.hutLocation = home;
        this.colony = colony;
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world      the world within which to path.
     * @param start      the start position from which to path from.
     * @param home       the position of the worker hut.
     * @param startRestriction    start of the restricted area.
     * @param endRestriction      end of the restricted area.
     * @param treesToCut the trees the lj is supposed to cut.
     * @param entity the entity.
     */
    public PathJobFindTree(
            final World world,
            @NotNull final BlockPos start,
            final BlockPos home,
            final BlockPos startRestriction,
            final BlockPos endRestriction,
            final List<ItemStorage> treesToCut,
            final Colony colony,
            final EntityLivingBase entity)
    {
        super(world, startRestriction, endRestriction, new TreePathResult(), entity);
        this.treesToNotCut = treesToCut;
        this.hutLocation = home;
        this.colony = colony;
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
        if (Tree.checkTree(world, pos, treesToNotCut) && Tree.checkIfInColonyAndNotInBuilding(pos, colony))
        {
            if (startRestriction != null && endRestriction != null) {
                // check block pos is inside restricted area

                final int posX = pos.getX();

                final int maxX = Math.max(startRestriction.getX(), endRestriction.getX());
                if (posX > maxX)
                {
                    return false;
                }

                final int minX = Math.min(startRestriction.getX(), endRestriction.getX());
                if (posX < minX)
                {
                    return false;
                }

                final int posZ = pos.getZ();

                final int maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ());
                if (posZ > maxZ)
                {
                    return false;
                }

                final int minZ = Math.min(startRestriction.getZ(), endRestriction.getZ());
                if (posZ < minZ)
                {
                    return false;
                }

                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "Found tree with restrictions!");
            }

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
        return super.isPassable(block) || block.getMaterial() == Material.LEAVES || Compatibility.isDynamicTrunkShell(block.getBlock());
    }
}
