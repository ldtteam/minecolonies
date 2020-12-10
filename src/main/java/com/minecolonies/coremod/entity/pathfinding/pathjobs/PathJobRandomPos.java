package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.RandomPathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.pathfinding.Node;
import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.PathingConstants.DEBUG_VERBOSITY_NONE;

/**
 * Job that handles random pathing.
 */
public class PathJobRandomPos extends AbstractPathJob
{
    /**
     * Direction to walk to.
     */
    @NotNull
    protected final BlockPos destination;

    /**
     * Required avoidDistance.
     */
    protected final int distance;

    /**
     * Random pathing rand.
     */
    private static Random random = new Random();

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world         world the entity is in.
     * @param start         starting location.
     * @param distance how far to move away.
     * @param range         max range to search.
     * @param entity        the entity.
     */
    public PathJobRandomPos(
      final World world,
      @NotNull final BlockPos start,
      final int distance,
      final int range,
      final LivingEntity entity)
    {
        super(world, start, start, range, new RandomPathResult(), entity);
        this.distance = distance;

        final Tuple<Direction, Direction> dir = BlockPosUtil.getRandomDirectionTuple(random);
        this.destination = start.offset(dir.getA(), distance).offset(dir.getB(), distance);
    }

    @Nullable
    @Override
    protected Path search()
    {
        if (MineColonies.getConfig().getCommon().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] in the direction of [%d,%d,%d]",
              start.getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()));
        }

        return super.search();
    }

    @NotNull
    @Override
    public RandomPathResult getResult()
    {
        return (RandomPathResult) super.getResult();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return Math.sqrt(destination.distanceSq(new BlockPos(pos.getX(), destination.getY(), pos.getZ())));
    }

    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        if (Math.sqrt(start.distanceSq(n.pos)) > distance && isWalkableSurface(world.getBlockState(n.pos.down()), n.pos.down()) == SurfaceType.WALKABLE)
        {
            getResult().randomPos = n.pos;
            return true;
        }
        return false;
    }

    @Override
    protected double getNodeResultScore(@NotNull final Node n)
    {
        //  For Result Score lower is better
        return destination.distanceSq(n.pos);
    }
}
