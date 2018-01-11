package com.minecolonies.api.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DIMENSION;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * ChunkPos class to retrieve a chunk from a hashmap.
 */
public class ChunkPos
{
    /**
     * X pos
     */
    private final int x;

    /**
     * Z pos.
     */
    private final int z;

    /**
     * The dimension.
     */
    private final int dim;

    /**
     * Intitialize a ChunkPos from nbt.
     * @param compound the compound to use.
     */
    public ChunkPos(final NBTTagCompound compound)
    {
        final BlockPos pos = BlockPosUtil.readFromNBT(compound, TAG_POS);
        this.x = pos.getX();
        this.z = pos.getZ();
        this.dim = compound.getInteger(TAG_DIMENSION);
    }

    /**
     * Create a new chunkPos.
     * @param x the x value.
     * @param z the z value.
     * @param dim the dimension.
     */
    public ChunkPos(final int x, final int z, final int dim)
    {
        this.x = x;
        this.z = z;
        this.dim = dim;
    }

    /**
     * Getter for the x value.
     * @return the x int.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Getter for the z value.
     * @return the z int.
     */
    public int getZ()
    {
        return z;
    }

    /**
     * Getter for the dim.
     * @return the dim int.
     */
    public int getDim()
    {
        return dim;
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
        final ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x &&
                z == chunkPos.z &&
                dim == chunkPos.dim;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(x, z, dim);
    }
}
