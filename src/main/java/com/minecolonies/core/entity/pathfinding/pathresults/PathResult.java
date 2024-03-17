package com.minecolonies.core.entity.pathfinding.pathresults;

import com.minecolonies.api.util.Log;
import com.minecolonies.core.entity.pathfinding.PathFindingStatus;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Creates a pathResult of a certain path.
 */
public class PathResult<T extends Callable<Path>>
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
     * Starts the job by queing it to an executor
     *
     * @param executorService executor
     */
    public void startJob(final ExecutorService executorService)
    {
        if (job != null)
        {
            pathCalculation = executorService.submit(job);
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
}
