package com.minecolonies.api.entity.pathfinding;

import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the Navigator used by minecolonies entities
 */
public interface IMinecoloniesNavigator
{
    /**
     * Sets a new pathjob to execute
     *
     * @param job             to run
     * @param dest
     * @param speedFactor
     * @param safeDestination
     * @param <T>
     * @return null or new pathresult
     */
    @Nullable
    <T extends AbstractPathJob> PathResult<T> setPathJob(
        @NotNull AbstractPathJob job,
        BlockPos dest,
        double speedFactor, boolean safeDestination);

    /**
     * Indirectly triggers a recalulation, by marking the navigator as done
     */
    void recalc();

    /**
     * Returns the pathresult holding the current pathing task and result
     *
     * @return
     */
    PathResult getPathResult();

    /**
     * Gets the safe destination the entity wants to travel to
     *
     * @return
     */
    BlockPos getSafeDestination();

    /**
     * Gets the entity of the navigator
     *
     * @return
     */
    Mob getOurEntity();

    /**
     * Pauses the navigator for X ticks from starting any new pathing tasks
     *
     * @param pauseTicks
     */
    void setPauseTicks(int pauseTicks);

    /**
     * Returns the stuck handler used by the navigator
     *
     * @return
     */
    IStuckHandler<MinecoloniesAdvancedPathNavigate> getStuckHandler();
}
