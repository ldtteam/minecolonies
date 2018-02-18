package com.minecolonies.api.util;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DIMENSION;
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
    private final int colonyId;

    /**
     * XZ pos as long.
     */
    private final long xz;

    /**
     * If add or remove.
     */
    private final boolean add;

    /**
     * The dimension of the chunk.
     */
    private final int dimension;

    /**
     * Intitialize a ChunLoadStorage from nbt.
     * @param compound the compound to use.
     */
    public ChunkLoadStorage(final NBTTagCompound compound)
    {
        this.colonyId = compound.getInteger(TAG_ID);
        this.xz = compound.getLong(TAG_POS);
        this.add = compound.getBoolean(TAG_ADD);
        this.dimension = compound.getInteger(TAG_DIMENSION);
    }

    /**
     * Create a new chunkload storage.
     * @param colonyId the id of the colony.
     * @param xz the chunk xz.
     * @param add the operation type.
     * @param dimension the dimension.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final boolean add, final int dimension)
    {
        this.colonyId = colonyId;
        this.xz = xz;
        this.add = add;
        this.dimension = dimension;
    }

    /**
     * Write the ChunkLoadStorage to NBT.
     * @return the compound.
     */
    public NBTTagCompound toNBT()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_ID, colonyId);
        compound.setLong(TAG_POS, xz);
        compound.setBoolean(TAG_ADD, add);
        compound.setInteger(TAG_DIMENSION, dimension);
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
     * Check if add operation.
     * @return true if so.
     */
    public boolean isAdd()
    {
        return add;
    }

    /**
     * Getter for the dimension.
     * @return the dimension id.
     */
    public int getDimension()
    {
        return dimension;
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
        final ChunkLoadStorage storage = (ChunkLoadStorage) o;
        return colonyId == storage.colonyId &&
                add == storage.add &&
                dimension == storage.dimension &&
                xz == storage.xz;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(colonyId, xz, add, dimension);
    }
}
