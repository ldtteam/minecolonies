package com.minecolonies.entity.pathfinding;

import net.minecraft.nbt.NBTTagCompound;

public class PathResult
{
    enum Status
    {
        IN_PROGRESS_COMPUTING,
        IN_PROGRESS_FOLLOWING,
        COMPLETE,
        CANCELLED
    }

    protected volatile Status status = Status.IN_PROGRESS_COMPUTING;
    protected volatile boolean pathReachesDestination = false;
    protected volatile int pathLength = 0;

    protected NBTTagCompound data;

    public PathResult() {}

    /**
     * For PathNavigate and PathJob use only
     * @param s status to set
     */
    public void setStatus(Status s)
    {
        status = s;
    }

    /**
     * Get Status of the Path
     * @return status
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @return true if the path is still computing or being followed
     */
    public boolean isInProgress() { return isComputing() || status == Status.IN_PROGRESS_FOLLOWING; }

    /**
     * @return true if the path was cancelled before being computed or before the entity reached it's destination
     */
    public boolean isCancelled() { return status == Status.CANCELLED; }

    /**
     * For PathNavigate and PathJob use only
     * @param value
     */
    public void setPathReachesDestination(boolean value) { pathReachesDestination = value; }

    /**
     * @return true if the path is computed, and it reaches a desired destination
     */
    public boolean getPathReachesDestination() { return pathReachesDestination; }

    /**
     * For PathNavigate use only
     * @param l
     */
    public void setPathLength(int l) { pathLength = l; }

    /**
     * @return length of the compute path, in nodes
     */
    public int getPathLength()
    {
        return pathLength;
    }

    /**
     * @return true if the path moves from the current location, useful for checking if a path actually generated
     */
    public boolean didPathGenerate()
    {
        return pathLength > 0;
    }

    public boolean isComputing() { return status == Status.IN_PROGRESS_COMPUTING; }
}
