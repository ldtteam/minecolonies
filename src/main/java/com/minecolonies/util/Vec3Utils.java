package com.minecolonies.util;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Vec3Utils
{
    /**
     * Returns the distance from a {@link Vec3} to a location
     *
     * @param vec Vector (point 1)
     * @param x   x-coordinate (point 2)
     * @param y   y-coordinate (point 2)
     * @param z   z-coordinate (point 2)
     * @return Distance between points
     */
    public static double distanceTo(Vec3 vec, double x, double y, double z)
    {
        return vec.distanceTo(Vec3.createVectorHelper(x, y, z));
    }

    /**
     * Returns whether or not the locations are the same
     *
     * @param vec Vector (point 1)
     * @param x   x-coordinate (point 2)
     * @param y   y-coordinate (point 2)
     * @param z   z-coordinate (point 2)
     * @return True if locations are equal, otherwise false
     */
    public static boolean equals(Vec3 vec, double x, double y, double z)
    {
        return equals(vec, Vec3.createVectorHelper(x, y, z));
    }

    /**
     * Returns whether or not the locations are the same
     *
     * @param vec1 Vector (point 1)
     * @param vec2 Vector (point 2)
     * @return True if locations are equal, otherwise false
     */
    public static boolean equals(Vec3 vec1, Vec3 vec2)
    {
        return vec1.xCoord == vec2.xCoord && vec1.yCoord == vec2.yCoord && vec1.zCoord == vec2.zCoord;
    }

    /**
     * Creates a floor {@link Vec3}
     *
     * @param vec Vector
     * @return Floor vector
     */
    public static Vec3 vec3Floor(Vec3 vec)
    {
        return Vec3.createVectorHelper(MathHelper.floor_double(vec.xCoord), MathHelper.floor_double(vec.yCoord), MathHelper.floor_double(vec.zCoord));
    }
}
