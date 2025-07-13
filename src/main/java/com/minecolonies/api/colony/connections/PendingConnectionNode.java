package com.minecolonies.api.colony.connections;

import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveToLocation;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Pending connected colony data while pathfinding is still trying to connect.
 */
public class PendingConnectionNode extends ColonyConnectionNode
{
    /**
     * If it is trying to mend a path, we won't destroy the origin pos.
     */
    private boolean isPathMending;

    /**
     * Cached path result.
     */
    private PathResult<PathJobMoveToLocation> cachedPathResult;

    /**
     * Create a new pending node.
     * @param pos it's pos.
     * @param pathResult the path result.
     * @param isPathMending if it's path fixing one.
     */
    public PendingConnectionNode(
        final BlockPos pos,
        final PathResult<PathJobMoveToLocation> pathResult,
        final boolean isPathMending)
    {
        super(pos);
        this.cachedPathResult = pathResult;
        this.isPathMending = isPathMending;
    }

    /**
     * Constructor for deserialization/serialization.
     */
    public PendingConnectionNode(final BlockPos pos)
    {
        super(pos);
    }

    /**
     * Write connections to NBT data for saving.
     * @return compound NBT-Tag.
     */
    public CompoundTag write()
    {
        final CompoundTag compound = super.write();
        compound.putBoolean(TAG_MENDING, isPathMending);
        return compound;
    }

    /**
     * Read connections from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    public void read(@NotNull final CompoundTag compound)
    {
        super.read(compound);
        this.isPathMending = compound.getBoolean(TAG_MENDING);
    }

    /**
     * If this is a regular connection, or a mending connection (pending connection does not destroy sign if fail to path).
     * @return true if mending.
     */
    public boolean isPathMending()
    {
        return isPathMending;
    }

    /**
     * Store the cached path result.
     * @param cachedPathResult the cached path result.
     */
    public void setCachedPathResult(final PathResult<PathJobMoveToLocation> cachedPathResult)
    {
        this.cachedPathResult = cachedPathResult;
    }

    /**
     * Get the cached path result.
     *
     * @return the cached path result.
     */
    @Nullable
    public PathResult<PathJobMoveToLocation> getCachedPathResult()
    {
        return cachedPathResult;
    }
}
