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
     * The different connection types.
     */
    public enum PendingConnectionType
    {
        DEFAULT,
        FIX_PATH,
        CONNECT_COLONY
    }

    /**
     * The connection type.
     */
    private PendingConnectionType connectionType;

    /**
     * Cached path result.
     */
    private PathResult<PathJobMoveToLocation> cachedPathResult;

    /**
     * Create a new pending node.
     * @param pos it's pos.
     * @param pathResult the path result.
     * @param connectionType the connection type.
     */
    public PendingConnectionNode(
        final BlockPos pos,
        final PathResult<PathJobMoveToLocation> pathResult,
        final PendingConnectionType connectionType)
    {
        super(pos);
        this.cachedPathResult = pathResult;
        this.connectionType = connectionType;
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
        compound.putInt(TAG_CONNECTION_TYPE, connectionType.ordinal());
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
        this.connectionType = PendingConnectionType.values()[compound.getInt(TAG_CONNECTION_TYPE)];
    }

    /**
     * If this is a regular connection, or a mending connection (pending connection does not destroy sign if fail to path) or a colony connection.
     * @return the enum type.
     */
    public PendingConnectionType getPendingConnectionType()
    {
        return connectionType;
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
