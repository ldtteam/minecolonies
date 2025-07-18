package com.minecolonies.api.colony.connections;

import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Node in the path from one colony to another.
 */
public class ColonyConnectionNode
{
    /**
     * The previous point in the connection grid.
     */
    private BlockPos previousNode = BlockPos.ZERO;

    /**
     * The next point in the connection grid.
     */
    private BlockPos nextNode = BlockPos.ZERO;

    /**
     * Position of this point.
     */
    private final BlockPos position;

    /**
     * Connected colony.
     */
    private int targetColonyId = -1;

    /**
     * Create a new connection node at a given pos.
     * @param position the pos.
     */
    public ColonyConnectionNode(final BlockPos position)
    {
        this.position = position;
    }

    /**
     * Alter the previous node connection.
     * @param previousNode the previous node pos.
     */
    public void alterPreviousNode(final BlockPos previousNode)
    {
        this.previousNode = previousNode;
    }

    /**
     * Alter the next node connection.
     * @param nextNode the next nod epos.
     */
    public void alterNextNode(final BlockPos nextNode)
    {
        this.nextNode = nextNode;
    }

    /**
     * Get position of this point.
     * @return the position.
     */
    public BlockPos getPosition()
    {
        return position;
    }

    /**
     * Get the previous node.
     * @return prev node pos or zero if not set.
     */
    public BlockPos getPreviousNode()
    {
        return previousNode;
    }

    /**
     * Get the next node.
     * @return next node pos or ZERO if not set.
     */
    public BlockPos getNextNode()
    {
        return nextNode;
    }

    /**
     * Get the connected target colony id.
     * @return the target colony id.
     */
    public int getTargetColonyId()
    {
        return targetColonyId;
    }

    /**
     * Set the target colony id.
     * @param targetColonyId the id to set.
     */
    public void setTargetColonyId(final int targetColonyId)
    {
        this.targetColonyId = targetColonyId;
    }

    /**
     * Write connections to NBT data for saving.
     * @return compound NBT-Tag.
     */
    public CompoundTag write()
    {
        final CompoundTag compound = new CompoundTag();
        BlockPosUtil.write(compound, TAG_POS, position);
        BlockPosUtil.write(compound, TAG_PREV_POS, previousNode);
        BlockPosUtil.write(compound, TAG_NEXT_POS, nextNode);
        compound.putInt(TAG_TARGET_COLONY_ID, targetColonyId);
        return compound;
    }

    /**
     * Read connections from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    public void read(@NotNull final CompoundTag compound)
    {
        this.previousNode = BlockPosUtil.read(compound, TAG_PREV_POS);
        this.nextNode = BlockPosUtil.read(compound, TAG_NEXT_POS);
        this.targetColonyId = compound.getInt(TAG_TARGET_COLONY_ID);
    }

    /**
     * If another node can connect to this.
     * @return true if so.
     */
    public boolean hasNextNode()
    {
        return !nextNode.equals(BlockPos.ZERO);
    }
}
