package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.colony.events.raid.RaidManager;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.BlockPosUtil.HORIZONTAL_DIRS;

/**
 * Special raider pathfinding, can go through blocks and place ladders, is finished when reaching close to the intended spawn and is a legit spawn point.
 */
public class PathJobRaiderPathing extends AbstractPathJob implements IDestinationPathJob
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
      @NotNull final BlockPos start, final BlockPos targetSpawnPoint)
    {
        super(world, start, targetSpawnPoint, new PathResult<PathJobRaiderPathing>(), null);
        this.buildings = buildings;
        direction = targetSpawnPoint;
        maxNodes = 10000;
        setPathingOptions(new PathingOptions().withJumpCost(1).withStartSwimCost(1).withSwimCost(1).withCanSwim(true).withCanEnterDoors(true));
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(direction, x, y, z);
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        if (BlockPosUtil.distSqr(start, n.x, n.y, n.z) < 50 * 50)
        {
            return false;
        }

        return (BlockPosUtil.distSqr(direction, n.x, n.y, n.z) < 50 * 50) && RaidManager.isValidSpawnPoint(buildings, tempWorldPos.set(n.x, n.y, n.z))
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                      == SurfaceType.WALKABLE;
    }

    @Override
    protected boolean isPassable(final int x, final int y, final int z, final boolean head, final MNode currentnode)
    {
        return true;
    }

    @Override
    protected void visitNode(final MNode node)
    {
        super.visitNode(node);

        if (!node.isSwimming())
        {
            exploreInDirection(node, 0, -1, 0);
        }

        if (SurfaceType.getSurfaceType(cachedBlockLookup,
          cachedBlockLookup.getBlockState(node.x, node.y, node.z),
          tempWorldPos.set(node.x, node.y, node.z)) == SurfaceType.WALKABLE)
        {
            exploreInDirection(node, 0, 1, 0);
            return;
        }

        int dX = 0;
        int dY = 0;
        int dZ = 0;

        if (node.parent != null)
        {
            dX = node.x - node.parent.x;
            dY = node.y - node.parent.y;
            dZ = node.z - node.parent.z;
        }

        if (dY >= 0 || dX != 0 || dZ != 0)
        {
            for (final Direction dir : HORIZONTAL_DIRS)
            {
                final BlockState toPlace = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, dir.getOpposite());
                if (BlockUtils.isAnySolid(cachedBlockLookup.getBlockState(node.x + dir.getStepX(), node.y + dir.getStepY(), node.z + dir.getStepZ()))
                      && toPlace.canSurvive(world, tempWorldPos.set(node.x, node.y, node.z)))
                {
                    exploreInDirection(node, 0, 1, 0);
                    return;
                }
            }
        }
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
                   && SurfaceType.getSurfaceType(cachedBlockLookup, cachedBlockLookup.getBlockState(x, y - 1, z), tempWorldPos.set(x, y - 1, z), getPathingOptions())
                        == SurfaceType.WALKABLE)
        {
            addCost = 3.5;
            return y;
        }

        return height;
    }

    @Override
    protected double modifyCost(
      final double cost,
      final MNode parent,
      final boolean swimstart,
      final boolean swimming,
      final int x,
      final int y,
      final int z,
      final BlockState state, final BlockState below)
    {
        double modifier = addCost;
        addCost = 1.0;
        if (!super.isPassable(x, y, z, false, null))
        {
            modifier *= THROUGH_BLOCK_COST;
        }

        if (SurfaceType.getSurfaceType(cachedBlockLookup, cachedBlockLookup.getBlockState(x, y - 1, z), tempWorldPos.set(x, y - 1, z), getPathingOptions()) != SurfaceType.WALKABLE)
        {
            modifier *= THROUGH_BLOCK_COST;
        }

        return cost * modifier;
    }

    @Override
    public BlockPos getDestination()
    {
        return direction;
    }
}
