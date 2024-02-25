package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.colony.managers.RaidManager;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.pathfinding.PathingStuckHandler.HORIZONTAL_DIRS;

/**
 * Special raider pathfinding, can go through blocks and place ladders, is finished when reaching close to the intended spawn and is a legit spawn point.
 */
public class PathJobRaiderPathing extends AbstractPathJob
{
    /**
     * Cost for moving through a block
     */
    private final double THROUGH_BLOCK_COST = 30;

    /**
     * Buildings to avoid
     */
    private final List<IBuilding> buildings;

    /**
     * Targeted position
     */
    private final BlockPos direction;

    /**
     * Additional cost multiplier
     */
    private double addCost = 1.0;

    public PathJobRaiderPathing(
      final List<IBuilding> buildings,
      final Level world,
      @NotNull final BlockPos start, final BlockPos targetSpawnPoint, final int range)
    {
        super(world, start, targetSpawnPoint, range, null);
        this.buildings = buildings;
        direction = targetSpawnPoint;
        setPathingOptions(new PathingOptions().withJumpCost(1).withStartSwimCost(1).withSwimCost(1).withCanSwim(true).withCanEnterDoors(true));
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.dist(direction, x, y, z);
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        if (BlockPosUtil.distSqr(start, n.x, n.y, n.z) < 50 * 50)
        {
            return false;
        }

        return (BlockPosUtil.distSqr(direction, n.x, n.y, n.z) < 50 * 50) && RaidManager.isValidSpawnPoint(buildings, tempWorldPos.set(n.x, n.y, n.z));
    }

    @Override
    protected double getNodeResultScore(final MNode n)
    {
        return BlockPosUtil.dist(direction, n.x, n.y, n.z);
    }

    @Override
    protected boolean isPassable(final int x, final int y, final int z, final boolean head, final MNode currentnode)
    {
        return true;
    }

    @Override
    protected boolean onLadderGoingDown(@NotNull final MNode currentNode, final int dX, final int dY, final int dZ)
    {
        return !currentNode.isSwimming();
    }

    @Override
    protected boolean onLadderGoingUp(@NotNull final MNode currentNode, final int dX, final int dY, final int dZ)
    {
        if (SurfaceType.getSurfaceType(cachedBlockLookup,
          cachedBlockLookup.getBlockState(currentNode.x, currentNode.y, currentNode.z),
          tempWorldPos.set(currentNode.x, currentNode.y, currentNode.z)) == SurfaceType.WALKABLE)
        {
            return true;
        }

        if (dY >= 0 || dX != 0 || dZ != 0)
        {
            if (currentNode.isLadder())
            {
                return true;
            }

            for (final Direction dir : HORIZONTAL_DIRS)
            {
                final BlockState toPlace = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, dir.getOpposite());
                if (BlockUtils.isAnySolid(cachedBlockLookup.getBlockState(currentNode.x + dir.getStepX(), currentNode.y + dir.getStepY(), currentNode.z + dir.getStepZ()))
                      && Blocks.LADDER.canSurvive(toPlace, world, tempWorldPos.set(currentNode.x, currentNode.y, currentNode.z)))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected int getGroundHeight(final MNode parent, final int x, final int y, final int z)
    {
        final int height = super.getGroundHeight(parent, x, y, z);
        if (height != y)
        {
            addCost = 0.5;
        }

        if ((parent.x - x == 0 && parent.z - z == 0)
              || (Math.abs(height - y) > 1)
                   && SurfaceType.getSurfaceType(cachedBlockLookup, cachedBlockLookup.getBlockState(x, y - 1, z), tempWorldPos.set(x, y - 1, z)) == SurfaceType.WALKABLE)
        {
            addCost = 3.5;
            return y;
        }

        return height;
    }

    @Override
    protected double computeCost(
      final int dX, final int dY, final int dZ,
      final boolean isSwimming,
      final boolean onPath,
      final boolean onRails,
      final boolean railsExit,
      final boolean swimStart,
      final boolean corner,
      final BlockState state,
      final int x, final int y, final int z)
    {
        double modifier = addCost;
        addCost = 1.0;
        if (!super.isPassable(x, y, z, false, null))
        {
            modifier *= THROUGH_BLOCK_COST;
        }

        if (!corner && SurfaceType.getSurfaceType(cachedBlockLookup, cachedBlockLookup.getBlockState(x, y - 1, z), tempWorldPos.set(x, y - 1, z)) != SurfaceType.WALKABLE)
        {
            modifier *= THROUGH_BLOCK_COST;
        }

        return super.computeCost(dX, dY, dZ, isSwimming, onPath, onRails, railsExit, swimStart, corner, state, x, y, z) * modifier;
    }
}
