package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.coremod.colony.managers.RaidManager;
import com.minecolonies.coremod.entity.pathfinding.MNode;
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
    protected double computeHeuristic(final BlockPos pos)
    {
        return Math.sqrt(direction.distSqr(pos));
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        if (start.distSqr(n.pos) < 50 * 50)
        {
            return false;
        }

        return (direction.distSqr(n.pos) < 50 * 50) && RaidManager.isValidSpawnPoint(buildings, n.pos);
    }

    @Override
    protected double getNodeResultScore(final MNode n)
    {
        return Math.sqrt(direction.distSqr(n.pos));
    }

    @Override
    protected boolean isPassable(final BlockPos pos, final boolean head, final MNode currentnode)
    {
        return true;
    }

    @Override
    protected boolean onLadderGoingDown(@NotNull final MNode currentNode, @NotNull final BlockPos dPos)
    {
        return !currentNode.isSwimming();
    }

    @Override
    protected boolean onLadderGoingUp(@NotNull final MNode currentNode, @NotNull final BlockPos dPos)
    {
        if (SurfaceType.getSurfaceType(world, world.getBlockState(currentNode.pos), currentNode.pos) == SurfaceType.WALKABLE)
        {
            return true;
        }

        if (dPos.getY() >= 0 || dPos.getX() != 0 || dPos.getZ() != 0)
        {
            if (currentNode.isLadder())
            {
                return true;
            }

            for (final Direction dir : HORIZONTAL_DIRS)
            {
                final BlockState toPlace = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, dir.getOpposite());
                if (world.getBlockState(currentNode.pos.relative(dir)).getMaterial().isSolid() && Blocks.LADDER.canSurvive(toPlace, world, currentNode.pos))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected int getGroundHeight(final MNode parent, @NotNull final BlockPos pos)
    {
        final int height = super.getGroundHeight(parent, pos);
        if (height != pos.getY())
        {
            addCost = 0.5;
        }

        if ((parent.pos.getX() - pos.getX() == 0 && parent.pos.getZ() - pos.getZ() == 0)
              || (Math.abs(height - pos.getY()) > 1) && SurfaceType.getSurfaceType(world, world.getBlockState(pos.below()), pos.below()) == SurfaceType.WALKABLE)
        {
            addCost = 3.5;
            return pos.getY();
        }

        return height;
    }

    @Override
    protected double computeCost(
      @NotNull final BlockPos dPos,
      final boolean isSwimming,
      final boolean onPath,
      final boolean onRails,
      final boolean railsExit,
      final boolean swimStart,
      final boolean corner,
      final BlockState state,
      final BlockPos blockPos)
    {
        double modifier = addCost;
        addCost = 1.0;
        if (!super.isPassable(blockPos, false, null))
        {
            modifier *= THROUGH_BLOCK_COST;
        }

        if (!corner && SurfaceType.getSurfaceType(world, world.getBlockState(blockPos.below()), blockPos.below()) != SurfaceType.WALKABLE)
        {
            modifier *= THROUGH_BLOCK_COST;
        }

        return super.computeCost(dPos, isSwimming, onPath, onRails, railsExit, swimStart, corner, state, blockPos) * modifier;
    }
}
