package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Path job to find a path between buildings
 */
public class PathJobPathway extends AbstractPathJob
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

    public PathJobPathway(
      final int colonyID,
      final List<IBuilding> buildings,
      final Level world,
      @NotNull final BlockPos start, final BlockPos end, final int range, final EntityCitizen citizen)
    {
        super(world, start, end, range, citizen);
        this.colonyid = colonyID;
        this.buildings = buildings;
        setPathingOptions(new PathingOptions().withJumpCost(100).withStartSwimCost(30).withSwimCost(5).withCanSwim(true).withCanEnterDoors(true));
    }

    @Override
    protected double computeHeuristic(final BlockPos pos)
    {
        final LevelChunk chunk = (LevelChunk) world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        if (ColonyUtils.getOwningColony(chunk) == colonyid)
        {
            return Math.sqrt(end.distSqr(pos)) / (ColonyUtils.getAllClaimingBuildings(chunk).size() + 1);
        }

        return Math.sqrt(end.distSqr(pos));
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        return (end.distSqr(n.pos) < 5 * 5);
    }

    @Override
    protected double getNodeResultScore(final MNode n)
    {
        final double dist = Math.sqrt(end.distSqr(n.pos));
        if (dist < 15)
        {
            return n.getCost();
        }

        return dist;
    }

    @Override
    protected boolean isPassable(final BlockPos pos, final boolean head, final MNode currentnode)
    {
        if (super.isPassable(pos, head, currentnode))
        {
            for (final IBuilding building : buildings)
            {
                if (BlockPosUtil.isInArea(building.getCorners().getA(), building.getCorners().getB(), pos)
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
    protected double calcAdditionalCost(final double stepCost, final MNode parent, final BlockPos pos, final BlockState state)
    {
        if (parent.parent != null && parent.pos.getX() == parent.parent.pos.getX() && pos.getX() != parent.pos.getX())
        {
            return stepCost * 10;
        }

        if (parent.parent != null && parent.pos.getZ() == parent.parent.pos.getZ() && pos.getZ() != parent.pos.getZ())
        {
            return stepCost * 10;
        }

        return stepCost;
    }
}
