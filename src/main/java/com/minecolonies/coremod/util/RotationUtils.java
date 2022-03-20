package com.minecolonies.coremod.util;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;

public class RotationUtils
{

    private RotationUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: RotationUtils. This is a utility class");
    }

    public static Rotation fromVector(final Vec3i vector) {
        if (vector.getX() >= 0 && vector.getZ() >= 0) {
            return Rotation.NONE;
        }

        if (vector.getX() < 0 && vector.getZ() < 0) {
            return Rotation.CLOCKWISE_180;
        }

        if (vector.getX() < 0 && vector.getZ() >= 0) {
            return Rotation.CLOCKWISE_90;
        }

        if (vector.getX() >= 0 && vector.getZ() < 0) {
            return Rotation.COUNTERCLOCKWISE_90;
        }

        return Rotation.NONE;
    }
}
