package com.minecolonies.entity.pathfinding;

public class PathResult
{
    protected volatile Status  status                 = Status.IN_PROGRESS_COMPUTING;
    protected volatile boolean pathReachesDestination = false;
    protected volatile int     pathLength             = 0;

    public PathResult() {}

    /**
     * Get Status of the Path
     *
     * @return status
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * For PathNavigate and AbstractPathJob use only
     *
     * @param s status to set
     */
    public void setStatus(Status s)
    {
        status = s;
    }

    /**
     * @return true if the path is still computing or being followed
     */
    public boolean isInProgress() { return isComputing() || status == Status.IN_PROGRESS_FOLLOWING; }

    public boolean isComputing() { return status == Status.IN_PROGRESS_COMPUTING; }

    /**
     * @return true if the no path can be found
     */
    public boolean failedToReachDestination()
    {
        return !isComputing() && !getPathReachesDestination();
    }

    /**
     * @return true if the path is computed, and it reaches a desired destination
     */
    public boolean getPathReachesDestination() { return pathReachesDestination; }

    /**
     * For PathNavigate and AbstractPathJob use only.
     *
     * @param value new value for pathReachesDestination.
     */
    public void setPathReachesDestination(boolean value) { pathReachesDestination = value; }

    /**
     * @return true if the path was cancelled before being computed or before the entity reached it's destination
     */
    public boolean isCancelled() { return status == Status.CANCELLED; }

    /**
     * @return length of the compute path, in nodes
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
    public void setPathLength(int l) { pathLength = l; }

    /**
     * @return true if the path moves from the current location, useful for checking if a path actually generated
     */
    public boolean didPathGenerate()
    {
        return pathLength > 0;
    }

    enum Status
    {
        IN_PROGRESS_COMPUTING,
        IN_PROGRESS_FOLLOWING,
        COMPLETE,
        CANCELLED
    }
}
