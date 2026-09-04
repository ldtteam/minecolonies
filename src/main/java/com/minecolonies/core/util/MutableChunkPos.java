package com.minecolonies.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

/**
 * A mutable chunk coordinate intended for short-lived calculations.
 */
public final class MutableChunkPos
{
    private int x;
    private int z;

    public MutableChunkPos(final int x, final int z)
    {
        this.x = x;
        this.z = z;
    }

    public MutableChunkPos(final BlockPos pos)
    {
        this(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    public MutableChunkPos(final long packed)
    {
        this(ChunkPos.getX(packed), ChunkPos.getZ(packed));
    }

    public long toLong()
    {
        return ChunkPos.pack(x, z);
    }

    @Override
    public int hashCode()
    {
        return ChunkPos.hash(x, z);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        return obj instanceof ChunkPos pos && x == pos.x() && z == pos.z();
    }

    public int getMinBlockX()
    {
        return SectionPos.sectionToBlockCoord(x);
    }

    public int getMinBlockZ()
    {
        return SectionPos.sectionToBlockCoord(z);
    }

    public int getBlockX(final int offset)
    {
        return SectionPos.sectionToBlockCoord(x, offset);
    }

    public int getBlockZ(final int offset)
    {
        return SectionPos.sectionToBlockCoord(z, offset);
    }

    public int getRegionX()
    {
        return x >> 5;
    }

    public int getRegionZ()
    {
        return z >> 5;
    }

    public int getRegionLocalX()
    {
        return x & 31;
    }

    public int getRegionLocalZ()
    {
        return z & 31;
    }

    public int getChessboardDistance(final ChunkPos pos)
    {
        return Math.max(Math.abs(x - pos.x()), Math.abs(z - pos.z()));
    }

    public int getChessboardDistance(final MutableChunkPos pos)
    {
        return Math.max(Math.abs(x - pos.x), Math.abs(z - pos.z));
    }

    public int getX()
    {
        return x;
    }

    public void setX(final int x)
    {
        this.x = x;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(final int z)
    {
        this.z = z;
    }

    public void from(final ChunkPos pos)
    {
        x = pos.x();
        z = pos.z();
    }

    public ChunkPos toImmutable()
    {
        return new ChunkPos(x, z);
    }

    @Override
    public String toString()
    {
        return "[" + x + ", " + z + "]";
    }
}
