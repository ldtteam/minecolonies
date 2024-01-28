package com.minecolonies.api.util;

import org.jetbrains.annotations.NotNull;

/**
 * Data structure to hold a two dimensional point. Uses x and z, because y is typically the third dimension in minecraft.
 *
 * @author Colton
 */
public class Vec2i
{
    private final int x;
    private final int z;

    public Vec2i(final int x, final int z)
    {
        this.x = x;
        this.z = z;
    }

    public long distanceSq(@NotNull final Vec2i vec2i)
    {
        return distanceSq(vec2i.getX(), vec2i.getZ());
    }

    public long distanceSq(final int x, final int y)
    {
        final long xDiff = (long) this.getX() - x;
        final long zDiff = (long) this.getZ() - y;

        return xDiff * xDiff + zDiff * zDiff;
    }

    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }

    @Override
    public int hashCode()
    {
        return (x << 16) + z;
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

        final Vec2i vec2i = (Vec2i) o;

        return x == vec2i.x && z == vec2i.z;
    }
}
