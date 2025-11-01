package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.PathingConstants.DEBUG_VERBOSITY_NONE;

/**
 * Job that handles moving away from something.
 */
public class PathJobEscapeWater extends AbstractPathJob implements IDestinationPathJob
{
    /**
     * Position to run to, in order to avoid something.
     */
    @NotNull
    protected final BlockPos avoid;

    /**
     * The blockposition we're trying to move away to
     */
    private BlockPos preferredDirection;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world  world the entity is in.
     * @param start  starting location.
     * @param range  max range to search.
     * @param entity the entity.
     */
    public PathJobEscapeWater(
      final Level world,
      @NotNull final BlockPos start,
      final int range,
      final Mob entity)
    {
        super(world, start, 500, new PathResult<PathJobEscapeWater>(), entity);

        this.avoid = start;
        preferredDirection = entity.blockPosition().offset(entity.blockPosition().subtract(avoid).multiply(range));
        if (entity instanceof AbstractEntityCitizen)
        {
            final IColony colony = ((AbstractEntityCitizen) entity).getCitizenColonyHandler().getColonyOrRegister();
            if (colony != null)
            {
                preferredDirection = colony.getCenter();
            }
        }
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    @Override
    protected Path search()
    {
        if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] away from [%d,%d,%d]",
              start.getX(), start.getY(), start.getZ(), avoid.getX(), avoid.getY(), avoid.getZ()));
        }

        return super.search();
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight.
     *
     * @return heuristic as a double - Manhatten Distance with tie-breaker.
     */
    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.dist(preferredDirection, x, y, z) * 2;
    }

    /**
     * Checks if the destination has been reached. Meaning that the avoid distance has been reached.
     *
     * @param n Node to test.
     * @return true if so.
     */
    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        return cachedBlockLookup.getBlockState(n.x, n.y, n.z).isAir() && cachedBlockLookup.getBlockState(n.x, n.y + 1, n.z).isAir()
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                      == SurfaceType.WALKABLE;
    }

    @Override
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        super.setPathingOptions(pathingOptions);
        getPathingOptions().setWalkUnderWater(true);
        getPathingOptions().swimCost = 1;
        getPathingOptions().swimCostEnter = 1;
        getPathingOptions().nonLadderClimbableCost = 1;
    }

    @Override
    public BlockPos getDestination()
    {
        return preferredDirection;
    }
}
