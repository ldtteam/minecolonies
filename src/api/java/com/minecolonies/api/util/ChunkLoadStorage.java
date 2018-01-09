package com.minecolonies.api.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * The chunkload storage used to load chunks with colony information.
 */
public class ChunkLoadStorage
{
    /**
     * Compound tag used to store the add boolean to NBT.
     */
    private static final String TAG_ADD = "add";

    /**
     * The colony id.
     */
    private final int      colonyId;

    /**
     * The position of the chunk.
     */
    private final BlockPos pos;

    /**
     * If add or remove.
     */
    private final boolean add;

    /**
     * Intitialize a ChunLoadStorage from nbt.
     * @param compound the compound to use.
     */
    public ChunkLoadStorage(final NBTTagCompound compound)
    {
        this.colonyId = compound.getInteger(TAG_ID);
        this.pos = BlockPosUtil.readFromNBT(compound, TAG_POS);
        this.add = compound.getBoolean(TAG_ADD);
    }

    /**
     * Create a new chunkload storage.
     * @param colonyId the id of the colony.
     * @param pos the chunk pos.
     * @param add the operation type.
     */
    public ChunkLoadStorage(final int colonyId, final BlockPos pos, final boolean add)
    {
        this.colonyId = colonyId;
        this.pos = pos;
        this.add = add;
    }

    /**
     * Write the ChunkLoadStorage to NBT.
     * @return the compound.
     */
    public NBTTagCompound toNBT()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_ID, colonyId);
        BlockPosUtil.writeToNBT(compound, TAG_POS, pos);
        compound.setBoolean(TAG_ADD, add);
        return compound;
    }

    /**
     * Getter for the colonyId.
     * @return the id.
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Getter for the position.
     * @return the position.
     */
    public BlockPos getPos()
    {
        return pos;
    }

    /**
     * Check if add operation.
     * @return true if so.
     */
    public boolean isAdd()
    {
        return add;
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
        final ChunkLoadStorage that = (ChunkLoadStorage) o;
        return colonyId == that.colonyId &&
                add == that.add &&
                Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(colonyId, pos, add);
    }
}
