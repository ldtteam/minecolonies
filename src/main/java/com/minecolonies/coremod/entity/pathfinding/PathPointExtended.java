package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
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
    private EnumFacing ladderFacing = EnumFacing.DOWN;

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
     * @return enumFacing.
     */
    public EnumFacing getLadderFacing()
    {
        return ladderFacing;
    }

    /**
     * Sets the facing of the ladder.
     *
     * @param ladderFacing facing to set.
     */
    public void setLadderFacing(final EnumFacing ladderFacing)
    {
        this.ladderFacing = ladderFacing;
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
