package com.minecolonies.core.entity.pathfinding.pathresults;

import com.minecolonies.api.util.Log;
import com.minecolonies.core.entity.pathfinding.PathFindingStatus;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.pathjobs.AbstractPathJob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Creates a pathResult of a certain path.
 */
public class PathResult<T extends AbstractPathJob>
{
    /**
     * The pathfinding status
     */
    protected PathFindingStatus status = PathFindingStatus.IN_PROGRESS_COMPUTING;

    /**
     * Whether the pathfinding job reached its destination
     */
    private volatile boolean pathReachesDestination = false;

    /**
     * Finished path reference
     */
    private Path path = null;

    /**
     * The calculation future for this result and job
     */
    private Future<Path> pathCalculation = null;

    /**
     * The job to execute for this result
     */
    private T job = null;

    /**
     * Whether the pathing calc is done and processed
     */
    private boolean pathingDoneAndProcessed = false;

    /**
     * Cost per distance traveled factor, only if path reaches
     */
    public double costPerDist = 1;

    /**
     * Amount of searched nodes
     */
    public int searchedNodes = 0;

    /**
     * The players getting debug information
     */
    private List<UUID> debugWatchers = null;

    /**
     * Get Status of the Path.
     *
     * @return status.
     */
    public PathFindingStatus getStatus()
    {
        return status;
    }

    /**
     * For PathNavigate and AbstractPathJob use only.
     *
     * @param s status to set.
     */
    public void setStatus(final PathFindingStatus s)
    {
        status = s;
    }

    /**
     * @return true if the path is still computing or being followed.
     */
    public boolean isInProgress()
    {
        return isComputing() || status == PathFindingStatus.IN_PROGRESS_FOLLOWING;
    }

    public boolean isComputing()
    {
        return status == PathFindingStatus.IN_PROGRESS_COMPUTING;
    }

    /**
     * @return true if the no path can be found.
     */
    public boolean failedToReachDestination()
    {
        return isDone() && !pathReachesDestination;
    }

    /**
     * @return true if the path is computed, and it reaches a desired destination.
     */
    public boolean isPathReachingDestination()
    {
        return isDone() && path != null && pathReachesDestination;
    }

    /**
     * For PathNavigate and AbstractPathJob use only.
     *
     * @param value new value for pathReachesDestination.
     */
    public void setPathReachesDestination(final boolean value)
    {
        pathReachesDestination = value;
    }

    /**
     * @return true if the path was cancelled before being computed or before the entity reached it's destination.
     */
    public boolean isCancelled()
    {
        return status == PathFindingStatus.CANCELLED;
    }

    /**
     * @return length of the compute path, in nodes.
     */
    public int getPathLength()
    {
        return path.getNodeCount();
    }

    /**
     * @return true if the path moves from the current location, useful for checking if a path actually generated.
     */
    public boolean hasPath()
    {
        return path != null;
    }

    /**
     * Get the generated path or null
     *
     * @return path
     */
    @Nullable
    public Path getPath()
    {
        return path;
    }

    /**
     * Get the queried job for the pathresult
     *
     * @return
     */
    public T getJob()
    {
        return job;
    }

    /**
     * Set the job for this result
     *
     * @param job
     */
    public void setJob(final T job)
    {
        this.job = job;
    }

    /**
     * Adds another player to the debug tracking
     *
     * @param uuid player ID
     */
    public void addTrackingPlayer(final UUID uuid)
    {
        if (uuid == null)
        {
            Log.getLogger().warn("Trying to add null uuid as tracking player");
        }

        if (debugWatchers == null)
        {
            debugWatchers = new ArrayList<>();
        }

        debugWatchers.add(uuid);

        if (job != null)
        {
            job.initDebug();
        }
    }

    /**
     * Starts the job by queing it to an executor
     *
     * @param executorService executor
     */
    public void startJob(final ExecutorService executorService)
    {
        if (job != null)
        {
            checkDebugging();
            pathCalculation = executorService.submit(job);
        }
    }

    /**
     * Checks for debug tracking
     */
    private void checkDebugging()
    {
        if (!PathfindingUtils.trackByType.isEmpty())
        {
            for (Iterator<Map.Entry<String, UUID>> iterator = PathfindingUtils.trackByType.entrySet().iterator(); iterator.hasNext(); )
            {
                final Map.Entry<String, UUID> entry = iterator.next();
                final Player player = job.getActualWorld().getPlayerByUUID(entry.getValue());
                if (player == null)
                {
                    iterator.remove();
                    continue;
                }

                // Exclude stuff thats not visible
                if (player.blockPosition().distManhattan(job.getStart()) > 400)
                {
                    continue;
                }

                if (job.getClass().getSimpleName().toLowerCase().contains(entry.getKey().toLowerCase()))
                {
                    addTrackingPlayer(entry.getValue());
                }
            }
        }

        if (job.getEntity() != null && PathfindingUtils.trackingMap.containsValue(job.getEntity().getUUID()))
        {
            for (final Map.Entry<UUID, UUID> entry : PathfindingUtils.trackingMap.entrySet())
            {
                if (entry.getValue().equals(job.getEntity().getUUID()))
                {
                    addTrackingPlayer(entry.getKey());
                }
            }
        }

        if (debugWatchers != null)
        {
            job.initDebug();
        }
    }

    /**
     * Processes the completed calculation results
     */
    public void processCalculationResults()
    {
        if (pathingDoneAndProcessed)
        {
            return;
        }

        try
        {
            path = pathCalculation.get();
            pathCalculation = null;
            setStatus(PathFindingStatus.CALCULATION_COMPLETE);
            var watchers = getDebugWatchers();
            job.syncDebug(watchers);

            if (!watchers.isEmpty())
            {
                Log.getLogger().info(" Finished pathjob:" + job + " reaches: " + path.canReach() + " path target:" + path.getTarget());
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            Log.getLogger().catching(e);
        }
    }

    /**
     * Check if we're calculating the pathfinding currently
     *
     * @return true
     */
    public boolean isCalculatingPath()
    {
        return pathCalculation != null && !pathCalculation.isDone();
    }

    /**
     * Whether the path calculation finished and was processed
     *
     * @return true if calculation is done and processed
     */
    public boolean isDone()
    {
        if (!pathingDoneAndProcessed)
        {
            if (pathCalculation != null && pathCalculation.isDone())
            {
                processCalculationResults();
                pathingDoneAndProcessed = true;
            }
        }

        return pathingDoneAndProcessed;
    }

    /**
     * Cancels the path calculation
     */
    public void cancel()
    {
        if (pathCalculation != null)
        {
            pathCalculation.cancel(true);
            pathCalculation = null;
        }

        pathingDoneAndProcessed = true;
    }

    /**
     * Gets the tracking players
     *
     * @return
     */
    public List<ServerPlayer> getDebugWatchers()
    {
        final List<ServerPlayer> newList = new ArrayList<>();

        if (job != null && debugWatchers != null)
        {
            for (final UUID playerID : debugWatchers)
            {
                final Player player = job.getActualWorld().getPlayerByUUID(playerID);
                if (player instanceof ServerPlayer serverPlayer)
                {
                    newList.add(serverPlayer);
                }
            }
        }

        return newList;
    }
}
