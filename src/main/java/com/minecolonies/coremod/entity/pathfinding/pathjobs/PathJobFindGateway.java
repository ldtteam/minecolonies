package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.GatePathResult;
import com.minecolonies.api.entity.pathfinding.WaterPathResult;
import com.minecolonies.api.util.Pond;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Find and return a path to the nearest gateway.
 */
public class PathJobFindGateway extends AbstractPathJob
{
    private static final int                                  MIN_DISTANCE = 40;
    private static final int                                  MAX_RANGE    = 300*300;
    private final        BlockPos                             hutLocation;
    @NotNull
    private final        ArrayList<BlockPos> gates;

    /**
     * AbstractPathJob constructor.
     *  @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param home   the position of the worker hut.
     * @param range  maximum path range.
     * @param gates  all available gates.
     * @param entity the entity.
     */
    public PathJobFindGateway(
      final Level world,
      @NotNull final BlockPos start,
      final BlockPos home,
      final int range,
      final Set<BlockPos> gates,
      final LivingEntity entity)
    {
        super(world, start, start, range, new GatePathResult(), entity);
        this.gates = new ArrayList<>(gates);
        hutLocation = home;
    }

    private static double squareDistance(@NotNull final BlockPos currentGate, @NotNull final BlockPos nextGate)
    {
        return currentGate.distSqr(nextGate);
    }

    @NotNull
    @Override
    public GatePathResult getResult()
    {
        return (GatePathResult) super.getResult();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        final int dx = pos.getX() - hutLocation.getX();
        final int dy = pos.getY() - hutLocation.getY();
        final int dz = pos.getZ() - hutLocation.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 0.501D;
    }

    //Overrides the Superclass in order to find only ponds of water with follow the wished conditions
    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        if (squareDistance(hutLocation, n.pos) > MAX_RANGE)
        {
            return false;
        }

        if (isGate(n))
        {
            getResult().gate = n.pos;
            getResult().parent = Objects.requireNonNull(n.parent).pos;
            getResult().isEmpty = gates.isEmpty();
            return true;
        }

        return false;
    }

    /**
     * Checks if a certain location is a gate.
     *
     * @param n the location.
     * @return true if so.
     */
    private boolean isGate(@NotNull final MNode n)
    {
        if (n.parent == null)
        {
            return false;
        }

        return gates.contains(n.pos);
    }

    @Override
    protected double getNodeResultScore(final MNode n)
    {
        return 0;
    }
}

