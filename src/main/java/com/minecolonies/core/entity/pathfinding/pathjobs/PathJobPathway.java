package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Path job to find a path between buildings
 */
public class PathJobPathway extends AbstractPathJob implements IDestinationPathJob
{
    /**
     * Buildings to avoid
     */
    private final List<IBuilding> buildings;

    private int colonyid = -1;

    /**
     * Additional cost multiplier
     */
    private double addCost = 1.0;

    /**
     * The destination pos
     */
    private final BlockPos end;

    public PathJobPathway(
      final int colonyID,
      final List<IBuilding> buildings,
      final Level world,
      @NotNull final BlockPos start, final BlockPos end, final EntityCitizen citizen)
    {
        super(world, start, end, new PathResult<PathJobPathway>(), citizen);
        this.colonyid = colonyID;
        this.buildings = buildings;
        this.end = end;
        setPathingOptions(new PathingOptions().withJumpCost(30).withStartSwimCost(30).withSwimCost(5).withCanSwim(true).withCanEnterDoors(true));
    }

    // TODO: Before usage not thread safe chunk/cap access. Should be using passed along info
    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        final LevelChunk chunk = (LevelChunk) world.getChunk(x >> 4, z >> 4);
        if (ColonyUtils.getOwningColony(chunk) == colonyid)
        {
            return Math.sqrt(BlockPosUtil.distSqr(end.getX(), end.getY(), end.getZ(), x, y, z)) / (ColonyUtils.getAllClaimingBuildings(chunk).size() + 1);
        }

        return Math.sqrt(BlockPosUtil.distSqr(end.getX(), end.getY(), end.getZ(), x, y, z));
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        return BlockPosUtil.distSqr(end, n.x, n.y, n.z) < 5 * 5;
    }

    @Override
    protected double getEndNodeScore(final MNode n)
    {
        final double dist = BlockPosUtil.dist(end, n.x, n.y, n.z);
        if (dist < 15)
        {
            return n.getCost();
        }

        return dist;
    }

    @Override
    protected boolean isPassable(final int x, final int y, final int z, final boolean head, final MNode currentnode)
    {
        if (super.isPassable(x, y, z, head, currentnode))
        {
            for (final IBuilding building : buildings)
            {
                if (BlockPosUtil.isInArea(building.getCorners().getA(), building.getCorners().getB(), tempWorldPos.set(x, y, z))
                      && !BlockPosUtil.isInArea(building.getCorners().getA(), building.getCorners().getB(), end)
                      && !BlockPosUtil.isInArea(building.getCorners().getA(), building.getCorners().getB(), start))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    protected double modifyCost(
      final double stepCost,
      final MNode parent,
      final boolean swimstart,
      final boolean swimming,
      final int x,
      final int y,
      final int z,
      final BlockState state, final BlockState below)
    {
        if (parent.parent != null && parent.x == parent.parent.x && x != parent.x)
        {
            return stepCost * 10;
        }

        if (parent.parent != null && parent.z == parent.parent.z && z != parent.z)
        {
            return stepCost * 10;
        }

        return stepCost;
    }

    @Override
    public BlockPos getDestination()
    {
        return end;
    }
}
