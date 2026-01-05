package com.minecolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for storing a mutable vector
 */
public class Vec3Mutable
{
    double x;
    double y;
    double z;

    public Vec3Mutable(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos asBlockPos()
    {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public Vec3 asVec3()
    {
        return new Vec3(x, y, z);
    }

    public boolean empty()
    {
        return x == 0 && y == 0 && z == 0;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public int getXi()
    {
        return Mth.floor(x);
    }

    public int getYi()
    {
        return Mth.floor(y);
    }

    public int getZi()
    {
        return Mth.floor(z);
    }
}
