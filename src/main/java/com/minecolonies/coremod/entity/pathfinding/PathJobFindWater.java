package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.entity.LivingEntity;
import com.minecolonies.api.entity.pathfinding.WaterPathResult;
import com.minecolonies.api.util.Pond;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Find and return a path to the nearest water.
 * Created: March 25, 2016
 */
public class PathJobFindWater extends AbstractPathJob
{
    private static final int MIN_DISTANCE = 40;
    private static final int MAX_RANGE    = 250;
    private final BlockPos            hutLocation;
    @NotNull
    private final ArrayList<BlockPos> ponds;

    /**
     * AbstractPathJob constructor.
     *
     * @param world the world within which to path.
     * @param start the start position from which to path from.
     * @param home  the position of the worker hut.
     * @param range maximum path range.
     * @param ponds already visited fishing places.
     * @param entity the entity.
     */
    PathJobFindWater(final World world, @NotNull final BlockPos start, final BlockPos home, final int range, @NotNull final List<BlockPos> ponds, final LivingEntity entity)
    {
        super(world, start, start, range, new WaterPathResult(), entity);
        this.ponds = new ArrayList<>(ponds);
        hutLocation = home;
    }

    private static double squareDistance(@NotNull final BlockPos currentPond, @NotNull final BlockPos nextPond)
    {
        return currentPond.distanceSq(nextPond.getX(), nextPond.getY(), nextPond.getZ());
    }

    @NotNull
    @Override
    public WaterPathResult getResult()
    {
        return (WaterPathResult) super.getResult();
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
    protected boolean isAtDestination(@NotNull final Node n)
    {
        if (n.parent == null)
        {
            return false;
        }

        if (squareDistance(hutLocation, n.pos) > MAX_RANGE)
        {
            return false;
        }

        if (n.pos.getX() == n.parent.pos.getX())
        {
            final int dz = n.pos.getZ() > n.parent.pos.getZ() ? 1 : -1;
            return isWater(n.pos.add(0, -1, dz)) || isWater(n.pos.add(-1, -1, 0)) || isWater(n.pos.add(1, -1, 0));
        }
        else
        {
            final int dx = n.pos.getX() > n.parent.pos.getX() ? 1 : -1;
            return isWater(n.pos.add(dx, -1, 0)) || isWater(n.pos.add(0, -1, -1)) || isWater(n.pos.add(0, -1, 1));
        }
    }

    /**
     * Checks if a certain location is water.
     *
     * @param newPond the location.
     * @return true if so.
     */
    private boolean isWater(@NotNull final BlockPos newPond)
    {
        if (ponds.contains(newPond) || pondsAreNear(ponds, newPond))
        {
            return false;
        }

        @Nullable final Pond pond = Pond.createWater(world, newPond);

        if (pond != null)
        {
            getResult().pond = newPond;
            getResult().isEmpty = ponds.isEmpty();
            return true;
        }

        return false;
    }

    /**
     * Creates the distance to calculate it in a stream.
     *
     * @param range   the range.
     * @param newPond the pond.
     * @return a predicate of the position.
     */
    private static Predicate<BlockPos> generateDistanceFrom(final int range, @NotNull final BlockPos newPond)
    {
        return pond -> squareDistance(pond, newPond) < range;
    }

    /**
     * Checks if there are close ponds to a position.
     *
     * @param ponds   all ponds.
     * @param newPond the position.
     * @return true if so.
     */
    private static boolean pondsAreNear(@NotNull final ArrayList<BlockPos> ponds, @NotNull final BlockPos newPond)
    {
        if (ponds.isEmpty())
        {
            return false;
        }
        @NotNull final Predicate<BlockPos> compare = generateDistanceFrom(MIN_DISTANCE, newPond);
        return ponds.stream().anyMatch(compare);
    }

    @Override
    protected double getNodeResultScore(final Node n)
    {
        return 0;
    }
}

