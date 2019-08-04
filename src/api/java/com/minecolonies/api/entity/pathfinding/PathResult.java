package com.minecolonies.api.entity.pathfinding;

/**
 * Creates a pathResult of a certain path.
 */
public class PathResult
{
    protected volatile PathFindingStatus status                 = PathFindingStatus.IN_PROGRESS_COMPUTING;
    protected volatile boolean           pathReachesDestination = false;
    protected volatile int               pathLength             = 0;

    /**
     * Public constructor of the path result.
     */
    public PathResult()
    {
        /**
         * Intentionally left empty.
         */
    }

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
        return !isComputing() && !isPathReachingDestination();
    }

    /**
     * @return true if the path is computed, and it reaches a desired destination.
     */
    public boolean isPathReachingDestination()
    {
        return pathReachesDestination;
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
        return pathLength;
    }

    /**
     * For PathNavigate use only.
     *
     * @param l new value for pathLength.
     */
    public void setPathLength(final int l)
    {
        pathLength = l;
    }

    /**
     * @return true if the path moves from the current location, useful for checking if a path actually generated.
     */
    public boolean didPathGenerate()
    {
        return pathLength > 0;
    }
}
