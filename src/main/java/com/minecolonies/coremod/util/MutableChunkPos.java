package com.minecolonies.coremod.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class MutableChunkPos extends ChunkPos
{
    private int mutableX;
    private int mutableZ;

    public MutableChunkPos(final int x, int z)
    {
        super(x, z);
        this.mutableX = x;
        this.mutableZ = z;
    }

    public MutableChunkPos(final BlockPos pos)
    {
        super(pos);
        this.mutableX = pos.getX() >> 4;
        this.mutableZ = pos.getZ() >> 4;
    }

    public MutableChunkPos(final long longIn)
    {
        super(longIn);
        this.mutableX = (int) longIn;
        this.mutableZ = (int) (longIn >> 32);
    }

    @Override
    public long toLong()
    {
        return asLong(this.mutableX, this.mutableZ);
    }

    @Override
    public int hashCode()
    {
        final int i = 1664525 * this.mutableX + 1013904223;
        final int j = 1664525 * (this.mutableZ ^ -559038737) + 1013904223;
        return i ^ j;
    }

    @Override
    public boolean equals(final Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof ChunkPos))
        {
            return false;
        }
        else
        {
            final ChunkPos chunkpos = (ChunkPos) p_equals_1_;
            return this.mutableX == chunkpos.x && this.mutableZ == chunkpos.z;
        }
    }

    @Override
    public int getMinBlockX()
    {
        return this.mutableX << 4;
    }

    @Override
    public int getMinBlockZ()
    {
        return this.mutableZ << 4;
    }

    @Override
    public int getMaxBlockX()
    {
        return (this.mutableX << 4) + 15;
    }

    @Override
    public int getMaxBlockZ()
    {
        return (this.mutableZ << 4) + 15;
    }

    @Override
    public int getRegionX()
    {
        return this.mutableX >> 5;
    }

    @Override
    public int getRegionZ()
    {
        return this.mutableZ >> 5;
    }

    @Override
    public int getRegionLocalX()
    {
        return this.mutableX & 31;
    }

    @Override
    public int getRegionLocalZ()
    {
        return this.mutableZ & 31;
    }

    @Override
    public String toString()
    {
        return "[" + this.mutableX + ", " + this.mutableZ + "]";
    }

    @Override
    public int getChessboardDistance(final ChunkPos chunkPosIn)
    {
        return Math.max(Math.abs(this.mutableX - chunkPosIn.x), Math.abs(this.mutableZ - chunkPosIn.z));
    }

    public int getX()
    {
        return mutableX;
    }

    public void setX(final int x)
    {
        this.mutableX = x;
    }

    public int getZ()
    {
        return mutableZ;
    }

    public void setZ(final int z)
    {
        this.mutableZ = z;
    }

    public void from(final ChunkPos chunkPos)
    {
        this.mutableX = chunkPos.x;
        this.mutableZ = chunkPos.z;
    }

    public ChunkPos toImmutable()
    {
        return new ChunkPos(this.mutableX, this.mutableZ);
    }
}
