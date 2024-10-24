package com.minecolonies.api.entity.pathfinding;

import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.concurrent.Callable;

/**
 * Interface for path jobs
 */
public interface IPathJob extends Callable<Path>
{
    /**
     * Get the path result holder for this job
     * @return
     */
    PathResult getResult();

    /**
     * Get the pathing options used for this job
     * @return
     */
    public PathingOptions getPathingOptions();

    Mob getEntity();

    Level getActualWorld();

    BlockPos getStart();
}
