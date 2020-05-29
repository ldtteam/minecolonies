package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class extending pathPoint for our usage with ladders.
 */
public class PathPointExtended extends PathPoint
{
    /**
     * Is the point on a ladder.
     */
    private boolean    onLadder     = false;
    /**
     * What direction does the ladder face.
     * Should be instantiated to something he doesn't recognize as climbable.
     */
    private Direction ladderFacing = Direction.DOWN;

    /**
     * Rails params.
     */
    private boolean onRails;
    private boolean railsEntry;
    private boolean railsExit;

    /**
     * Instantiates the pathPoint with a position.
     *
     * @param pos the position.
     */
    public PathPointExtended(@NotNull final BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Checks if the point is on a ladder.
     *
     * @return true if so.
     */
    public boolean isOnLadder()
    {
        return onLadder;
    }

    /**
     * Sets if the point is on a ladder.
     *
     * @param onLadder value to set.
     */
    public void setOnLadder(final boolean onLadder)
    {
        this.onLadder = onLadder;
    }

    /**
     * Get the facing of the ladder.
     *
     * @return Direction.
     */
    public Direction getLadderFacing()
    {
        return ladderFacing;
    }

    /**
     * Sets the facing of the ladder.
     *
     * @param ladderFacing facing to set.
     */
    public void setLadderFacing(final Direction ladderFacing)
    {
        this.ladderFacing = ladderFacing;
    }

    /**
     * Set if it is on rails.
     * @param isOnRails if on rails.
     */
    public void setOnRails(final boolean isOnRails)
    {
        this.onRails = isOnRails;
    }

    /**
     * Set the rails entry.
     */
    public void setRailsEntry()
    {
        this.railsEntry = true;
    }

    /**
     * Set the rails exit.
     */
    public void setRailsExit()
    {
        this.railsExit = true;
    }

    /**
     * Whether this is on rails.
     * @return true if so.
     */
    public boolean isOnRails()
    {
        return onRails;
    }

    /**
     * Whether this is the rails entry.
     * @return true if so.
     */
    public boolean isRailsEntry()
    {
        return railsEntry;
    }

    /**
     * Whether this is the rails exit.
     * @return true if so.
     */
    public boolean isRailsExit()
    {
        return railsExit;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final PathPointExtended that = (PathPointExtended) o;

        if (onLadder != that.onLadder)
        {
            return false;
        }
        return ladderFacing == that.ladderFacing;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (onLadder ? 1 : 0);
        result = 31 * result + ladderFacing.hashCode();
        return result;
    }
}
